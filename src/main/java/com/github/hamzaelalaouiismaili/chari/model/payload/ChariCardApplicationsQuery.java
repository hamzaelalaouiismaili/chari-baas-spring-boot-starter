package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pagination and status filters for card applications. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariCardApplicationsQuery {
    private Integer pageSize;
    private Integer pageNumber;
    private ChariCardApplicationStatus status;
}
