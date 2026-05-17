package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for card cash-in (funding) execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariCardFundingExecutionResponse {

    private CardFundingExecutionData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CardFundingExecutionData {

        private Boolean redirect;

        private BigDecimal amount;

        private String transactionTrackId;

        private String orderId;

        private String transactionReferenceId;

        private String redirectionURL;

        private String acceptURL;

        private String declineURL;

        private String status;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;
    }
}
