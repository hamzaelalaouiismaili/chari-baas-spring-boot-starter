package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for a retail agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariRetailAgentResponse {

    private RetailAgentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RetailAgentData {

        @JsonAlias("Code")
        private String code;

        @JsonAlias("Name")
        private String name;

        private String createdAt;

        @JsonAlias("Active")
        private String active;

        private String phoneNumber;

        @JsonAlias("Account")
        private Map<String, Object> account;
    }
}
