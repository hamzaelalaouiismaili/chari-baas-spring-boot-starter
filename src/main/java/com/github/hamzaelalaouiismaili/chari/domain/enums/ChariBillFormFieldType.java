package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** UI field types returned by the dynamic Fatourati identification form. */
public enum ChariBillFormFieldType {
    TEXT("text"),
    SELECT("select"),
    PASSWORD("password"),
    LABEL("libelle"),
    UNKNOWN("unknown");

    private final String value;

    ChariBillFormFieldType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ChariBillFormFieldType fromValue(String value) {
        if (value != null) {
            for (ChariBillFormFieldType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }

    public boolean isDisplayOnly() {
        return this == LABEL;
    }
}
