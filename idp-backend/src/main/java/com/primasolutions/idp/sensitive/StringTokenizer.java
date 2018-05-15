package com.primasolutions.idp.sensitive;

import static org.apache.commons.lang.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;

public class StringTokenizer {

    private static final int THIRD = 3;

    public static StringTokenizer newInstance() {
        return new StringTokenizer();
    }

    String tokenize(final String value) {
        return left(value) + mid(value) + right(value);
    }

    private String left(final String value) {
        return rightPad(value.charAt(0) + "", value.length() / THIRD, '*');
    }

    private char mid(final String value) {
        return value.charAt(value.length() / THIRD);
    }

    private String right(final String value) {
        final int length = value.length();
        return leftPad(value.charAt(length - 1) + "", length - 1 - length / THIRD, '*');
    }
}
