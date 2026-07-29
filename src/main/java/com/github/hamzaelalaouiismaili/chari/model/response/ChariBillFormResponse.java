package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariBillFormFieldType;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dynamic identification form that must be rendered for a creditor service,
 * as returned by {@code GET /api/bills/form} under a {@code data.collection}
 * envelope.
 *
 * <p>Render one input per field in {@link #getFields()}; validate the
 * customer's answers with
 * {@link com.github.hamzaelalaouiismaili.chari.util.ChariBillFormValidator}
 * before calling the unpaid-items endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillFormResponse {

    @JsonProperty("data")
    private Form data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Form {
        private List<IdentificationField> collection;
        private Integer count;
    }

    /** Form fields to render, never {@code null}. */
    @JsonIgnore
    public List<IdentificationField> getFields() {
        return data == null || data.getCollection() == null
                ? Collections.emptyList() : data.getCollection();
    }

    /** Field matching the given technical name ({@code nomChamp}), or {@code null}. */
    @JsonIgnore
    public IdentificationField findField(String nomChamp) {
        if (nomChamp == null) {
            return null;
        }
        return getFields().stream()
                .filter(field -> nomChamp.equals(field.getNomChamp()))
                .findFirst()
                .orElse(null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdentificationField {
        /** Human-readable label to display to the customer. */
        private String libelle;

        /** Technical field name to send back as {@code nomChamp}. */
        private String nomChamp;

        /** Raw UI type: {@code text}, {@code select}, {@code password}, {@code libelle}. */
        private String typeChamp;

        /** Allowed values when the field is a {@code select}. */
        private List<String> listVals;

        private String formatChamp;
        private Integer tailleMin;
        private Integer tailleMax;

        /** {@code "1"} when the field is mandatory. */
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
        public boolean isSelect() {
            return getTypedFieldType() == ChariBillFormFieldType.SELECT;
        }

        /** Allowed values for a select field, never {@code null}. */
        @JsonIgnore
        public List<String> getAllowedValues() {
            return listVals == null ? Collections.emptyList() : listVals;
        }

        @JsonIgnore
        public boolean shouldSubmit() {
            return !getTypedFieldType().isDisplayOnly();
        }
    }
}
