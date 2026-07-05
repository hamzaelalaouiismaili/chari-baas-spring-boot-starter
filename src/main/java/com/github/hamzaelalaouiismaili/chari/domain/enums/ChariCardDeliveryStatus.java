package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Physical card delivery and personalization status. */
@Getter
@RequiredArgsConstructor
public enum ChariCardDeliveryStatus {
    PENDING(1),
    SENT_TO_PERSONALIZATION(2),
    READY_FOR_DELIVERY(3),
    DELIVERED(4),
    UNKNOWN(-1);

    @JsonValue
    private final int code;

    @JsonCreator
    public static ChariCardDeliveryStatus fromCode(Integer code) {
        if (code != null) {
            for (ChariCardDeliveryStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }
}
