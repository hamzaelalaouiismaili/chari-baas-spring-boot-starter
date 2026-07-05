package com.github.hamzaelalaouiismaili.chari.model.response;

import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Active Fatourati creditors available to the configured partner. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillCreditorsResponse extends ChariFatouratiResponse {
    private Integer nbreCreancier;
    private List<Creditor> listeCreanciers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Creditor {
        private String codeCreancier;
        private String nomCreancier;
        private String descriptionCreancier;
        private String logoPath;
        private String siteWeb;
        private List<ChariBillFieldValue> params;
    }
}
