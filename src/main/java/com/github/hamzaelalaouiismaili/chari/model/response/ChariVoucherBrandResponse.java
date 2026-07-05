package com.github.hamzaelalaouiismaili.chari.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A single voucher brand response. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherBrandResponse {

    private VoucherBrand data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherBrand {

        private Integer id;
        private String name;
        private String description;
        private String image;
        private String expirationDelay;
    }
}
