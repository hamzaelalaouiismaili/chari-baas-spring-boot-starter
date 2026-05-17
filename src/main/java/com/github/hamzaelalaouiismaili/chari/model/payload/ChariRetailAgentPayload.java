package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating a retail agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariRetailAgentPayload {

    private String phoneNumber;
    private String name;
    private String firstName;
    private String lastName;
    private String cin;
    private String email;
    private String address;
}
