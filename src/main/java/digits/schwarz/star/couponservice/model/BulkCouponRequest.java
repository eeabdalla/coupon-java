package digits.schwarz.star.couponservice.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCouponRequest {

  @NotEmpty(message = "coupon list must not be empty")
  @Size(max = 1000, message = "batch size must not exceed 1000")
  private List<@Valid CouponModel> coupons;
}
