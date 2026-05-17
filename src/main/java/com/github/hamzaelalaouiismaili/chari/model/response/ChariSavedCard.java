package com.github.hamzaelalaouiismaili.chari.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a saved card from Chari.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariSavedCard {

    private Integer customerBankCardId;
    private String maskedPan;
    private String cardExpiryDate;
    private String cardName;
    private String cardholderFirstname;
    private String cardholderLastname;
    private Boolean requiredCvv;
    private String issuer;
    private String scheme;
    private String createdAt;
}
