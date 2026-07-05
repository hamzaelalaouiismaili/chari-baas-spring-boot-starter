package com.github.hamzaelalaouiismaili.chari.model.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardDeliveryStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariIssuedCardStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Issued physical, virtual, or digital card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariManagedCard {
    private Long customerId;
    private Long cardId;
    private Long cardProgramId;
    private String cardProgramName;
    private String binRange;
    private String cardType;
    private String cardScheme;
    private String customerFullName;
    private String cardToken;
    private String maskedPan;
    private Integer cardStatus;
    private Integer deliveryStatus;
    private String issueDate;
    private String activationDate;
    private String expiryDate;
    private String newExpiryDate;
    private String embossedName;
    private Boolean isVirtual;
    private Boolean pinSet;
    private Integer failedPinAttempts;
    private String lockedUntil;
    private String createdAt;
    private Integer partnerId;
    private List<ChariCardLimit> limits;
    private Boolean allowAtm;
    private Boolean allowOnline;
    private Boolean allowInternational;
    private Boolean contactlessEnabled;
    private Boolean allowPos;

    @JsonIgnore
    public ChariCardType getTypedCardType() {
        return ChariCardType.fromValue(cardType);
    }

    @JsonIgnore
    public ChariIssuedCardStatus getTypedCardStatus() {
        return ChariIssuedCardStatus.fromCode(cardStatus);
    }

    @JsonIgnore
    public ChariCardDeliveryStatus getTypedDeliveryStatus() {
        return ChariCardDeliveryStatus.fromCode(deliveryStatus);
    }
}
