package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariAccountLevel {
    BASIC(1, "Name + valid phone + CIN number", "1 000 MAD"),
    KYC_LEVEL_2(2, "Full KYC (CIN + selfie or document scan via ShareID)", "4 000 MAD"),
    KYC_LEVEL_3(3, "Verified ID (KYC) + interview + digital customer record", "20 000 MAD"),
    KYC_LEVEL_4(4, "Full KYC + interview + digital customer record + proof of income + proof of address",
            "100 000 MAD"),
    MERCHANT(null, "Full KYB + business registration (IF/RC)", "Negotiated"),
    UNKNOWN(-1, "Unknown account level", "Unknown");

    @JsonValue
    private final Integer code;
    private final String kycRequirement;
    private final String balanceLimit;

    @JsonCreator
    public static ChariAccountLevel fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariAccountLevel level : values()) {
            if (code.equals(level.code)) {
                return level;
            }
        }
        return UNKNOWN;
    }
}
