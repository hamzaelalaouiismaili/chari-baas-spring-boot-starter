package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.domain.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for confirming a customer with OTP.
 * The Chari API expects the field name "code" (not "otp").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariCustomerConfirmPayload {

    private String phoneNumber;

    @JsonProperty("code")
    private String code;

    private String walletType;

    private Boolean autoActivate;

    public static class ChariCustomerConfirmPayloadBuilder {

        public ChariCustomerConfirmPayloadBuilder walletType(String walletType) {
            this.walletType = walletType;
            return this;
        }

        public ChariCustomerConfirmPayloadBuilder walletType(WalletType walletType) {
            this.walletType = walletType == null ? null : walletType.name();
            return this;
        }
    }

}
