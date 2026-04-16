package digits.schwarz.star.couponservice.service;

import digits.schwarz.star.couponservice.mapper.CouponMapper;
import digits.schwarz.star.couponservice.model.CouponModel;
import digits.schwarz.star.couponservice.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public List<CouponModel> getCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toModel)
                .toList();
    }

    public void saveCoupon(CouponModel model) {
        var entity = couponMapper.toEntity(model);
        entity.setCreationDateTime(Instant.now());
        couponRepository.save(entity);
    }

    public void cleanup() {
        var deletedCount = couponRepository.deleteCouponsOlderThan(Duration.of(5, ChronoUnit.MINUTES));
        log.info("Deleted {} coupons", deletedCount);
    }

    public List<CouponModel> getCouponsByCodes(List<String> codes) {
        return couponRepository.findAllByCodeIn(codes).stream()
                .map(couponMapper::toModel)
                .toList();
    }
}
