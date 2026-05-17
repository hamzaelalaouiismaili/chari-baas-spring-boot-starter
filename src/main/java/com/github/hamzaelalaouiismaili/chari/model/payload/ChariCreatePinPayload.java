package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating a customer PIN.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCreatePinPayload {

    private String phoneNumber;

    private String pin;
}
