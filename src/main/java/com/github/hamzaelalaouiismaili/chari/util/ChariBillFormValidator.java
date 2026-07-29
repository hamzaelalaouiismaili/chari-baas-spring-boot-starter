package com.github.hamzaelalaouiismaili.chari.util;

import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillFormResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates customer answers against the dynamic Fatourati identification
 * form BEFORE calling the unpaid-items endpoint, so integrators can surface
 * precise input errors without a network round-trip.
 *
 * <p>Checks performed per submittable form field:
 * <ul>
 *   <li>required fields ({@code contrainte = "1"}) have a non-blank value;</li>
 *   <li>value length respects {@code tailleMin}/{@code tailleMax};</li>
 *   <li>select fields only accept one of their {@code listVals};</li>
 *   <li>submitted names all belong to the form (no unknown {@code nomChamp}).</li>
 * </ul>
 */
public final class ChariBillFormValidator {

    private ChariBillFormValidator() {
    }

    /** Returns all validation problems; empty when the values are valid. */
    public static List<String> check(ChariBillFormResponse form, List<ChariBillFieldValue> values) {
        List<String> problems = new ArrayList<>();
        if (form == null) {
            problems.add("Identification form is required");
            return problems;
        }
        Map<String, String> valueByName = new HashMap<>();
        if (values != null) {
            for (ChariBillFieldValue value : values) {
                if (value == null || value.getNomChamp() == null || value.getNomChamp().isBlank()) {
                    problems.add("Each submitted value requires a nomChamp");
                    continue;
                }
                valueByName.put(value.getNomChamp(), value.getValeurChamp());
            }
        }

        for (ChariBillFormResponse.IdentificationField field : form.getFields()) {
            if (!field.shouldSubmit()) {
                continue;
            }
            String name = field.getNomChamp();
            String label = field.getLibelle() != null ? field.getLibelle() : name;
            String value = valueByName.remove(name);
            boolean blank = value == null || value.isBlank();

            if (blank) {
                if (field.isRequired()) {
                    problems.add("Field '" + label + "' (" + name + ") is required");
                }
                continue;
            }
            Integer min = field.getTailleMin();
            Integer max = field.getTailleMax();
            if (min != null && min > 0 && value.length() < min) {
                problems.add("Field '" + label + "' (" + name + ") must contain at least "
                        + min + " characters");
            }
            if (max != null && max > 0 && value.length() > max) {
                problems.add("Field '" + label + "' (" + name + ") must contain at most "
                        + max + " characters");
            }
            if (field.isSelect() && !field.getAllowedValues().isEmpty()
                    && !field.getAllowedValues().contains(value)) {
                problems.add("Field '" + label + "' (" + name + ") must be one of "
                        + field.getAllowedValues());
            }
        }

        for (String unknown : valueByName.keySet()) {
            problems.add("Field '" + unknown + "' is not part of the identification form");
        }
        return problems;
    }

    /** Throws {@link IllegalArgumentException} listing every problem when invalid. */
    public static void validate(ChariBillFormResponse form, List<ChariBillFieldValue> values) {
        List<String> problems = check(form, values);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid bill identification values: " + String.join("; ", problems));
        }
    }
}
