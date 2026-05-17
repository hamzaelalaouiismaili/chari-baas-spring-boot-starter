package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for merchant payment by phone preview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariMerchantPaymentByPhonePreviewResponse {

    private MerchantPaymentByPhonePreviewData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MerchantPaymentByPhonePreviewData {

        private Integer type;

        private MerchantPaymentByPhonePreviewOperation operation;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private String checkedAt;

        private Boolean openLoop;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(type);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MerchantPaymentByPhonePreviewOperation {

        private String customerPhoneNumber;

        private BigDecimal amount;

        private String reason;

        private String recipientPhoneNumber;

        private Integer beneficiaryId;
    }
}
