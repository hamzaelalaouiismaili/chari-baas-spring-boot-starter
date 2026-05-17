package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for customer PIN login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariLoginWithPinPayload {

    private String phoneNumber;

    private String pin;
}
