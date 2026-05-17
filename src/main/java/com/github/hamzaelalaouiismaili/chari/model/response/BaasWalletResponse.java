package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for BaaS wallet information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaasWalletResponse {

    private BaasWallet data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BaasWallet {

        private Integer id;

        private String fullName;

        private String firstName;

        private String lastName;

        private String phoneNumber;

        private String createdAt;

        private String enrolledAt;

        private BigDecimal balance;

        private String rib;

        private Integer accountLevel;

        private Integer customerStatus;
    }
}
