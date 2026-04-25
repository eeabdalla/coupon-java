package digits.schwarz.star.couponservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponModel {

    @NotBlank(message = "coupon code must be provided")
    private String code;
    @NotNull(message = "discount must be provided")
    @Positive(message = "discount must be a positive value")
    private BigDecimal discount;
    @NotBlank(message = "coupon description must be provided")
    private String description;
    private Integer applicationCount;

}
