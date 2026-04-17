package digits.schwarz.star.couponservice.scheduler;

import digits.schwarz.star.couponservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponCleanupScheduler {

    private final CouponService couponService;

    @Scheduled(cron = "${coupon.cleanup.cron}")
    public void doScheduledCleanup() {
        log.info("Starting cleanup of coupons.");
        couponService.cleanup();
        log.info("Cleaning up coupons done.");
    }

}
