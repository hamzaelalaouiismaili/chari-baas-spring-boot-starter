package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for the dedicated Fatourati CashIn request (FATREF- reference flow).
 * For a principal agent, {@code code} replaces the phone number.
 * POST /api/operations/fatourati/cashin/request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariFatouratiCashinRequestPayload {

    private String code;

    private BigDecimal amount;

    private BigDecimal feesPercent;

    private String description;
}
