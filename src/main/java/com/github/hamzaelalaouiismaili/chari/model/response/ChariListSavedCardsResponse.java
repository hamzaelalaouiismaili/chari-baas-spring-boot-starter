package com.github.hamzaelalaouiismaili.chari.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for listing saved cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariListSavedCardsResponse {

    private ListSavedCardsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListSavedCardsData {
        private List<ChariSavedCard> collection;
        private Integer count;
    }
}
