package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for generating QR code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariGenerateQrCodePayload {

    private BigDecimal amount;
}
