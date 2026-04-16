package digits.schwarz.star.couponservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Document("coupons")
@NoArgsConstructor
@AllArgsConstructor
public class CouponEntity {

    @MongoId
    private ObjectId id;

    private String code;
    private BigDecimal discount;
    private String description;
    private Integer applicationCount;

    private Instant creationDateTime;

}
