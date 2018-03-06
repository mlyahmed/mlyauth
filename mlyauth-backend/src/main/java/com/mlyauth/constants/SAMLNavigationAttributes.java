package com.mlyauth.constants;

public enum SAMLNavigationAttributes implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    SAML_RESPONSE("SAMLResponse");

    private final String value;

    SAMLNavigationAttributes(String value) {
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
