package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for merchant card payment execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariMerchantCardPaymentPayload {

    private String firstName;

    private String lastName;

    private String cvv;

    private BigDecimal amount;

    private String pan;

    private String expiryDate;

    private Boolean keepAlive;

    private String currency;

    private Boolean threeDSecure;

    private BigDecimal feesPercent;

    private Boolean allowInternationalCards;

    private BigDecimal internationalFeesPercent;

    private Boolean autoCapture;

    private String notificationUrl;

    private String acceptUrl;

    private String declineUrl;

    private String cardName;

    private String externalReference;
}
