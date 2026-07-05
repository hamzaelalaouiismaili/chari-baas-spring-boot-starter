package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Moroccan mobile operators supported by the Chari Telco API. */
@Getter
@RequiredArgsConstructor
public enum ChariTelcoOperator {

    MAROC_TELECOM(1, "Maroc Telecom", "IAM"),
    ORANGE(2, "Orange", "Orange"),
    INWI(3, "Inwi", "Inwi");

    @JsonValue
    private final int code;
    private final String displayName;
    private final String shortName;

    @JsonCreator
    public static ChariTelcoOperator fromCode(Integer code) {
        if (code != null) {
            for (ChariTelcoOperator operator : values()) {
                if (operator.code == code) {
                    return operator;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported Telco operator code: " + code);
    }
}
