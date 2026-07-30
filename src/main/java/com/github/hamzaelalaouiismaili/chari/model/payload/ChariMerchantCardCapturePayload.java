package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for capturing or reversing an authorized merchant card payment.
 * Shared by:
 * POST /api/operations/merchant/payment/card/capture
 * POST /api/operations/merchant/payment/card/reverse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariMerchantCardCapturePayload {

    private String phoneNumber;

    private BigDecimal amount;

    private String orderId;

    private String transactionTrackId;

    private Boolean skipGatewayCall;
}
