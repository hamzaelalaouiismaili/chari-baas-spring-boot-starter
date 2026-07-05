package com.github.hamzaelalaouiismaili.chari.model.response;

import com.github.hamzaelalaouiismaili.chari.model.card.ChariCardApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One card application returned after validation or rejection. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardApplicationResponse {
    private ChariCardApplication data;
}
