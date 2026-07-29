package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Open Fatourati transaction and its unpaid selectable articles, as returned
 * by {@code POST /api/bills/impayes} under a {@code data} envelope.
 *
 * <p>Top-level getters delegate into the envelope so callers can use
 * {@code response.getRefTxFatourati()} directly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillUnpaidItemsResponse {

    @JsonProperty("data")
    private UnpaidItems data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnpaidItems {
        /** Reference of the open Fatourati transaction; echo it back on confirm. */
        private String refTxFatourati;
        private Integer nbreCreances;

        /** Total amount of all unpaid articles, tax included. */
        @JsonAlias({"montantTotalTTC", "montantTotalTTCField"})
        private BigDecimal montantTotalTTC;

        private String typeFrais;
        private BigDecimal valeurFrais;
        private BigDecimal seuilMinimal;
        private String codeDevise;

        /** Customer/technical parameters; echo ALL of them back on confirm. */
        private List<ChariBillFieldValue> globalParams;

        /** Unpaid articles the customer can select for payment. */
        private List<ChariBillArticle> impayesParams;
    }

    @JsonIgnore
    public String getRefTxFatourati() {
        return data == null ? null : data.getRefTxFatourati();
    }

    @JsonIgnore
    public Integer getNbreCreances() {
        return data == null ? null : data.getNbreCreances();
    }

    @JsonIgnore
    public BigDecimal getMontantTotalTTC() {
        return data == null ? null : data.getMontantTotalTTC();
    }

    @JsonIgnore
    public List<ChariBillFieldValue> getGlobalParams() {
        return data == null || data.getGlobalParams() == null
                ? Collections.emptyList() : data.getGlobalParams();
    }

    /** Unpaid articles the customer can select for payment. */
    @JsonIgnore
    public List<ChariBillArticle> getImpayesParams() {
        return data == null || data.getImpayesParams() == null
                ? Collections.emptyList() : data.getImpayesParams();
    }

    /** Alias of {@link #getImpayesParams()} with a developer-friendly name. */
    @JsonIgnore
    public List<ChariBillArticle> getArticles() {
        return getImpayesParams();
    }

    /** Article matching the given identifier, or {@code null}. */
    @JsonIgnore
    public ChariBillArticle findArticle(String idArticle) {
        if (idArticle == null) {
            return null;
        }
        return getImpayesParams().stream()
                .filter(article -> idArticle.equals(article.getIdArticle()))
                .findFirst()
                .orElse(null);
    }

    /** Technical parameters have an empty label and must not be displayed. */
    @JsonIgnore
    public List<ChariBillFieldValue> getDisplayableGlobalParams() {
        return getGlobalParams().stream()
                .filter(param -> param.getLibelle() != null && !param.getLibelle().isBlank())
                .toList();
    }
}
