package com.github.hamzaelalaouiismaili.chari.model.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillArticle {
    private String idArticle;
    private String description;
    private String dateFacture;

    /** Chari sends and expects the amount as a JSON string, e.g. {@code "168.00"}. */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal prixTTC;
    private Integer typeArticle;
    private List<ChariBillFieldValue> extraArticleParams;

    @JsonIgnore
    public ChariBillArticleType getTypedArticleType() {
        return ChariBillArticleType.fromCode(typeArticle);
    }
}
