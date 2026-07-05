package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Selected articles and open Fatourati transaction used to confirm payment. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillPaymentPayload {
    private String creancierId;
    private String creanceId;
    private String refTxFatourati;

    @JsonProperty("CreancierVals")
    private List<ChariBillFieldValue> creditorValues;

    @JsonProperty("ListeArticleSelectionnes")
    private List<ChariBillArticle> selectedArticles;
}
