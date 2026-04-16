package digits.schwarz.star.couponservice.repository;

import java.time.Duration;

public interface CustomCouponRepository {
    long deleteCouponsOlderThan(Duration duration);
}
