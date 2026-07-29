package com.github.hamzaelalaouiismaili.chari.model.bill;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Name/value field used by Fatourati identification and technical parameters. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillFieldValue {
    private String libelle;
    private String nomChamp;

    /** Fatourati sends {@code valChamp} on creditor/params payloads and {@code valeurChamp} elsewhere. */
    @JsonAlias("valChamp")
    private String valeurChamp;

    /** Convenience factory for submitting a form answer: {@code of("ND", "0669440735")}. */
    public static ChariBillFieldValue of(String nomChamp, String valeurChamp) {
        return ChariBillFieldValue.builder()
                .nomChamp(nomChamp)
                .valeurChamp(valeurChamp)
                .build();
    }
}
