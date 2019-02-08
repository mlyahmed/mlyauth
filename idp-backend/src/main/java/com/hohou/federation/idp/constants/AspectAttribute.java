package com.hohou.federation.idp.constants;

import static java.util.Arrays.stream;

public enum AspectAttribute implements IStringEnum {

    SP_BASIC_SSO_URL(AspectType.SP_BASIC, AttributeType.ENDPOINT, "Auth:SP:Basic:EndPoint", false),
    SP_BASIC_USERNAME(AspectType.SP_BASIC, AttributeType.USERNAME, "Auth:SP:Basic:Username", false),
    SP_BASIC_PASSWORD(AspectType.SP_BASIC, AttributeType.PASSWORD, "Auth:SP:Basic:Password", true),

    SP_SAML_SSO_URL(AspectType.SP_SAML, AttributeType.ENDPOINT, "Auth:SP:SAML:EndPoint", false),
    SP_SAML_ENTITY_ID(AspectType.SP_SAML, AttributeType.ENTITYID, "Auth:SP:SAML:Entity:ID", false),
    SP_SAML_ENCRYPTION_CERTIFICATE(AspectType.SP_SAML, AttributeType.CERTIFICATE, "Auth:SP:SAML:Encryption:Certificate", false),

    IDP_JOSE_SSO_URL(AspectType.IDP_JOSE, AttributeType.ENDPOINT, "Auth:IDP:JOSE:EndPoint", false),
    IDP_JOSE_ENTITY_ID(AspectType.IDP_JOSE, AttributeType.ENTITYID, "Auth:IDP:JOSE:Entity:ID", false),
    IDP_JOSE_ENCRYPTION_CERTIFICATE(AspectType.IDP_JOSE, AttributeType.CERTIFICATE, "Auth:IDP:JOSE:Encryption:Certificate", false),

    CL_JOSE_CONTEXT(AspectType.CL_JOSE, AttributeType.CONTEXT, "Auth:CL:JOSE:Context", false),
    CL_JOSE_ENTITY_ID(AspectType.CL_JOSE, AttributeType.ENTITYID, "Auth:CL:JOSE:Entity:ID", false),
    CL_JOSE_ENCRYPTION_CERTIFICATE(AspectType.CL_JOSE, AttributeType.CERTIFICATE, "Auth:CL:JOSE:Encryption:Certificate", false),

    RS_JOSE_CONTEXT(AspectType.RS_JOSE, AttributeType.CONTEXT, "Auth:RS:JOSE:Context", false),
    RS_JOSE_ENTITY_ID(AspectType.RS_JOSE, AttributeType.ENTITYID, "Auth:RS:JOSE:Entity:ID", false),
    RS_JOSE_ENCRYPTION_CERTIFICATE(AspectType.RS_JOSE, AttributeType.CERTIFICATE, "Auth:RS:JOSE:Encryption:Certificate", false);

    private final AttributeType type;
    private final AspectType aspect;
    private final String value;
    private final boolean secret;

    AspectAttribute(final AspectType aspect, final AttributeType type, final String value, final boolean secret) {
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
    public boolean equals(final String value) {
        return this.value.equals(value);
    }

    public static AspectAttribute create(final String value) {
        return stream(AspectAttribute.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }

    public static AspectAttribute get(final AspectType aspect, final AttributeType type) {
        return stream(AspectAttribute.values())
                .filter(v -> v.getAspect() == aspect && v.getType() == type)
                .findFirst().orElse(null);
    }
}
