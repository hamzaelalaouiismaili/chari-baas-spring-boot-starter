package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Chari customer status check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariCustomerStatusResponse {

    private CustomerStatusData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerStatusData {

        private Integer status;

        private String message;

        @JsonIgnore
        public ChariCustomerStatus getCustomerStatus() {
            return ChariCustomerStatus.fromCode(status);
        }

        @JsonIgnore
        public boolean canTransact() {
            return getCustomerStatus().canTransact();
        }

        @JsonIgnore
        public boolean needsRegistration() {
            return getCustomerStatus().needsRegistration();
        }

        @JsonIgnore
        public boolean needsOtpConfirmation() {
            return getCustomerStatus().needsOtpConfirmation();
        }

        @JsonIgnore
        public boolean isConfirmed() {
            return getCustomerStatus() == ChariCustomerStatus.CONFIRMED;
        }

        @JsonIgnore
        public boolean isLocked() {
            return getCustomerStatus().isLocked();
        }
    }
}
