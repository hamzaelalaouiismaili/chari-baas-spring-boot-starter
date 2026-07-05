package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Complete usage-control configuration for an issued card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardUsageControlPayload {
    private Boolean allowAtm;
    private Boolean allowOnline;
    private Boolean allowPos;
    private Boolean contactlessEnabled;
}
