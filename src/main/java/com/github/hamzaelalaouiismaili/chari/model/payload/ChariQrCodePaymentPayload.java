package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for QR code payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariQrCodePaymentPayload {

    private String customerPhoneNumber;
    private String qrCodeContent;
    private BigDecimal amount;
    private String idempotencyKey;
}
