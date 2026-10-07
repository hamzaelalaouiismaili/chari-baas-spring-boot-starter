package com.github.hamzaelalaouiismaili.chari.domain.enums;

/** Card gateway transaction states returned by GET /api/operations/gateway/state. */
public enum ChariGatewayTransactionState {
    AUTHORIZED,
    CAPTURED,
    UNKNOWN;

    public static ChariGatewayTransactionState fromValue(String value) {
        if (value != null) {
            for (ChariGatewayTransactionState state : values()) {
                if (state.name().equalsIgnoreCase(value.trim())) {
                    return state;
                }
            }
        }
        return UNKNOWN;
    }
}
