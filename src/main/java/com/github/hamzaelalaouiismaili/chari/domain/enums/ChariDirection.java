package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariDirection {
    CREDIT(1, "Incoming funds (money received)"),
    DEBIT(2, "Outgoing funds (money sent)"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariDirection fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariDirection direction : values()) {
            if (direction.code == code) {
                return direction;
            }
        }
        return UNKNOWN;
    }
}
