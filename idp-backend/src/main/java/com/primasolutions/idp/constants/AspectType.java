package com.primasolutions.idp.constants;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.primasolutions.idp.constants.AspectType.MetaType.CL;
import static com.primasolutions.idp.constants.AspectType.MetaType.IDP;
import static com.primasolutions.idp.constants.AspectType.MetaType.RS;
import static com.primasolutions.idp.constants.AspectType.MetaType.SP;
import static com.primasolutions.idp.constants.TokenNorm.BASIC;
import static com.primasolutions.idp.constants.TokenNorm.JOSE;
import static com.primasolutions.idp.constants.TokenNorm.SAML;
import static java.util.Arrays.stream;

public enum AspectType implements IStringEnum {
    SP_BASIC("SP_BASIC", SP, BASIC),
    SP_SAML("SP_SAML", SP, SAML),
    IDP_JOSE("IDP_JOSE", IDP, JOSE),
    CL_JOSE("CL_JOSE", CL, JOSE),
    RS_JOSE("CL_JOSE", RS, JOSE);


    public enum MetaType { IDP, SP, CL, RS }

    private final String value;
    private final MetaType metaType;
    private final TokenNorm norm;

    AspectType(final String value, final MetaType metaType, final TokenNorm norm) {
        this.value = value;
        this.metaType = metaType;
        this.norm = norm;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name();
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public TokenNorm getNorm() {
        return norm;
    }

    @Override
    public boolean equals(final String value) {
        return this.value.equals(value);
    }

    public static AspectType create(final String value) {
        return stream(AspectType.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }

    public static Collection<AspectType> getAllSP() {
        return stream(AspectType.values()).filter(aspect -> aspect.metaType == SP).collect(Collectors.toSet());
    }

}
