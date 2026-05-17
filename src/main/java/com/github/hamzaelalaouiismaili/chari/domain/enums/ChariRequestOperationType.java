package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariRequestOperationType {
    CASHIN(1, "CashIn request"),
    CASHOUT(2, "CashOut request"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariRequestOperationType fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariRequestOperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
