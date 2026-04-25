package digits.schwarz.star.couponservice.controller;

import digits.schwarz.star.couponservice.model.BulkCouponRequest;
import digits.schwarz.star.couponservice.model.CouponModel;
import digits.schwarz.star.couponservice.service.CouponService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<CouponModel>> getCouponsByCodes(
            @RequestParam(required = false) List<String> codes
    ) {
        if (codes == null || codes.isEmpty()) {
            return ResponseEntity.ok(couponService.getCoupons());
        } else {
            return ResponseEntity.ok(couponService.getCouponsByCodes(codes));
        }
    }

    @PostMapping
    public ResponseEntity<Void> saveCoupon(@Valid @RequestBody CouponModel model) {
            couponService.saveCoupon(model);
            return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CouponModel>> saveCoupons(@Valid @RequestBody BulkCouponRequest request) {
        var saved = couponService.saveCoupons(request.getCoupons());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}
