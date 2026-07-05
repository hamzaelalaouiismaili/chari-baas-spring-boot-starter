package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Recharge modes supported by the Chari Telco API. */
@Getter
@RequiredArgsConstructor
public enum ChariTelcoRechargeType {

    CLASSIC(0),
    PRODUCT(1);

    @JsonValue
    private final int code;

    @JsonCreator
    public static ChariTelcoRechargeType fromCode(Integer code) {
        if (code != null) {
            for (ChariTelcoRechargeType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported Telco recharge type code: " + code);
    }
}
