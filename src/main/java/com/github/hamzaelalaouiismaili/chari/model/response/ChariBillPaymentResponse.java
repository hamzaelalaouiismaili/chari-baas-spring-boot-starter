package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Fatourati payment confirmation and receipt information. */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChariBillPaymentResponse extends ChariFatouratiResponse {
    private String refTxFatourati;
    private String refReglement;
    private String codeDevise;
    private BigDecimal montantTotalTTC;
    private List<ChariBillFieldValue> params;

    @JsonIgnore
    private final Map<String, Object> additionalFields = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalField(String name, Object value) {
        additionalFields.put(name, value);
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
