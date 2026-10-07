package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariGatewayTransactionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for GET /api/operations/gateway/state, used to reconcile a card
 * operation with Chari by its orderId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariGatewayStateResponse {

    private GatewayStateData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GatewayStateData {

        private String orderId;

        private String reference;

        private String createdAt;

        private Long operationId;

        private Boolean validated;

        private BigDecimal feesPercent;

        /** Raw gateway state, e.g. AUTHORIZED or CAPTURED. */
        private String gatewayTransactionState;

        private String gateway;

        @JsonIgnore
        public ChariGatewayTransactionState getTypedGatewayTransactionState() {
            return ChariGatewayTransactionState.fromValue(gatewayTransactionState);
        }

        /** Chari only reports a gateway state once the operation has completed on its side. */
        @JsonIgnore
        public boolean isCompleted() {
            return gatewayTransactionState != null && !gatewayTransactionState.isBlank();
        }

        /** True once the funds have been captured (not just authorized). */
        @JsonIgnore
        public boolean isCaptured() {
            return getTypedGatewayTransactionState() == ChariGatewayTransactionState.CAPTURED;
        }
    }
}
