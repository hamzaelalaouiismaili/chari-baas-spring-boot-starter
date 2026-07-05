package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Customer identification values submitted to retrieve unpaid bills. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariBillUnpaidItemsPayload {

    @JsonProperty("CreancierVals")
    private List<ChariBillFieldValue> creditorValues;
}
