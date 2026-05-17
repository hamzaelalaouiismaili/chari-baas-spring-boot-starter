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
