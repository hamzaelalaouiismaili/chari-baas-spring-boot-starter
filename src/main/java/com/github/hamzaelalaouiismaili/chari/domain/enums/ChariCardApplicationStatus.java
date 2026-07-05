package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Card application lifecycle statuses. */
@Getter
@RequiredArgsConstructor
public enum ChariCardApplicationStatus {
    PENDING(1),
    VALIDATED(2),
    REJECTED(3),
    UNKNOWN(-1);

    @JsonValue
    private final int code;

    @JsonCreator
    public static ChariCardApplicationStatus fromCode(Integer code) {
        if (code != null) {
            for (ChariCardApplicationStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }
}
