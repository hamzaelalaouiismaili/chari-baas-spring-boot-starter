package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillUnpaidItemsResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Selected articles and open Fatourati transaction used to confirm payment
 * ({@code POST /api/bills/confirm}).
 *
 * <p>Build it from the unpaid-items response with
 * {@link #fromUnpaidItems(String, String, ChariBillUnpaidItemsResponse, List)}
 * so the transaction reference and ALL global parameters (including technical
 * ones such as {@code contrPaiement}) are echoed back correctly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariBillPaymentPayload {
    private String creancierId;
    private String creanceId;

    /** Reference of the open transaction returned by the unpaid-items step. */
    private String refTxFatourati;

    /** All global parameters from the unpaid-items response, echoed back verbatim. */
    @JsonProperty("globalParams")
    @JsonAlias("CreancierVals")
    private List<ChariBillFieldValue> globalParams;

    @JsonProperty("ListeArticleSelectionnes")
    private List<ChariBillArticle> selectedArticles;

    /**
     * Builds a confirmation payload from the unpaid-items response, echoing
     * back the transaction reference and every global parameter.
     *
     * @param creancierId      four-digit creditor identifier used on the lookup
     * @param creanceId        two-digit receivable identifier used on the lookup
     * @param unpaidItems      response of the unpaid-items step
     * @param selectedArticles articles the customer chose to pay (subset of
     *                         {@link ChariBillUnpaidItemsResponse#getArticles()})
     */
    public static ChariBillPaymentPayload fromUnpaidItems(
            String creancierId,
            String creanceId,
            ChariBillUnpaidItemsResponse unpaidItems,
            List<ChariBillArticle> selectedArticles) {
        if (unpaidItems == null) {
            throw new IllegalArgumentException("Unpaid-items response is required");
        }
        return ChariBillPaymentPayload.builder()
                .creancierId(creancierId)
                .creanceId(creanceId)
                .refTxFatourati(unpaidItems.getRefTxFatourati())
                .globalParams(unpaidItems.getGlobalParams())
                .selectedArticles(selectedArticles)
                .build();
    }
}
