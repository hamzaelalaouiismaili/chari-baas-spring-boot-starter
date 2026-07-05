package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoOperator;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoRechargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request used to execute a prepaid mobile top-up. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariTelcoRechargePayload {

    private String recipientPhoneNumber;
    private Integer amount;
    private ChariTelcoOperator operator;
    private ChariTelcoRechargeType rechargeType;
    private Integer productCode;
    private String code;
}
