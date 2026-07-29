package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Active service types exposed by one Fatourati creditor, as returned by
 * {@code GET /api/bills/creances} under a {@code data.collection} envelope.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillReceivablesResponse {

    @JsonProperty("data")
    private Receivables data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receivables {
        private List<Receivable> collection;
        private Integer count;
    }

    /** Receivables of the creditor, never {@code null}. */
    @JsonIgnore
    public List<Receivable> getReceivables() {
        return data == null || data.getCollection() == null
                ? Collections.emptyList() : data.getCollection();
    }

    @JsonIgnore
    public int getCount() {
        return data == null || data.getCount() == null ? 0 : data.getCount();
    }

    /** Receivable matching the given two-digit code, or {@code null}. */
    @JsonIgnore
    public Receivable findReceivable(String codeCreance) {
        if (codeCreance == null) {
            return null;
        }
        return getReceivables().stream()
                .filter(receivable -> codeCreance.equals(receivable.getCodeCreance()))
                .findFirst()
                .orElse(null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receivable {
        private String codeCreance;
        private String nomCreance;
    }
}
