package digits.schwarz.star.couponservice.mapper;

import digits.schwarz.star.couponservice.entity.CouponEntity;
import digits.schwarz.star.couponservice.model.CouponModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    private final CouponMapper couponMapper = new CouponMapper();

    @Test
    @DisplayName("Map CouponEntity to CouponModel successfully")
    void toModel_mapsAllFields() {
        // given
        var entity = CouponEntity.builder()
                .code("SUMMER20")
                .discount(new BigDecimal("20.00"))
                .description("Summer Sale Discount")
                .applicationCount(100)
                .build();

        // when
        var result = couponMapper.toModel(entity);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SUMMER20");
        assertThat(result.getDiscount()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getDescription()).isEqualTo("Summer Sale Discount");
        assertThat(result.getApplicationCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("Map CouponModel to CouponEntity successfully")
    void toEntity_mapsAllFields() {
        // given
        var model = CouponModel.builder()
                .code("WINTER10")
                .discount(new BigDecimal("10.00"))
                .description("Winter Sale Discount")
                .applicationCount(50)
                .build();

        // when
        var result = couponMapper.toEntity(model);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("WINTER10");
        assertThat(result.getDiscount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getDescription()).isEqualTo("Winter Sale Discount");
        assertThat(result.getApplicationCount()).isEqualTo(50);
    }
}