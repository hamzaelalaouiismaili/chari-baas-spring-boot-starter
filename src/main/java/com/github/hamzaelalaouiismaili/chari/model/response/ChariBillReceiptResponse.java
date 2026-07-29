package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Receipt of a settled bill payment as returned by
 * {@code GET /api/bills/bill-receipt/{operationId}}.
 *
 * <p>The receipt content is creditor-dependent, so it is exposed as a
 * key/value map with typed convenience accessors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariBillReceiptResponse {

    @JsonProperty("data")
    private Map<String, Object> data = new LinkedHashMap<>();

    /** Receipt fields, never {@code null}. */
    @JsonIgnore
    public Map<String, Object> getFields() {
        return data == null ? Collections.emptyMap() : data;
    }

    /** Value of the given receipt field as text, or {@code null}. */
    @JsonIgnore
    public String getString(String key) {
        Object value = getFields().get(key);
        return value == null ? null : String.valueOf(value);
    }
}
