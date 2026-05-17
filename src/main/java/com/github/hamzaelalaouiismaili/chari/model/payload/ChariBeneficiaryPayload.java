package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating or updating a beneficiary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariBeneficiaryPayload {

    private String name;

    private String phoneNumber;

    private String rib;

    private String email;
}
