package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for updating an existing customer PIN.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariUpdatePinPayload {

    private String phoneNumber;

    private String oldPin;

    private String newPin;
}
