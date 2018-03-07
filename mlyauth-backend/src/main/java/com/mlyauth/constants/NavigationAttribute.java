package com.mlyauth.constants;

public enum NavigationAttribute implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    SAML_RESPONSE("SAMLResponse");

    private final String value;

    NavigationAttribute(String value) {
        this.value = value;
    }

    public static AspectAttribute create(String value) {
        return UNDEFINED.create(AspectAttribute.class, value);
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
