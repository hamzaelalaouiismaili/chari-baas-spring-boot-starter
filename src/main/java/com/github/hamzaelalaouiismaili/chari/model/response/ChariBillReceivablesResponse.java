package com.github.hamzaelalaouiismaili.chari.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Active service types exposed by one Fatourati creditor. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillReceivablesResponse extends ChariFatouratiResponse {
    private Integer nbreCreance;
    private List<Receivable> listeCreance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Receivable {
        private String codeCreance;
        private String nomCreance;
    }
}
