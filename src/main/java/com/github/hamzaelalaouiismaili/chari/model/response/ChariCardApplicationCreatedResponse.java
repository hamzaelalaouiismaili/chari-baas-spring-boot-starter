package com.github.hamzaelalaouiismaili.chari.model.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Empty acknowledgement returned after adding a card application. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardApplicationCreatedResponse {
    private Map<String, Object> data;
}
