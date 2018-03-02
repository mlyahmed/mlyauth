package com.mlyauth.security.token;

import com.mlyauth.constants.IStringEnum;
import com.mlyauth.constants.SPSAMLAttribute;

public enum ExtraClaims implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    BP("bp"),
    SCOPES("scopes"),
    DELEGATOR("delegator"),
    DELEGATE("delegate"),
    STATE("state");

    private final String value;

    ExtraClaims(String value) {
        this.value = value;
    }

    public static SPSAMLAttribute create(String value) {
        return UNDEFINED.create(SPSAMLAttribute.class, value);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean equals(String value) {
        return this.value.equals(value);
    }
}
