package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardDeliveryStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariIssuedCardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Filters for issued cards belonging to the partner. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariManagedCardsQuery {
    private Integer pageSize;
    private Integer pageNumber;
    private String phoneNumber;
    private Long cardProgramId;
    private ChariCardDeliveryStatus deliveryStatus;
    private ChariIssuedCardStatus cardStatus;
}
