package digits.schwarz.star.couponservice.mapper;

import digits.schwarz.star.couponservice.entity.CouponEntity;
import digits.schwarz.star.couponservice.model.CouponModel;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponModel toModel(CouponEntity entity) {
        return CouponModel.builder()
                .code(entity.getCode())
                .discount(entity.getDiscount())
                .description(entity.getDescription())
                .applicationCount(entity.getApplicationCount())
                .build();
    }

    public CouponEntity toEntity(CouponModel model) {
        return CouponEntity.builder()
                .code(model.getCode())
                .discount(model.getDiscount())
                .description(model.getDescription())
                .applicationCount(model.getApplicationCount())
                .build();
    }

}
