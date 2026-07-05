package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Shared payload for voucher purchase preview and confirmation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherPurchasePayload {

    private String customerPhoneNumber;
    private String destinationPhoneNumber;
    private String beneficiaryName;
    private String providerSkuId;
    private Integer providerId;
}
