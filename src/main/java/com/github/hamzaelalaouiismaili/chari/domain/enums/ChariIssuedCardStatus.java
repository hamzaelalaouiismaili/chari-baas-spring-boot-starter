package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Status of an issued card. */
@Getter
@RequiredArgsConstructor
public enum ChariIssuedCardStatus {
    ISSUED(1),
    ACTIVATED(2),
    BLOCKED(3),
    SUSPENDED(4),
    EXPIRED(5),
    CANCELLED(6),
    UNKNOWN(-1);

    @JsonValue
    private final int code;

    @JsonCreator
    public static ChariIssuedCardStatus fromCode(Integer code) {
        if (code != null) {
            for (ChariIssuedCardStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }
}
