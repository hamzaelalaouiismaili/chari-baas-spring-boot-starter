package com.github.hamzaelalaouiismaili.chari.model.response;

import com.github.hamzaelalaouiismaili.chari.model.card.ChariCardTransaction;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated transactions for an issued card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardTransactionsResponse {
    private CardTransactionsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardTransactionsData {
        private List<ChariCardTransaction> collection;
        private Integer count;
    }
}
