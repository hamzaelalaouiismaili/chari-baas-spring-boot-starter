package com.github.hamzaelalaouiismaili.chari.model.card;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Transaction performed with an issued card. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardTransaction {
    private Long cardTransactionId;
    private Long transactionId;
    private Long cardId;
    private String maskedPan;
    private String description;
    private String merchantName;
    private String merchantCity;
    private String merchantCountry;
    private String merchantCategoryCode;
    private String authCode;
    private String terminalId;
    private Boolean isContactless;
    private Integer transactionMethod;
    private BigDecimal amount;
    private String currencyCode;
    private Integer transactionStatus;
    private Integer transactionType;
    private String transactionDate;
    private String createdAt;
    private Integer partnerId;
    private Integer operationType;
    private Long operationId;
}
