package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for merchant payment using a tokenized card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariMerchantTokenizedCardPaymentPayload {

    private String cvv;

    private BigDecimal amount;

    private BigDecimal feesPercent;

    private BigDecimal internationalFeesPercent;

    private Boolean threeDSecure;

    private Boolean autoCapture;

    private Boolean allowInternationalCards;

    private String acceptUrl;

    private String declineUrl;

    private String notificationUrl;

    private String externalReference;
}
