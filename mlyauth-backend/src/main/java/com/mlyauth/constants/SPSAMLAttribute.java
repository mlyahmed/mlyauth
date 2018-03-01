package com.mlyauth.constants;

public enum SPSAMLAttribute implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    SP_SAML_SSO_URL("Auth:SP:SAML:EndPoint"),
    SP_SAML_ENTITY_ID("Auth:SP:SAML:Entity:ID"),
    SP_SAML_ENCRYPTION_CERTIFICATE("Auth:SP:SAML:Encryption:Certificate");

    private final String value;

    SPSAMLAttribute(String value) {
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
