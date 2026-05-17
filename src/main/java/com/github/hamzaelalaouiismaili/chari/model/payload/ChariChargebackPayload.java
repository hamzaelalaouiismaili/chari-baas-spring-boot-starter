package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for chargeback preview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariChargebackPayload {

    private String sourcePhoneNumber;

    private String destinationPhoneNumber;

    private BigDecimal amount;

    private String description;

    private Long originalOperationId;
}
