package digits.schwarz.star.couponservice.repository;

import digits.schwarz.star.couponservice.entity.CouponEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends MongoRepository<CouponEntity, ObjectId>, CustomCouponRepository {

    List<CouponEntity> findAllByCodeIn(List<String> codes);

}