package com.github.hamzaelalaouiismaili.chari.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OtpCodeUtilTest {

    @Test
    void formatsCompactConfirmationCode() {
        assertThat(OtpCodeUtil.normalizeConfirmationCode("365768"))
                .isEqualTo("365-768");
    }

    @Test
    void keepsFormattedConfirmationCode() {
        assertThat(OtpCodeUtil.normalizeConfirmationCode("365-768"))
                .isEqualTo("365-768");
    }

    @Test
    void toleratesWhitespaceAroundConfirmationCode() {
        assertThat(OtpCodeUtil.normalizeConfirmationCode(" 365 768 "))
                .isEqualTo("365-768");
    }

    @Test
    void preservesNullConfirmationCode() {
        assertThat(OtpCodeUtil.normalizeConfirmationCode(null)).isNull();
    }
}
