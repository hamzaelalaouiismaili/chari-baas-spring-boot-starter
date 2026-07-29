package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Confirmation of a Fatourati bill payment as returned by
 * {@code POST /api/bills/confirm} under a {@code data} envelope.
 *
 * <p>{@code operationId} identifies the wallet operation and is the value to
 * pass to the bill-receipt endpoint. {@code fatouratiErrorCode} carries the
 * Fatourati business outcome ({@code 000} = success).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillPaymentResponse {

    @JsonProperty("data")
    private Confirmation data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Confirmation {
        private Integer operationType;

        /** Wallet operation identifier; use it to download the bill receipt. */
        private Long operationId;

        private BigDecimal amount;
        private BigDecimal feesAmount;
        private BigDecimal totalAmount;
        private String reason;
        private String checkedAt;

        /** Creditor display name, e.g. {@code "Orange Recharges et Catalogue Pass"}. */
        private String creditor;

        /** Receivable display name, e.g. {@code "Orange recharge Sim"}. */
        private String debt;

        private String categoryCode;
        private String category;
        private String authorizationCode;

        /** Fatourati business result code; {@code 000} means success. */
        private String fatouratiErrorCode;
    }

    @JsonIgnore
    public Long getOperationId() {
        return data == null ? null : data.getOperationId();
    }

    @JsonIgnore
    public String getAuthorizationCode() {
        return data == null ? null : data.getAuthorizationCode();
    }

    @JsonIgnore
    public String getFatouratiErrorCode() {
        return data == null ? null : data.getFatouratiErrorCode();
    }

    @JsonIgnore
    public boolean hasCode(String code) {
        return code != null && code.equals(getFatouratiErrorCode());
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return hasCode("000");
    }

    /** Code 301 means Fatourati already processed the payment and is receipt-safe. */
    @JsonIgnore
    public boolean isAlreadyProcessed() {
        return hasCode("301");
    }

    @JsonIgnore
    public boolean isReceiptAvailable() {
        return isSuccessful() || isAlreadyProcessed();
    }

    /** Digital-channel codes whose final state must be resolved by webhook. */
    @JsonIgnore
    public boolean isAwaitingWebhookResolution() {
        return hasCode("908") || hasCode("909") || hasCode("910");
    }
}
