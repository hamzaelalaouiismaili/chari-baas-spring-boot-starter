package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Open Fatourati transaction and its unpaid selectable articles. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillUnpaidItemsResponse extends ChariFatouratiResponse {
    private String refTxFatourati;
    private String codeDevise;
    private Integer nbreCreances;
    private BigDecimal montantTotalTTC;
    private String typeFrais;
    private BigDecimal valeurFrais;
    private BigDecimal seuilMinimal;
    private List<ChariBillFieldValue> globalParams;
    private List<ChariBillArticle> impayesParams;

    /** Technical parameters have an empty label and must not be displayed. */
    @JsonIgnore
    public List<ChariBillFieldValue> getDisplayableGlobalParams() {
        if (globalParams == null) {
            return Collections.emptyList();
        }
        return globalParams.stream()
                .filter(param -> param.getLibelle() != null && !param.getLibelle().isBlank())
                .toList();
    }
}
