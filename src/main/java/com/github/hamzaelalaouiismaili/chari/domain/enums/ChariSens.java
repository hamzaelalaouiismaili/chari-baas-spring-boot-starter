package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariSens {
    CREDIT(1, "Incoming funds"),
    DEBIT(2, "Outgoing funds"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariSens fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariSens sens : values()) {
            if (sens.code == code) {
                return sens;
            }
        }
        return UNKNOWN;
    }
}
