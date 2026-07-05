package com.github.hamzaelalaouiismaili.chari.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated voucher brand catalog. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherBrandsResponse {

    private VoucherBrandsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherBrandsData {

        private List<ChariVoucherBrandResponse.VoucherBrand> collection;
        private Integer count;
    }
}
