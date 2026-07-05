package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariBillFormFieldType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Dynamic identification form that must be rendered for a creditor service. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillFormResponse extends ChariFatouratiResponse {
    private Integer nbreParams;
    private List<IdentificationField> creancierParams;
    private String refTxFatourati;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentificationField {
        private String libelle;
        private String nomChamp;
        private String typeChamp;
        private List<Object> listVals;
        private String formatChamp;
        private Integer tailleMin;
        private Integer tailleMax;
        private String contrainte;

        @JsonIgnore
        public ChariBillFormFieldType getTypedFieldType() {
            return ChariBillFormFieldType.fromValue(typeChamp);
        }

        @JsonIgnore
        public boolean isRequired() {
            return "1".equals(contrainte);
        }

        @JsonIgnore
        public boolean shouldSubmit() {
            return !getTypedFieldType().isDisplayOnly();
        }
    }
}
