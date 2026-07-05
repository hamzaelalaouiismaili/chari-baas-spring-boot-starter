package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Query parameters for paginated voucher catalog endpoints. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherCatalogQuery {

    private String phoneNumber;
    private Integer brandId;
    private Integer page;
    private Integer take;
}
