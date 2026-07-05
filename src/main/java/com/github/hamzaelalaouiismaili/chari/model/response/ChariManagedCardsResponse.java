package com.github.hamzaelalaouiismaili.chari.model.response;

import com.github.hamzaelalaouiismaili.chari.model.card.ChariManagedCard;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated issued-card response. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariManagedCardsResponse {
    private ManagedCardsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManagedCardsData {
        private List<ChariManagedCard> collection;
        private Integer count;
    }
}
