package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for refund preview and execution.
 * POST /api/operations/refund/preview
 * POST /api/operations/refund
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariRefundResponse {

    private RefundData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefundData {

        private Integer type;

        private RefundOperation operation;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private String checkedAt;

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
    public static class RefundOperation {

        @JsonProperty("PhoneNumber")
        private String phoneNumber;

        private String customerPhoneNumber;

        private Long operationId;

        private BigDecimal refundAmount;

        private BigDecimal amount;

        private String reason;

        private Integer beneficiaryId;

        private String recipientPhoneNumber;

        private String orderId;

        private String transactionTrackId;
    }
}
