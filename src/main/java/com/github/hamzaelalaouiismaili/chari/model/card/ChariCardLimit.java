package com.github.hamzaelalaouiismaili.chari.model.card;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Limit attached to an issued card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardLimit {
    private Long cardLimitId;
    private Long cardId;
    private Integer limitType;
    private BigDecimal amount;
    private Integer period;
    private Boolean isActive;
    private String createdAt;
    private Integer partnerId;
}
