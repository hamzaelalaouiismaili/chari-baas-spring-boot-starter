package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for cashin operation using a saved card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariSavedCardCashinPayload {

    private String cvv;
    private BigDecimal amount;
    private String acceptUrl;
    private String declineUrl;

    /**
     * Optional 3-D Secure flag (sent as "3dSecure").
     */
    private Boolean threeDSecure;

    /**
     * Optional fee percentage applied to the operation.
     */
    private BigDecimal feesPercent;

    /**
     * Optional fee percentage applied to international cards.
     */
    private BigDecimal internationalFeesPercent;

    /**
     * Optional flag to capture the authorisation immediately.
     */
    private Boolean autoCapture;

    /**
     * Optional flag allowing international cards.
     */
    private Boolean allowInternationalCards;

    /**
     * Optional server-to-server notification URL.
     */
    private String notificationUrl;

    /**
     * Optional merchant-side reference echoed back by Chari.
     */
    private String externalReference;

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

    public static class ChariSavedCardCashinPayloadBuilder {

        public ChariSavedCardCashinPayloadBuilder acceptURL(String acceptURL) {
            this.acceptUrl = acceptURL;
            return this;
        }

        public ChariSavedCardCashinPayloadBuilder declineURL(String declineURL) {
            this.declineUrl = declineURL;
            return this;
        }
    }
}
