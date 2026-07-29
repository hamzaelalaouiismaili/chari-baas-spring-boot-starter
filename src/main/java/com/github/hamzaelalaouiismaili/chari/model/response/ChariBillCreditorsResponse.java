package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Active Fatourati creditors available to the configured partner, grouped by
 * category as returned by {@code GET /api/bills/creanciers}.
 *
 * <p>Each creditor already carries its receivables ({@code listeCreances}), so a
 * separate {@code /api/bills/creances} call is only needed to refresh them.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillCreditorsResponse {

    /** Categories in display order, as sent under the {@code data} envelope. */
    @JsonProperty("data")
    private List<CreditorCategory> categories;

    /** All creditors across every category, flattened in category order. */
    @JsonIgnore
    public List<Creditor> getAllCreditors() {
        List<Creditor> creditors = new ArrayList<>();
        if (categories != null) {
            for (CreditorCategory category : categories) {
                if (category != null && category.getCreditors() != null) {
                    creditors.addAll(category.getCreditors());
                }
            }
        }
        return creditors;
    }

    /** First creditor matching the given four-digit code, or {@code null}. */
    @JsonIgnore
    public Creditor findCreditor(String codeCreancier) {
        for (Creditor creditor : getAllCreditors()) {
            if (creditor.getCodeCreancier() != null
                    && creditor.getCodeCreancier().equals(codeCreancier)) {
                return creditor;
            }
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditorCategory {
        private String categoryCode;
        private String categoryName;
        private String categoryDescription;
        private Integer order;

        @JsonProperty("listeCreanciersListe")
        private List<Creditor> creditors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creditor {
        private String codeCreancier;
        private String nomCreancier;
        private String descriptionCreancier;
        private String logoPath;
        private String siteWeb;

        @JsonProperty("listeCreances")
        private List<ChariBillReceivablesResponse.Receivable> receivables;

        private List<ChariBillFieldValue> params;
    }
}
