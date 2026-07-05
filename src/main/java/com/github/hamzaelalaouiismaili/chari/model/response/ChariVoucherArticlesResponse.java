package com.github.hamzaelalaouiismaili.chari.model.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Paginated voucher article catalog. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherArticlesResponse {

    private VoucherArticlesData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherArticlesData {

        private List<VoucherArticle> collection;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherArticle {

        private String providerSkuId;
        private String productName;
        private String imageUrl;
        private BigDecimal price;
        private String description;
        private Integer providerId;
        private Integer brandId;
    }
}
