package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for card cash-in (funding) preview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariCardFundingPreviewResponse {

    private CardFundingPreviewData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CardFundingPreviewData {

        private Integer type;

        private CardFundingPreviewOperation operation;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private Boolean openLoop;

        private String checkedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CardFundingPreviewOperation {

        private String code;

        private String phoneNumber;

        private BigDecimal amount;

        private Integer method;

        private Integer acceptedBy;

        private String description;
    }
}
