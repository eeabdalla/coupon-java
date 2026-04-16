package digits.schwarz.star.couponservice.controller;

import digits.schwarz.star.couponservice.model.CouponModel;
import digits.schwarz.star.couponservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponRestController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponModel>> getCouponUseCase() {
        return ResponseEntity.ok(couponService.getCoupons());
    }

    @PostMapping
    public ResponseEntity<Void> saveCouponUseCase(@RequestBody CouponModel model) {
        try {
            couponService.saveCoupon(model);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception exception) {
            log.error("Saving coupon was not possible", exception);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
