package com.github.hamzaelalaouiismaili.chari.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NumericIdentifierUtilTest {

    @Test
    void removesWhitespaceFromNumericIdentifiers() {
        assertThat(NumericIdentifierUtil.normalize(" 171 0301 \n"))
                .isEqualTo("1710301");
    }

    @Test
    void preservesNullIdentifiers() {
        assertThat(NumericIdentifierUtil.normalize(null)).isNull();
    }
}
