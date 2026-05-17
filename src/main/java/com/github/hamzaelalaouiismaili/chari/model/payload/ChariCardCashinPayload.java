package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for card cash-in (funding via card).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardCashinPayload {

    /**
     * Customer's first name.
     */
    private String firstName;

    /**
     * Customer's last name.
     */
    private String lastName;

    /**
     * Card number (PAN).
     */
    private String pan;

    /**
     * Card expiry date in MM/YY format.
     */
    private String expiryDate;

    /**
     * Card CVV/CVC.
     */
    private String cvv;

    /**
     * Amount to fund.
     */
    private BigDecimal amount;

    /**
     * Currency (default MAD).
     */
    @Builder.Default
    private String currency = "MAD";

    /**
     * Keep alive flag for session.
     */
    @Builder.Default
    private Boolean keepAlive = false;

    /**
     * Card holder name (optional).
     */
    private String cardName;

    /**
     * Optional 3DS redirect URL for successful card funding.
     */
    private String acceptUrl;

    /**
     * Optional 3DS redirect URL for declined card funding.
     */
    private String declineUrl;

    /**
     * Idempotency key for preventing duplicate transactions.
     */
    private String idempotencyKey;

    public String getAcceptUrl() {
        return acceptUrl;
    }

    public String getDeclineUrl() {
        return declineUrl;
    }

    public String getAcceptURL() {
        return acceptUrl;
    }

    public String getDeclineURL() {
        return declineUrl;
    }

    public static class ChariCardCashinPayloadBuilder {

        public ChariCardCashinPayloadBuilder acceptURL(String acceptURL) {
            this.acceptUrl = acceptURL;
            return this;
        }

        public ChariCardCashinPayloadBuilder declineURL(String declineURL) {
            this.declineUrl = declineURL;
            return this;
        }
    }
}
