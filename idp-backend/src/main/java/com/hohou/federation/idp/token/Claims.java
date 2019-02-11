package com.hohou.federation.idp.token;

import com.hohou.federation.idp.constants.IStringEnum;

import static java.util.Arrays.stream;

public enum Claims implements IStringEnum {
    REFRESH_MODE("refreshMode"),
    VALIDATION_MODE("validationhMode"),
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

    Claims(final String value) {
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

    @Override
    public boolean equals(final String value) {
        return this.value.equals(value);
    }

    public static Claims create(final String value) {
        return stream(Claims.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }
}
