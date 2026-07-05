package com.github.hamzaelalaouiismaili.chari.model.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Card application returned by application lifecycle endpoints. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardApplication {
    private Long customerId;
    private Long applicationId;
    private String customerFullName;
    private Long cardProgramId;
    private String cardProgramName;
    private Integer applicationStatus;
    private String createdAt;
    private String validatedAt;
    private String validatedByUsername;
    private String rejectionReason;
    private Boolean isManualEntry;
    private String phoneNumber;
    private Integer partnerId;
    private Object defaultLimits;

    @JsonIgnore
    public ChariCardApplicationStatus getTypedApplicationStatus() {
        return ChariCardApplicationStatus.fromCode(applicationStatus);
    }
}
