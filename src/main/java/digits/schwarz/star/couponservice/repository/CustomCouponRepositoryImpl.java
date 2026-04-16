package digits.schwarz.star.couponservice.repository;

import digits.schwarz.star.couponservice.entity.CouponEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class CustomCouponRepositoryImpl implements CustomCouponRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public long deleteCouponsOlderThan(Duration duration) {
        log.debug("Deleting all coupons older than {}", duration);

        var olderThan = Instant.now().minus(duration);

        var query = Query.query(Criteria.where("creationDateTime").lt(olderThan));

        var result = mongoTemplate.remove(
                query,
                CouponEntity.class
        );

        return result.getDeletedCount();
    }

}
