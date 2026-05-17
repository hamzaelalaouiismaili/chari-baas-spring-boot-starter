package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariClosureReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for unregistering a customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariUnregisterCustomerPayload {

    private String phoneNumber;

    private Integer reason;

    public static class ChariUnregisterCustomerPayloadBuilder {

        public ChariUnregisterCustomerPayloadBuilder reason(Integer reason) {
            this.reason = reason;
            return this;
        }

        public ChariUnregisterCustomerPayloadBuilder reason(ChariClosureReason reason) {
            this.reason = reason == null ? null : reason.getCode();
            return this;
        }
    }
}
