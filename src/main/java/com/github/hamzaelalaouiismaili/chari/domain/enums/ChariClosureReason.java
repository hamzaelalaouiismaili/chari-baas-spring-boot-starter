package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariClosureReason {
    EDP_UNSPECIFIED(1, "Closure by EDP - Unspecified reason"),
    EDP_SUSPICION_OF_FRAUD(2, "Closure by EDP - Suspicion of fraud"),
    CLIENT_CONTRACT_CLOSURE(3, "Closure by client - Contract closure"),
    CLIENT_LOST_OR_STOLEN_PHONE(4, "Closure by client - Lost or stolen phone"),
    CLIENT_UNSPECIFIED(5, "Closure by client - Unspecified reason"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariClosureReason fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariClosureReason reason : values()) {
            if (reason.code == code) {
                return reason;
            }
        }
        return UNKNOWN;
    }
}
