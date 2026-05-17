package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error envelope returned by Chari BaaS for non-success responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariErrorResponse {

    private Integer errorCode;

    private String errorDescription;

    @JsonIgnore
    public ChariErrorCode getKnownErrorCode() {
        return ChariErrorCode.fromCode(errorCode);
    }
}
