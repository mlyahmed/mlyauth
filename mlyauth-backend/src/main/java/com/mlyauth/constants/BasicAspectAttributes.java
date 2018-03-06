package com.mlyauth.constants;

public enum BasicAspectAttributes implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    SP_BASIC_SSO_URL("Auth:SP:Basic:EndPoint"),
    SP_BASIC_USERNAME("Auth:SP:Basic:Username"),
    SP_BASIC_PASSWORD("Auth:SP:Basic:Password");

    private final String value;

    BasicAspectAttributes(String value) {
        this.value = value;
    }

    public static AuthAspectAttribute create(String value) {
        return UNDEFINED.create(AuthAspectAttribute.class, value);
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
