package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for saving a card to customer's account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariSaveCardPayload {

    private String firstName;
    private String lastName;
    private String pan;
    private String expiryDate; // YYMM format
    private String cvv;
    private String cardName;
}
