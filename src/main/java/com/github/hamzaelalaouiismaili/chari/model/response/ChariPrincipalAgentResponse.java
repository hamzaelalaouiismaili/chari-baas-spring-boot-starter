package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Principal Agent information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariPrincipalAgentResponse {

    private PrincipalAgentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PrincipalAgentData {

        private Long agentId;

        @JsonAlias("Code")
        private String code;

        private String firstName;

        private String lastName;

        private String createdAt;

        private String phoneNumber;

        private String address;

        private String gender;

        private String email;

        private Long partnerId;

        private String cin;

        @JsonAlias("Account")
        private AccountData account;

        private PartnerData partner;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountData {

        private Long accountId;

        private Long customerId;

        private String rib;

        private String createdAt;

        private Integer accountType;

        private BigDecimal balance;

        private BigDecimal availableBalance;

        private BigDecimal pendingBalance;

        private BigDecimal frozenBalance;

        private Boolean isDefaultAccount;

        private Integer currentAccountLevel;

        private Integer accountStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PartnerData {

        private Long partnerId;

        private String name;

        private String email;

        private Boolean active;

        private String createdAt;

        private String description;

        private Long createdById;

        private Long ownerId;

        private String charipayPartnerName;
    }
}
