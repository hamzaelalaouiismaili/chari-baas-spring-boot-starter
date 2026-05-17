package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariSens;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Query filters for listing operations by customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariOperationsByCustomerQuery {

    private String phoneNumber;

    private Integer pageSize;

    private Integer pageNumber;

    private List<Integer> operationType;

    private Integer transactionStatus;

    private Integer sens;

    private String from;

    private String to;

    public static class ChariOperationsByCustomerQueryBuilder {

        public ChariOperationsByCustomerQueryBuilder operationTypes(List<ChariOperationType> operationTypes) {
            this.operationType = operationTypes == null
                    ? null
                    : operationTypes.stream().map(ChariOperationType::getCode).toList();
            return this;
        }

        public ChariOperationsByCustomerQueryBuilder transactionStatus(ChariOperationStatus transactionStatus) {
            this.transactionStatus = transactionStatus == null ? null : transactionStatus.getCode();
            return this;
        }

        public ChariOperationsByCustomerQueryBuilder sens(ChariSens sens) {
            this.sens = sens == null ? null : sens.getCode();
            return this;
        }
    }
}
