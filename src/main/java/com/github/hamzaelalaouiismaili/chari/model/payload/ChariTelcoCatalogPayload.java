package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request used to retrieve available Telco recharge products. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariTelcoCatalogPayload {

    private String recipientPhoneNumber;
    private Integer amount;
    private ChariTelcoOperator operator;
}
