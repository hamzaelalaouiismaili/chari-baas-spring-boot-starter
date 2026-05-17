package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for executing a request operation by reference with an agent code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariExecuteRequestOperationByReferencePayload {

    private String code;

    private String reference;
}
