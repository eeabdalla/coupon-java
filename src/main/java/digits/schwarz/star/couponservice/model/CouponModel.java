package digits.schwarz.star.couponservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponModel {

    @NotBlank(message = "coupon code must be provided")
    private String code;
    @NotBlank(message = "discount must be provided")
    private BigDecimal discount;
    @NotBlank(message = "coupon description must be provided")
    private String description;
    private Integer applicationCount;

}
