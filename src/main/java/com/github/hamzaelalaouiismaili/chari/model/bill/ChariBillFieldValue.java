package com.github.hamzaelalaouiismaili.chari.model.bill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Name/value field used by Fatourati identification and technical parameters.
 *
 * <p>Chari is inconsistent about the value key: the unpaid-items request reads
 * {@code valChamp} while the confirm request and most responses use
 * {@code valeurChamp}. Both names are therefore emitted on serialization and
 * accepted on deserialization, backed by the single {@link #valeurChamp} field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillFieldValue {
    private String libelle;
    private String nomChamp;

    private String valeurChamp;

    @JsonProperty("valChamp")
    public String getValChamp() {
        return valeurChamp;
    }

    @JsonProperty("valChamp")
    public void setValChamp(String valChamp) {
        this.valeurChamp = valChamp;
    }

    /** Convenience factory for submitting a form answer: {@code of("ND", "0669440735")}. */
    public static ChariBillFieldValue of(String nomChamp, String valeurChamp) {
        return ChariBillFieldValue.builder()
                .nomChamp(nomChamp)
                .valeurChamp(valeurChamp)
                .build();
    }
}
