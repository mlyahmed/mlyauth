package com.mlyauth.token;

import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.constants.IStringEnum;

public enum IDPClaims implements IStringEnum {
    UNDEFINED("UNDEFINED"),
    SUBJECT("subject"),
    SCOPES("scopes"),
    BP("bp"),
    STATE("state"),
    ISSUER("iss"),
    AUDIENCE("audience"),
    TARGET_URL("targetURL"),
    DELEGATOR("delegator"),
    DELEGATE("delegate"),
    VERDICT("verdict"),
    CLIENT_ID("idClient"),
    CLIENT_PROFILE("profilUtilisateur"),
    ENTITY_ID("idPrestation"),
    ACTION("action"),
    APPLICATION("application");

    private final String value;

    IDPClaims(String value) {
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
