package com.mlyauth.constants;

public enum AuthAspectAttribute implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    SP_SAML_SSO_URL("Auth:SP:SAML:EndPoint"),
    SP_SAML_ENTITY_ID("Auth:SP:SAML:Entity:ID"),
    SP_SAML_ENCRYPTION_CERTIFICATE("Auth:SP:SAML:Encryption:Certificate"),
    IDP_JOSE_SSO_URL("Auth:IDP:JOSE:EndPoint"),
    IDP_JOSE_ENTITY_ID("Auth:IDP:JOSE:Entity:ID"),
    IDP_JOSE_ENCRYPTION_CERTIFICATE("Auth:IDP:JOSE:Encryption:Certificate");

    private final String value;

    AuthAspectAttribute(String value) {
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
