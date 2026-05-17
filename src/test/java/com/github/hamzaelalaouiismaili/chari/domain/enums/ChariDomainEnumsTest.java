package com.github.hamzaelalaouiismaili.chari.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariAccountLevel;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariClosureReason;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariDirection;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariDocumentType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTransactionType;

class ChariDomainEnumsTest {

    @Test
    void operationTypesMatchOfficialCodes() {
        assertThat(ChariOperationType.fromCode(5)).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
        assertThat(ChariOperationType.fromCode(10)).isEqualTo(ChariOperationType.RECHARGE);
        assertThat(ChariOperationType.fromCode(24)).isEqualTo(ChariOperationType.CARD_PAYMENT);
        assertThat(ChariOperationType.fromCode(25)).isEqualTo(ChariOperationType.BILL_PAYMENT);
    }

    @Test
    void transactionTypesMatchOfficialCodes() {
        assertThat(ChariTransactionType.fromCode(5)).isEqualTo(ChariTransactionType.MOBILE_PAYMENT);
        assertThat(ChariTransactionType.fromCode(17)).isEqualTo(ChariTransactionType.RECHARGE);
        assertThat(ChariTransactionType.fromCode(24)).isEqualTo(ChariTransactionType.CARD_PAYMENT);
        assertThat(ChariTransactionType.fromCode(25)).isEqualTo(ChariTransactionType.BILL_PAYMENT);
    }

    @Test
    void missingEnumsMapKnownCodes() {
        assertThat(ChariAccountLevel.fromCode(4)).isEqualTo(ChariAccountLevel.KYC_LEVEL_4);
        assertThat(ChariOperationStatus.fromCode(4)).isEqualTo(ChariOperationStatus.CANCELED);
        assertThat(ChariDirection.fromCode(1)).isEqualTo(ChariDirection.CREDIT);
        assertThat(ChariClosureReason.fromCode(2)).isEqualTo(ChariClosureReason.EDP_SUSPICION_OF_FRAUD);
        assertThat(ChariDocumentType.fromCode(8)).isEqualTo(ChariDocumentType.CommercialRegister);
        assertThat(ChariRequestOperationType.fromCode(2)).isEqualTo(ChariRequestOperationType.CASHOUT);
        assertThat(ChariRequestOperationStatus.fromCode(3)).isEqualTo(ChariRequestOperationStatus.FAILED);
    }

    @Test
    void unknownCodesMapToUnknown() {
        assertThat(ChariOperationType.fromCode(999)).isEqualTo(ChariOperationType.UNKNOWN);
        assertThat(ChariTransactionType.fromCode(999)).isEqualTo(ChariTransactionType.UNKNOWN);
        assertThat(ChariOperationStatus.fromCode(999)).isEqualTo(ChariOperationStatus.UNKNOWN);
        assertThat(ChariAccountLevel.fromCode(null)).isEqualTo(ChariAccountLevel.UNKNOWN);
    }
}
