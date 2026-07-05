package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result of an executed Telco top-up. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariTelcoRechargeResponse {

    private TelcoRechargeData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelcoRechargeData {

        private Integer operationType;

        @JsonAlias("Amount")
        private BigDecimal amount;

        private BigDecimal feesAmount;
        private String checkedAt;
        private Boolean openLoop;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(operationType);
        }
    }
}
