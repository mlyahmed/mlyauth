package com.mlyauth.constants;

import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.IDP_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static com.mlyauth.constants.AspectType.SP_BASIC;
import static com.mlyauth.constants.AspectType.SP_SAML;
import static com.mlyauth.constants.AttributeType.CERTIFICATE;
import static com.mlyauth.constants.AttributeType.CONTEXT;
import static com.mlyauth.constants.AttributeType.ENDPOINT;
import static com.mlyauth.constants.AttributeType.ENTITYID;
import static com.mlyauth.constants.AttributeType.PASSWORD;
import static com.mlyauth.constants.AttributeType.USERNAME;
import static java.util.Arrays.stream;

public enum AspectAttribute implements IStringEnum {

    SP_BASIC_SSO_URL(SP_BASIC, ENDPOINT, "Auth:SP:Basic:EndPoint", false),
    SP_BASIC_USERNAME(SP_BASIC, USERNAME, "Auth:SP:Basic:Username", false),
    SP_BASIC_PASSWORD(SP_BASIC, PASSWORD, "Auth:SP:Basic:Password", true),

    SP_SAML_SSO_URL(SP_SAML, ENDPOINT, "Auth:SP:SAML:EndPoint", false),
    SP_SAML_ENTITY_ID(SP_SAML, ENTITYID, "Auth:SP:SAML:Entity:ID", false),
    SP_SAML_ENCRYPTION_CERTIFICATE(SP_SAML, CERTIFICATE, "Auth:SP:SAML:Encryption:Certificate", false),

    IDP_JOSE_SSO_URL(IDP_JOSE, ENDPOINT, "Auth:IDP:JOSE:EndPoint", false),
    IDP_JOSE_ENTITY_ID(IDP_JOSE, ENTITYID, "Auth:IDP:JOSE:Entity:ID", false),
    IDP_JOSE_ENCRYPTION_CERTIFICATE(IDP_JOSE, CERTIFICATE, "Auth:IDP:JOSE:Encryption:Certificate", false),

    CL_JOSE_CONTEXT(CL_JOSE, CONTEXT, "Auth:CL:JOSE:Context", false),
    CL_JOSE_ENTITY_ID(CL_JOSE, ENTITYID, "Auth:CL:JOSE:Entity:ID", false),
    CL_JOSE_ENCRYPTION_CERTIFICATE(CL_JOSE, CERTIFICATE, "Auth:CL:JOSE:Encryption:Certificate", false),

    RS_JOSE_CONTEXT(RS_JOSE, CONTEXT, "Auth:RS:JOSE:Context", false),
    RS_JOSE_ENTITY_ID(RS_JOSE, ENTITYID, "Auth:RS:JOSE:Entity:ID", false),
    RS_JOSE_ENCRYPTION_CERTIFICATE(RS_JOSE, CERTIFICATE, "Auth:RS:JOSE:Encryption:Certificate", false);

    private final AttributeType type;
    private final AspectType aspect;
    private final String value;
    private final boolean secret;

    AspectAttribute(AspectType aspect, AttributeType type, String value, boolean secret) {
        this.aspect = aspect;
        this.type = type;
        this.value = value;
        this.secret = secret;
    }

    public AttributeType getType() {
        return type;
    }

    public AspectType getAspect() {
        return aspect;
    }

    public boolean isSecret() {
        return secret;
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

    public static AspectAttribute create(String value) {
        return stream(AspectAttribute.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }

    public static AspectAttribute get(AspectType aspect, AttributeType type) {
        return stream(AspectAttribute.values())
                .filter(v -> v.getAspect() == aspect && v.getType() == type)
                .findFirst().orElse(null);
    }
}
