package com.github.hamzaelalaouiismaili.chari.model.response;

import com.github.hamzaelalaouiismaili.chari.model.card.ChariManagedCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One issued card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariManagedCardResponse {
    private ChariManagedCard data;
}
