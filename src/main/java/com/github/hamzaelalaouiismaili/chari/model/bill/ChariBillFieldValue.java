package com.github.hamzaelalaouiismaili.chari.model.bill;

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
    private String valeurChamp;
}
