package digits.schwarz.star.couponservice.service;

import digits.schwarz.star.couponservice.entity.CouponEntity;
import digits.schwarz.star.couponservice.mapper.CouponMapper;
import digits.schwarz.star.couponservice.model.CouponModel;
import digits.schwarz.star.couponservice.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("Get all coupons successfully")
    void getCoupons_returnsAllCoupons() {
        // given
        var entity = CouponEntity.builder().code("SUMMER20").build();
        var model = CouponModel.builder().code("SUMMER20").build();

        given(couponRepository.findAll()).willReturn(List.of(entity));
        given(couponMapper.toModel(entity)).willReturn(model);

        // when
        var result = couponService.getCoupons();

        // then
        assertThat(result)
                .hasSize(1)
                .containsExactly(model);

        then(couponRepository).should().findAll();
        then(couponMapper).should().toModel(entity);
        then(couponRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Save a coupon and set creation date time")
    void saveCoupon_setsCreationDateAndSaves() {
        // given
        var model = CouponModel.builder().code("NEW50").build();
        var entity = CouponEntity.builder().code("NEW50").build();

        given(couponMapper.toEntity(model)).willReturn(entity);

        // when
        couponService.saveCoupon(model);

        // then
        var entityCaptor = ArgumentCaptor.forClass(CouponEntity.class);

        then(couponMapper).should().toEntity(model);
        then(couponRepository).should().save(entityCaptor.capture());

        var savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getCode()).isEqualTo("NEW50");
        assertThat(savedEntity.getCreationDateTime()).isNotNull();
    }

    @Test
    @DisplayName("Cleanup coupons older than 5 minutes")
    void cleanup_deletesOldCoupons() {
        // given
        var expectedDuration = Duration.of(5, ChronoUnit.MINUTES);
        var expectedDeletedCount = 3L;

        given(couponRepository.deleteCouponsOlderThan(expectedDuration)).willReturn(expectedDeletedCount);

        // when
        couponService.cleanup();

        // then
        then(couponRepository).should().deleteCouponsOlderThan(expectedDuration);
        then(couponRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Get specific coupons by their codes")
    void getCouponsByCodes_returnsMatchingCoupons() {
        // given
        var codes = List.of("WINTER10", "SPRING15");

        var entity1 = CouponEntity.builder().code("WINTER10").build();
        var entity2 = CouponEntity.builder().code("SPRING15").build();

        var model1 = CouponModel.builder().code("WINTER10").build();
        var model2 = CouponModel.builder().code("SPRING15").build();

        given(couponRepository.findAllByCodeIn(codes)).willReturn(List.of(entity1, entity2));
        given(couponMapper.toModel(entity1)).willReturn(model1);
        given(couponMapper.toModel(entity2)).willReturn(model2);

        // when
        var result = couponService.getCouponsByCodes(codes);

        // then
        assertThat(result)
                .hasSize(2)
                .containsExactly(model1, model2);

        then(couponRepository).should().findAllByCodeIn(codes);
        then(couponMapper).should().toModel(entity1);
        then(couponMapper).should().toModel(entity2);
        then(couponRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Save multiple coupons sets creationDateTime and returns mapped models")
    void saveCoupons_setsCreationDateAndReturnsMappedModels() {
        // given
        var model1 = CouponModel.builder().code("A1").build();
        var model2 = CouponModel.builder().code("A2").build();

        var entity1 = CouponEntity.builder().code("A1").build();
        var entity2 = CouponEntity.builder().code("A2").build();

        given(couponMapper.toEntity(model1)).willReturn(entity1);
        given(couponMapper.toEntity(model2)).willReturn(entity2);
        given(couponRepository.saveAll(any())).willReturn(List.of(entity1, entity2));
        given(couponMapper.toModel(entity1)).willReturn(model1);
        given(couponMapper.toModel(entity2)).willReturn(model2);

        // when
        var result = couponService.saveCoupons(List.of(model1, model2));

        // then
        assertThat(result).hasSize(2).containsExactly(model1, model2);
        assertThat(entity1.getCreationDateTime()).isNotNull();
        assertThat(entity2.getCreationDateTime()).isNotNull();
        assertThat(entity1.getCreationDateTime()).isEqualTo(entity2.getCreationDateTime()); // same timestamp

        then(couponRepository).should().saveAll(any());
    }
}