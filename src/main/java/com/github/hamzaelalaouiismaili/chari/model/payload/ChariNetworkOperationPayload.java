package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for simulating a network CashIn/CashOut by reference in sandbox.
 * POST /api/network/operations/cashin
 * POST /api/network/operations/cashout
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariNetworkOperationPayload {

    private String reference;

    private String entity;
}
