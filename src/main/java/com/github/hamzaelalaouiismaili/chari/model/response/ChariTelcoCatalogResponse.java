package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Available recharge products returned by the Telco catalog endpoint. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariTelcoCatalogResponse {

    private List<TelcoProduct> data;

    /** Returns only products that can currently be purchased. */
    @JsonIgnore
    public List<TelcoProduct> getEnabledProducts() {
        if (data == null) {
            return Collections.emptyList();
        }
        return data.stream().filter(product -> Boolean.TRUE.equals(product.getEnabled())).toList();
    }

    @JsonIgnore
    public Optional<TelcoProduct> findProduct(Integer productCode) {
        if (data == null || productCode == null) {
            return Optional.empty();
        }
        return data.stream().filter(product -> productCode.equals(product.getProductCode())).findFirst();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelcoProduct {

        private Integer productCode;
        private String description;
        private String arDescription;
        private Boolean enabled;
    }
}
