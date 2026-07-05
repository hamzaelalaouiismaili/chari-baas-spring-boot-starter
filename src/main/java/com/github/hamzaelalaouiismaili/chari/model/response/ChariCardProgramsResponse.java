package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardType;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated card programs available to the partner. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardProgramsResponse {
    private CardProgramsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardProgramsData {
        private List<CardProgram> collection;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardProgram {
        private Long cardProgramId;
        private String programName;
        private String binRange;
        private Integer cardTypeId;
        private String cardTypeName;
        private Integer cardSchemeId;
        private String cardSchemeName;
        private String currencyCode;
        private Integer validityYears;
        private Integer renewalDaysBefore;
        private Integer pinTriesLimit;
        private Boolean isActive;
        private Boolean allowAtm;
        private Boolean allowOnline;
        private Boolean allowPos;
        private Boolean allowInternational;
        private Boolean contactlessEnabled;
        private String createdAt;
        private Integer partnerId;
        private Integer totalCards;
        private Integer activeCards;
        private BigDecimal price;

        @JsonIgnore
        public ChariCardType getTypedCardType() {
            return ChariCardType.fromCode(cardTypeId);
        }
    }
}
