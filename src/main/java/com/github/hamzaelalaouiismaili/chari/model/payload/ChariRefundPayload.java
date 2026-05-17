package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for refund preview and execution.
 * POST /api/operations/refund/preview
 * POST /api/operations/refund
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariRefundPayload {

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    private BigDecimal refundAmount;

    private Long operationId;

    private String orderId;

    private String transactionTrackId;
}
