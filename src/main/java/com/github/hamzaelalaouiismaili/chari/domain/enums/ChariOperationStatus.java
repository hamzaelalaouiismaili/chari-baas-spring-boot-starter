package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariOperationStatus {
    OPEN(1, "In progress"),
    COMPLETED(2, "Successfully completed"),
    FAILED(3, "Failed"),
    CANCELED(4, "Canceled"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariOperationStatus fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariOperationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELED;
    }
}
