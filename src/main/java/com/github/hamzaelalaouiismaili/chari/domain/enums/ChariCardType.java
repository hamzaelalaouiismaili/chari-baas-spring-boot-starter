package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Card form factors supported by card programs. */
public enum ChariCardType {
    PHYSICAL(1),
    VIRTUAL(2),
    DIGITAL(3),
    UNKNOWN(-1);

    private final int code;

    ChariCardType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static ChariCardType fromValue(String value) {
        if (value != null) {
            for (ChariCardType type : values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }

    public static ChariCardType fromCode(Integer code) {
        if (code != null) {
            for (ChariCardType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }
}
