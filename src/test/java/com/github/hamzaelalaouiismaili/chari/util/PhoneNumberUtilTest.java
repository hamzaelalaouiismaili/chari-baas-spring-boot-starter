package com.github.hamzaelalaouiismaili.chari.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;

class PhoneNumberUtilTest {

    @Test
    void normalizesMoroccanLocalNumber() {
        assertThat(PhoneNumberUtil.normalize("06 12-34-56-78")).isEqualTo("+212612345678");
    }

    @Test
    void normalizesNineDigitMoroccanNumberWithoutCountryPrefix() {
        assertThat(PhoneNumberUtil.normalize("608814002")).isEqualTo("+212608814002");
    }

    @Test
    void validatesMoroccanMobileNumber() {
        assertThat(PhoneNumberUtil.isValidMoroccanNumber("0712345678")).isTrue();
        assertThat(PhoneNumberUtil.isValidMoroccanNumber("608814002")).isTrue();
        assertThat(PhoneNumberUtil.isValidMoroccanNumber("+212412345678")).isFalse();
    }

    @Test
    void rejectsBlankNumber() {
        assertThatThrownBy(() -> PhoneNumberUtil.normalize(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone number is required");
    }
}
