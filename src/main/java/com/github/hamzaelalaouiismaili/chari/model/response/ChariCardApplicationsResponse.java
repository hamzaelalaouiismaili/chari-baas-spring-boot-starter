package com.github.hamzaelalaouiismaili.chari.model.response;

import com.github.hamzaelalaouiismaili.chari.model.card.ChariCardApplication;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated card application response. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardApplicationsResponse {
    private CardApplicationsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardApplicationsData {
        private List<ChariCardApplication> collection;
        private Integer count;
    }
}
