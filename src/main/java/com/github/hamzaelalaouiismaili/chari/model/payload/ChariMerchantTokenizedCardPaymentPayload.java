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
}
