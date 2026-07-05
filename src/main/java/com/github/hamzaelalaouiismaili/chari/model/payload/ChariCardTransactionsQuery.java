package com.github.hamzaelalaouiismaili.chari.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pagination, customer, and ISO-8601 date filters for card transactions. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardTransactionsQuery {
    private Integer pageSize;
    private Integer pageNumber;
    private String phoneNumber;
    private String from;
    private String to;
}
