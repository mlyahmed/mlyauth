package com.mlyauth.constants;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.mlyauth.constants.AspectType.MetaType.IDP;
import static com.mlyauth.constants.AspectType.MetaType.SP;
import static com.mlyauth.constants.TokenNorm.*;
import static java.util.Arrays.stream;

public enum AspectType implements IStringEnum {
    SP_BASIC("SP_BASIC", SP, BASIC),
    SP_SAML("SP_SAML", SP, SAML),
    IDP_JOSE("IDP_JOSE", IDP, JOSE);


    public enum MetaType {IDP, SP, CLIENT, RS}

    private final String value;
    private final MetaType metaType;
    private final TokenNorm norm;

    AspectType(String value, MetaType metaType, TokenNorm norm) {
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
    public boolean equals(String value) {
        return this.value.equals(value);
    }

    public static AspectType create(String value) {
        return stream(AspectType.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }

    public static Collection<AspectType> getAllSP() {
        return stream(AspectType.values()).filter(aspect -> aspect.metaType == SP).collect(Collectors.toSet());
    }

}
