package digits.schwarz.star.couponservice.scheduler;

import digits.schwarz.star.couponservice.entity.CouponEntity;
import digits.schwarz.star.couponservice.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(properties = {
        "coupon.cleanup.cron=*/1 * * * * *"
})
class CouponCleanupSchedulerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void cleanUp() {
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("Scheduler automatically deletes coupons older than 5 minutes from real MongoDB")
    void doScheduledCleanup_removesOldCouponsFromDatabase() {
        // given
        var now = Instant.now();

        var oldCoupon = CouponEntity.builder()
                .code("OLD_CODE")
                .discount(new BigDecimal("10.00"))
                .creationDateTime(now.minus(10, ChronoUnit.MINUTES))
                .build();

        var newCoupon = CouponEntity.builder()
                .code("FRESH_CODE")
                .discount(new BigDecimal("20.00"))
                .creationDateTime(now.minus(1, ChronoUnit.MINUTES))
                .build();

        couponRepository.saveAll(List.of(oldCoupon, newCoupon));

        // when & then
        await()
                .atMost(ofSeconds(5))
                .untilAsserted(() -> {
                    var remainingCoupons = couponRepository.findAll();

                    assertThat(remainingCoupons)
                            .extracting(CouponEntity::getCode)
                            .containsExactly("FRESH_CODE")
                            .doesNotContain("OLD_CODE");
                });
    }
}