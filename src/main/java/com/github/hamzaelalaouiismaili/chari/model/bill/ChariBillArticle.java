package com.github.hamzaelalaouiismaili.chari.model.bill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariBillArticleType;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A payable Fatourati article returned by the unpaid-items operation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillArticle {
    private String idArticle;
    private String description;
    private String dateFacture;
    private BigDecimal prixTTC;
    private Integer typeArticle;
    private List<ChariBillFieldValue> extraArticleParams;

    @JsonIgnore
    public ChariBillArticleType getTypedArticleType() {
        return ChariBillArticleType.fromCode(typeArticle);
    }
}
