package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for merchant payment by phone number.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariMerchantPaymentByPhonePayload {

    private String customerPhoneNumber;
    private BigDecimal amount;
    private String reason;
    private String recipientPhoneNumber;
    private Integer beneficiaryId;
}
