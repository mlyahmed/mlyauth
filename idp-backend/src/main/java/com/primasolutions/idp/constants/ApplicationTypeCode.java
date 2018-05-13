package com.primasolutions.idp.constants;

import static java.util.Arrays.stream;

public enum ApplicationTypeCode implements IStringEnum {
    IDP("IDP"), POLICY("POLICY"), CLAIMS("CLAIMS"), CLIENT_SPACE("CLIENT_SPACE"), SEFAS("SEFAS"), STORE("STORE");

    private final String value;

    ApplicationTypeCode(final String value) {
        this.value = value;
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
    public boolean equals(final String value) {
        return this.value.equals(value);
    }

    public boolean isPolicy() {
        return this == POLICY;
    }

    public static ApplicationTypeCode create(final String value) {
        return stream(ApplicationTypeCode.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }
}
