package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChariDocumentType {
    IdentityCard(1, "National identity card"),
    DrivingLicense(2, "Driving license"),
    Passport(3, "Passport"),
    ResidencePermit(4, "Residence permit"),
    ProofOfIncome(5, "Proof of income"),
    ProofOfResidence(6, "Proof of residence"),
    Selfie(7, "Selfie / Face photo"),
    CommercialRegister(8, "Commercial register"),
    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String description;

    @JsonCreator
    public static ChariDocumentType fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariDocumentType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public boolean requiresBackImage() {
        return this == IdentityCard || this == DrivingLicense || this == ResidencePermit;
    }
}
