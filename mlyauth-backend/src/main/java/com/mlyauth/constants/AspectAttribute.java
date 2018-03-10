package com.mlyauth.constants;

import static com.mlyauth.constants.AspectType.*;
import static com.mlyauth.constants.AttributeType.*;
import static java.util.Arrays.stream;

public enum AspectAttribute implements IStringEnum {

    SP_BASIC_SSO_URL(SP_BASIC, ENDPOINT, "Auth:SP:Basic:EndPoint"),
    SP_BASIC_USERNAME(SP_BASIC, USERNAME, "Auth:SP:Basic:Username"),
    SP_BASIC_PASSWORD(SP_BASIC, PASSWORD, "Auth:SP:Basic:Password"),

    SP_SAML_SSO_URL(SP_SAML, ENDPOINT, "Auth:SP:SAML:EndPoint"),
    SP_SAML_ENTITY_ID(SP_SAML, ENTITYID, "Auth:SP:SAML:Entity:ID"),
    SP_SAML_ENCRYPTION_CERTIFICATE(SP_SAML, CERTIFICATE, "Auth:SP:SAML:Encryption:Certificate"),

    IDP_JOSE_SSO_URL(IDP_JOSE, ENDPOINT, "Auth:IDP:JOSE:EndPoint"),
    IDP_JOSE_ENTITY_ID(IDP_JOSE, ENTITYID, "Auth:IDP:JOSE:Entity:ID"),
    IDP_JOSE_ENCRYPTION_CERTIFICATE(IDP_JOSE, CERTIFICATE, "Auth:IDP:JOSE:Encryption:Certificate");

    private final AttributeType type;
    private final AspectType aspect;
    private final String value;

    AspectAttribute(AspectType aspect, AttributeType type, String value) {
        this.aspect = aspect;
        this.type = type;
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

    public AttributeType getType() {
        return type;
    }

    public AspectType getAspect() {
        return aspect;
    }

    @Override
    public boolean equals(String value) {
        return this.value.equals(value);
    }

    public static AspectAttribute create(String value) {
        return stream(AspectAttribute.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }

    public static AspectAttribute get(AspectType aspect, AttributeType type) {
        return stream(AspectAttribute.values())
                .filter(v -> v.getAspect() == aspect && v.getType() == type)
                .findFirst().orElse(null);
    }
}
