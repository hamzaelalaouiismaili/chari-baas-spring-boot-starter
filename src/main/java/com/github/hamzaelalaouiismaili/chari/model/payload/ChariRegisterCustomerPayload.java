package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hamzaelalaouiismaili.chari.domain.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for registering a new customer with Chari.
 * Uses NON_NULL to avoid sending null fields (e.g. companyName, companyIce)
 * which are rejected by the Chari API for personal (type "P") accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariRegisterCustomerPayload {

    private String phoneNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String cin;
    private String walletType; // "P" particular, "C" merchant
    private Boolean closeLoopOnly;

    public static class ChariRegisterCustomerPayloadBuilder {

        public ChariRegisterCustomerPayloadBuilder walletType(String walletType) {
            this.walletType = walletType;
            return this;
        }

        public ChariRegisterCustomerPayloadBuilder walletType(WalletType walletType) {
            this.walletType = walletType == null ? null : walletType.name();
            return this;
        }
    }
}
