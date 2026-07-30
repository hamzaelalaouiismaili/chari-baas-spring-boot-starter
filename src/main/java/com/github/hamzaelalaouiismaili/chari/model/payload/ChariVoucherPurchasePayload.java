package com.github.hamzaelalaouiismaili.chari.model.payload;

import java.math.BigDecimal;
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
    private BigDecimal amount;
    private Integer skuId;
    private String providerSkuId;
    private BigDecimal price;
    private Integer providerId;
}
