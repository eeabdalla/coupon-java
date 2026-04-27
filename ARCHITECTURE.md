# Part 2: Architecture & Code Review

## 1. Scalability: Risks in the Current GET Endpoint

### The Problem

The current `GET /coupons` endpoint calls `couponRepository.findAll()`, which loads **every document in the collection into memory** at once:

```java
public List<CouponModel> getCoupons() {
    return couponRepository.findAll().stream()
            .map(couponMapper::toModel)
            .toList();
}
```

With millions of records, this causes:

| Risk | Impact |
|------|--------|
| **OutOfMemoryError** | The JVM heap fills up trying to hold millions of `CouponEntity` objects + their mapped `CouponModel` copies |
| **Extreme latency** | Serializing millions of objects to JSON |
| **MongoDB cursor timeout** | The default cursor timeout (10 min) can be exceeded on very large result sets |
| **Network saturation** | A multi-GB JSON response overwhelms bandwidth between service and client |
| **GC pressure** | Even if it doesn't OOM, massive allocations cause long GC pauses, affecting all concurrent requests |

### The Solution: Pagination

Replace `findAll()` with paginated queries using Spring Data's `Pageable`:

**Controller:**
```java
@GetMapping
public ResponseEntity<Page<CouponModel>> getCoupons(
        @RequestParam(required = false) List<String> codes,
        @PageableDefault(size = 20, sort = "creationDateTime", direction = Sort.Direction.DESC) Pageable pageable
) {
    if (codes == null || codes.isEmpty()) {
        return ResponseEntity.ok(couponService.getCoupons(pageable));
    } else {
        return ResponseEntity.ok(couponService.getCouponsByCodes(codes, pageable));
    }
}
```

**Service:**
```java
public Page<CouponModel> getCoupons(Pageable pageable) {
    return couponRepository.findAll(pageable)
            .map(couponMapper::toModel);
}
```

This returns bounded result sets (e.g., 20 items per page) with metadata:
```json
{
  "content": [...],
  "totalElements": 5000000,
  "totalPages": 250000,
  "number": 0,
  "size": 20
}
```

**Additional measures for millions of records:**
- **Add indexes** on frequently queried fields: `@Indexed` on `code`, `creationDateTime`
- **Add `@Indexed(unique = true)` on `code`** to enforce uniqueness and speed up `findAllByCodeIn`
- **Set a max page size** (e.g., 100) to prevent clients from requesting `?size=9999999`
---

## 2. Persistence: Simplifying the Cleanup Logic

### Current Implementation

The cleanup uses **three custom components** working together:

1. `CouponCleanupScheduler` — Spring `@Scheduled` cron job running every 5 minutes
2. `CustomCouponRepository` — interface defining `deleteCouponsOlderThan(Duration)`
3. `CustomCouponRepositoryImpl` — uses `MongoTemplate` to manually build a query and delete

**Problems:**
- The 5-minute duration is **hardcoded** in the service
- The cleanup only runs at cron intervals — expired documents **linger** between runs
- 3 classes + configuration for something MongoDB can do natively
- If the application is down, **no cleanup happens at all**

### The Solution: MongoDB TTL Index

MongoDB natively supports **TTL (Time-To-Live) indexes** that automatically expire and delete documents — no application code needed.

**Replace all cleanup code with a single annotation on the entity:**

```java
@Data
@Document("coupons")
public class CouponEntity {

    @MongoId
    private ObjectId id;

    private String code;
    private BigDecimal discount;
    private String description;
    private Integer applicationCount;

    @Indexed(expireAfter = "5m")   // ← MongoDB deletes documents 5 minutes after this timestamp
    private Instant creationDateTime;
}
```

**What this allows us to delete:**
- `CouponCleanupScheduler.java`
- `CustomCouponRepository.java`
- `CustomCouponRepositoryImpl.java`
- `CouponService.cleanup()`
- `{coupon.cleanup.cron}`
- `@EnableScheduling`

**Benefits:**
- MongoDB's background thread handles expiration continuously (checks every 60 seconds)
- Works even if the application is completely down
- Less application code to maintain
- The TTL duration is visible and co-located with the entity definition, it doens't require digging through service code to find it
- If the duration needs to change, it can be updated via a MongoDB command without redeploying the app

**Trade-off to be aware of:** MongoDB's TTL background task runs every 60 seconds, so documents may persist up to ~60 seconds past their expiration. For a coupon system, this is acceptable.

---

## 3. Resilience: Protecting Against Excessively Large Payloads

### Potential Bottlenecks

If a client sends an excessively large payload to `POST /coupons/batch`, several bottlenecks can occur at different layers:

| Layer | Bottleneck | Impact |
|-------|-----------|--------|
| **HTTP/Tomcat** | Parsing a huge JSON body consumes thread time and memory | Thread pool exhaustion — other requests can't be served |
| **JVM Heap** | Deserializing thousands of `CouponModel` objects at once | `OutOfMemoryError`, service crash |
| **Validation** | Validating thousands of objects with `@Valid` is CPU-intensive | Slow response, thread blocking |
| **MongoDB** | `saveAll()` with a massive list creates a single bulk write | Database timeout, write lock contention, oplog overflow |
| **Network** | Large request + large response saturate bandwidth | Latency spikes for all users |

### Protections Already in Place

The `BulkCouponRequest` already has a first line of defense:

```java
@NotEmpty(message = "coupon list must not be empty")
@Size(max = 1000, message = "batch size must not exceed 1000")
private List<@Valid CouponModel> coupons;
```

This caps the list at 1000 items **after** deserialization. But the JSON must still be fully parsed first.

### Additional Protections

**1. Limit request body size at the server level (before deserialization):**

```properties
# application.properties
server.tomcat.max-http-post-size=1MB
spring.servlet.multipart.max-request-size=1MB
```

This rejects oversized payloads **immediately** at the Tomcat layer — before JSON parsing even starts.

**2. Chunk the database writes:**

Instead of a single `saveAll()` with 1000 documents, process in smaller batches to reduce MongoDB pressure:

```java
public List<CouponModel> saveCoupons(List<CouponModel> models) {
    var now = Instant.now();
    var entities = models.stream()
        .map(model -> {
            var entity = couponMapper.toEntity(model);
            entity.setCreationDateTime(now);
            return entity;
        })
        .toList();

    // Save in chunks of 100 to avoid overwhelming MongoDB
    var saved = Lists.partition(entities, 100).stream()
        .flatMap(chunk -> couponRepository.saveAll(chunk).stream())
        .toList();

    return saved.stream()
        .map(couponMapper::toModel)
        .toList();
}
```

**3. Rate limiting:**

Use a library like Bucket4j or Spring Cloud Gateway to limit how frequently a client can call the batch endpoint:

```java
@PostMapping("/batch")
@RateLimiter(name = "batchInsert", fallbackMethod = "rateLimitFallback")
public ResponseEntity<List<CouponModel>> saveCoupons(...) { ... }
```

**4. Request timeout:**

Configure a timeout so a slow bulk insert doesn't hold a thread indefinitely:

```properties
spring.mvc.async.request-timeout=30000
```