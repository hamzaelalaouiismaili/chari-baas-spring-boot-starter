package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for wallet-to-wallet transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariTransferPayload {

    private String customerPhoneNumber;
    private String recipientPhoneNumber;
    private BigDecimal amount;
    private String reason;
    private Integer beneficiaryId;
    private String idempotencyKey;
}
