package com.primasolutions.idp.sensitive;

import org.apache.commons.lang.StringUtils;
import org.jasypt.hibernate4.type.AbstractEncryptedAsStringType;

abstract class AbstractEncryptedType extends AbstractEncryptedAsStringType {
    private static final String WRAP_PREFIX = "ENC(";
    private static final String WRAP_SUFFIX = ")";

    boolean isWrapped(final String message) {
        return StringUtils.isNotBlank(message) && message.startsWith(WRAP_PREFIX) && message.endsWith(WRAP_SUFFIX);
    }

    String unwrap(final String message) {
        return message.substring(0, message.length() - 1).replace(WRAP_PREFIX, "");
    }

    String wrap(final String message) {
        return WRAP_PREFIX + message + WRAP_SUFFIX;
    }
}
