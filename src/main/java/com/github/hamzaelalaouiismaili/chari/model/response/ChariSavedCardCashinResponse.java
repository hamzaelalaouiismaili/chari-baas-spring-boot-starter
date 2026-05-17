package com.github.hamzaelalaouiismaili.chari.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response for cashin with saved card operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariSavedCardCashinResponse {

    private SavedCardCashinData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedCardCashinData {
        private String operationId;
        private Boolean redirect;
        private BigDecimal amount;
        private String transactionTrackId;
        private String orderId;
        private String transactionReferenceId;
        private BigDecimal feesAmount;
        private BigDecimal totalAmount;
        private String status;
        private String redirectionURL;
        private String acceptURL;
        private String declineURL;
        private String reference;
    }
}
