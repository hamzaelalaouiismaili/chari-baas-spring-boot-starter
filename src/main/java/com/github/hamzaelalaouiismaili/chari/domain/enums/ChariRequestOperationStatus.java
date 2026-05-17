package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariRequestOperationStatus {
    OPEN(1, "Pending execution by agent"),
    COMPLETED(2, "Successfully executed"),
    FAILED(3, "Failed"),
    CANCELED(4, "Canceled"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariRequestOperationStatus fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariRequestOperationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
