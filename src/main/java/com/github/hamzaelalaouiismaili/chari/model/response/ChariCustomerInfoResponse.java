package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariAccountLevel;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Chari customer information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariCustomerInfoResponse {

    private ChariCustomerInfo data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChariCustomerInfo {

        private Integer id;

        private String phoneNumber;

        private String email;

        private String firstName;

        private String lastName;

        private String fullName;

        private Integer status;

        private Integer customerStatus;

        private Integer accountType;

        private Integer accountLevel;

        private BigDecimal balance;

        private String rib;

        private String createdAt;

        private String enrolledAt;

        private Boolean hasPinSet;

        private Long partnerId;

        private String createdBy;

        private Integer levelInReview;

        private Partner partner;

        @JsonIgnore
        public ChariCustomerStatus getTypedCustomerStatus() {
            return ChariCustomerStatus.fromCode(customerStatus != null ? customerStatus : status);
        }

        @JsonIgnore
        public ChariAccountLevel getCurrentAccountLevel() {
            return ChariAccountLevel.fromCode(accountLevel);
        }

        @JsonIgnore
        public ChariAccountLevel getLevelInReviewValue() {
            return ChariAccountLevel.fromCode(levelInReview);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Partner {

        private Long id;

        private String name;

        private String email;

        private Boolean active;

        private String createdAt;

        private String description;
    }
}
