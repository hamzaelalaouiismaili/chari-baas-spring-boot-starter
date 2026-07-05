package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Article types returned by Fatourati unpaid-items lookup. */
@Getter
@RequiredArgsConstructor
public enum ChariBillArticleType {
    RECEIVABLE(0),
    FEE(1),
    MANDATORY(2),
    STAMP_FEE(3),
    UNKNOWN(-1);

    @JsonValue
    private final int code;

    @JsonCreator
    public static ChariBillArticleType fromCode(Integer code) {
        if (code != null) {
            for (ChariBillArticleType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }
}
