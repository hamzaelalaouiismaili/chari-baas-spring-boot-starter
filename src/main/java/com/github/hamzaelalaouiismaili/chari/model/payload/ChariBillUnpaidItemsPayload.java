package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer identification submitted to open a Fatourati transaction and
 * retrieve unpaid bills ({@code POST /api/bills/impayes}).
 *
 * <p>Two lookup modes are supported:
 * <ul>
 *   <li><b>Form values</b> — answers to the dynamic identification form,
 *       built with {@link #forFields(List)}.</li>
 *   <li><b>QR code</b> — the raw content of a scanned bill QR code, built
 *       with {@link #forQrCode(String)}. No form values are needed.</li>
 * </ul>
 *
 * <p>Optionally the bill can be saved to the customer's favorites by setting
 * {@code alias} and {@code addToFavorites}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariBillUnpaidItemsPayload {

    /** Answers to the dynamic identification form fields. */
    @JsonProperty("CreancierVals")
    private List<ChariBillFieldValue> creditorValues;

    /** Raw content of a scanned bill QR code; replaces the form values. */
    @JsonProperty("qrcodecontent")
    private String qrCodeContent;

    /** Display name used when saving this bill to the customer's favorites. */
    @JsonProperty("Alias")
    private String alias;

    /** When {@code true}, saves this bill lookup to the customer's favorites. */
    @JsonProperty("AddToFavorites")
    private Boolean addToFavorites;

    /** Payload for a form-based lookup. */
    public static ChariBillUnpaidItemsPayload forFields(List<ChariBillFieldValue> creditorValues) {
        return ChariBillUnpaidItemsPayload.builder()
                .creditorValues(creditorValues)
                .build();
    }

    /** Payload for a QR-code-based lookup. */
    public static ChariBillUnpaidItemsPayload forQrCode(String qrCodeContent) {
        return ChariBillUnpaidItemsPayload.builder()
                .qrCodeContent(qrCodeContent)
                .build();
    }

    @JsonIgnore
    public boolean isQrCodeLookup() {
        return qrCodeContent != null && !qrCodeContent.isBlank();
    }
}
