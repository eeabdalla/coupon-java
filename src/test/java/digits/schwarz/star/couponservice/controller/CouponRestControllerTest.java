package digits.schwarz.star.couponservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import digits.schwarz.star.couponservice.model.BulkCouponRequest;
import digits.schwarz.star.couponservice.model.CouponModel;
import digits.schwarz.star.couponservice.repository.CouponRepository;
import digits.schwarz.star.couponservice.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponRestController.class)
class CouponRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private CouponRepository couponRepository;

    @MockitoBean
    private MongoMappingContext mongoMappingContext;

    @Test
    @DisplayName("Get all coupons when no codes are provided")
    void getCouponsByCodes_withoutCodes_returnsAllCoupons() throws Exception {
        // given
        var coupon = CouponModel.builder()
                .code("SUMMER20")
                .discount(new BigDecimal("20.0"))
                .description("Summer Sale")
                .applicationCount(100)
                .build();

        given(couponService.getCoupons()).willReturn(List.of(coupon));

        // when
        var response = mockMvc.perform(get("/coupons")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SUMMER20"))
                .andExpect(jsonPath("$[0].discount").value(20.0))
                .andExpect(jsonPath("$[0].description").value("Summer Sale"))
                .andExpect(jsonPath("$[0].applicationCount").value(100));

        then(couponService).should().getCoupons();
        then(couponService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Get specific coupons when codes are provided")
    void getCouponsByCodes_withCodes_returnsSpecificCoupons() throws Exception {
        // given
        var coupon = CouponModel.builder()
                .code("WINTER10")
                .discount(new BigDecimal("10.0"))
                .description("Winter Sale")
                .applicationCount(50)
                .build();
        var codes = List.of("WINTER10");

        given(couponService.getCouponsByCodes(codes)).willReturn(List.of(coupon));

        // when
        var response = mockMvc.perform(get("/coupons")
                .param("codes", "WINTER10")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("WINTER10"))
                .andExpect(jsonPath("$[0].discount").value(10.0));

        then(couponService).should().getCouponsByCodes(codes);
        then(couponService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Get all coupons when codes list is empty")
    void getCouponsByCodes_withEmptyCodesList_returnsAllCoupons() throws Exception {
        // given
        given(couponService.getCoupons()).willReturn(Collections.emptyList());

        // when
        var response = mockMvc.perform(get("/coupons")
                .param("codes", "")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        then(couponService).should().getCoupons();
    }

    @Test
    @DisplayName("Save a new coupon successfully")
    void saveCoupon_successful_returnsCreated() throws Exception {
        // given
        var newCoupon = CouponModel.builder()
                .code("NEW50")
                .discount(new BigDecimal("50.0"))
                .description("New User Discount")
                .applicationCount(0)
                .build();

        willDoNothing().given(couponService).saveCoupon(any(CouponModel.class));

        // when
        var response = mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCoupon)));

        // then
        response.andExpect(status().isCreated());

        then(couponService).should().saveCoupon(argThat(model ->
                model.getCode().equals("NEW50") &&
                        model.getDiscount().compareTo(new BigDecimal("50.0")) == 0
        ));
    }

    @Test
    @DisplayName("Return 500 with error body when saving fails unexpectedly")
    void saveCoupon_throwsException_returnsInternalServerError() throws Exception {
        // given
        var newCoupon = CouponModel.builder()
                .code("ERROR50")
                .discount(new BigDecimal("50.0"))
                .description("New User Discount")
                .build();

        willThrow(new RuntimeException("Database down")).given(couponService).saveCoupon(any(CouponModel.class));

        // when
        var response = mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCoupon)));

        // then
        response.andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.path").value("/coupons"));

        then(couponService).should().saveCoupon(any(CouponModel.class));
    }

    @Test
    @DisplayName("Save coupon with missing code returns 400 with field errors")
    void saveCoupon_missingCode_returnsValidationError() throws Exception {
      var invalid = CouponModel.builder()
          .discount(new BigDecimal("10.0"))
          .description("No code coupon")
          .build();

      mockMvc.perform(post("/coupons")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalid)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Validation Failed"))
          .andExpect(jsonPath("$.fieldErrors[?(@.field == 'code')].message")
              .value("coupon code must be provided"));
    }

    @Test
    @DisplayName("Save coupon with negative discount returns 400")
    void saveCoupon_negativeDiscount_returnsValidationError() throws Exception {
      var invalid = CouponModel.builder()
          .code("NEG")
          .discount(new BigDecimal("-5.0"))
          .description("Negative discount")
          .build();

      mockMvc.perform(post("/coupons")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalid)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.fieldErrors[?(@.field == 'discount')].message").value("discount must be a positive value"));
    }

    @Test
    @DisplayName("Save coupon with missing description returns 400")
    void saveCoupon_missingDescription_returnsValidationError() throws Exception {
      var invalid = CouponModel.builder()
          .code("NEG")
          .discount(new BigDecimal("5.0"))
          .build();

      mockMvc.perform(post("/coupons")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalid)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.fieldErrors[?(@.field == 'description')].message").value("coupon description must be provided"));
    }

    @Test
    @DisplayName("Malformed JSON returns 400 with Malformed Request error")
    void saveCoupon_malformedJson_returnsMalformedError() throws Exception {
      mockMvc.perform(post("/coupons")
              .contentType(MediaType.APPLICATION_JSON)
              .content("Invalid JSON"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Malformed Request"));
    }

    @Test
    @DisplayName("Batch save coupons successfully returns 201")
    void saveCoupons_successful_returnsCreated() throws Exception {
      var coupon1 = CouponModel.builder().code("A1").discount(new BigDecimal("10")).description("Desc1").build();
      var coupon2 = CouponModel.builder().code("A2").discount(new BigDecimal("20")).description("Desc2").build();
      var request = new BulkCouponRequest(List.of(coupon1, coupon2));

      given(couponService.saveCoupons(any())).willReturn(List.of(coupon1, coupon2));

      mockMvc.perform(post("/coupons/batch")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$[0].code").value("A1"))
          .andExpect(jsonPath("$[1].code").value("A2"));
    }

    @Test
    @DisplayName("Batch save with empty list returns 400")
    void saveCoupons_emptyList_returnsValidationError() throws Exception {
      var request = new BulkCouponRequest(List.of());

      mockMvc.perform(post("/coupons/batch")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Batch save with invalid coupon in list returns 400 with nested field errors")
    void saveCoupons_invalidCouponInList_returnsValidationError() throws Exception {
      var invalid = CouponModel.builder().discount(new BigDecimal("10")).description("No code").build();
      var request = new BulkCouponRequest(List.of(invalid));

      mockMvc.perform(post("/coupons/batch")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

}