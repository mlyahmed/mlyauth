package com.primasolutions.idp.constants;

import static java.util.Arrays.stream;

public enum RoleCode implements IStringEnum {
    ADMIN("ADMIN"), MANAGER("MANAGER"), CLIENT("CLIENT");

    private final String value;

    RoleCode(final String value) {
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

    public boolean isClient() {
        return this == CLIENT;
    }

    public static RoleCode create(final String value) {
        return stream(RoleCode.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }
}
