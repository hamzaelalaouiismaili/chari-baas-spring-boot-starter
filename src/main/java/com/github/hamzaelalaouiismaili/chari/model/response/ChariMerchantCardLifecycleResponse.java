package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for merchant card capture, reverse, and refund operations.
 * POST /api/operations/merchant/payment/card/capture
 * POST /api/operations/merchant/payment/card/reverse
 * POST /api/operations/merchant/payment/card/refund
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariMerchantCardLifecycleResponse {

    private MerchantCardLifecycleData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MerchantCardLifecycleData {

        private String phoneNumber;

        private Long operationId;

        private BigDecimal refundAmount;

        private String orderId;

        private String transactionTrackId;
    }
}
