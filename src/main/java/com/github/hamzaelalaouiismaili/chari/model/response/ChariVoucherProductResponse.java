package com.github.hamzaelalaouiismaili.chari.model.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Detailed voucher product information (Blackhawk product management shape). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherProductResponse {

    private ProductData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductData {

        private Integer capProductId;
        private String capDefaultUpc;
        private String name;
        private String blackhawkId;
        private BigDecimal priceUsd;
        private BigDecimal priceMadCurrent;
        private String productConfigId;
        private String productDescription;
        private List<ProductImage> productImages;
        private ActivationCharacteristics activationCharacteristics;
        private RedemptionCharacteristics redemptionCharacteristics;
        private List<TermsAndConditions> termsAndConditions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImage {

        private String id;
        private String imageSize;
        private String frontImage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivationCharacteristics {

        private BigDecimal maxValueAmount;
        private BigDecimal baseValueAmount;
        private Boolean isVariableValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedemptionCharacteristics {

        private List<String> redemptionOptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermsAndConditions {

        private String id;
        private String termsAndConditions;
        private String termsAndConditionsType;
    }
}
