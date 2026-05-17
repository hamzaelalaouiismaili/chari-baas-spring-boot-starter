package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload for cash-out by reference request.
 * POST /api/operations/cashout/request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCashoutByReferencePayload {

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    private BigDecimal amount;
}
