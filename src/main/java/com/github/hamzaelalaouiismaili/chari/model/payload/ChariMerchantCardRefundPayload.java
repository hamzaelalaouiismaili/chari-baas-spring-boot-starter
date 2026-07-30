package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for refunding a captured merchant card payment.
 * A refundAmount below the captured amount performs a partial refund.
 * POST /api/operations/merchant/payment/card/refund
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariMerchantCardRefundPayload {

    private String phoneNumber;

    private Long operationId;

    private BigDecimal refundAmount;

    private String orderId;

    private String transactionTrackId;
}
