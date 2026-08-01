package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for executed merchant card payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariMerchantCardPaymentResponse {

    private MerchantCardPaymentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MerchantCardPaymentData {

        private Boolean redirect;

        private Integer responseCode;

        private BigDecimal amount;

        private String transactionTrackId;

        private String orderId;

        private String transactionReferenceId;

        private String redirectionURL;

        private String acceptURL;

        private String declineURL;

        private String gateway;

        private Long operationId;

        private String operationDate;

        private BigDecimal feesAmount;

        private String externalReference;

        /**
         * Identifier of the card token created when {@code keepAlive} is requested.
         * May be {@code null} when the card was not tokenized.
         */
        private Long tokenizedCardId;
    }
}
