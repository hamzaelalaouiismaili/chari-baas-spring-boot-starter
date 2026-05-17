package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for bank transfer from wallet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariBankTransferPayload {

    private String customerPhoneNumber;
    private BigDecimal amount;
    private String reason;
    private String rib;

    @JsonProperty("beneficiaryName")
    private String beneficiaryName;

    @JsonProperty("beneficiaryId")
    private String beneficiaryId;

    @JsonProperty("ForceCustomerName")
    private Boolean forceCustomerName;

    @JsonProperty("OriginatorName")
    private String originatorName;

    private String idempotencyKey;
}
