package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for network CashIn/CashOut by reference simulation.
 * POST /api/network/operations/cashin
 * POST /api/network/operations/cashout
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariNetworkOperationResponse {

    private NetworkOperationData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NetworkOperationData {

        private String reference;

        private String entity;

        private String createdAt;

        private String executedAt;

        private String phoneNumber;

        private BigDecimal amount;

        private String description;

        private String partner;
    }
}
