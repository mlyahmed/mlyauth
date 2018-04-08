package com.mlyauth.constants;

import static java.util.Arrays.stream;

public enum RoleCode implements IStringEnum {
    ADMIN("ADMIN"), MANAGER("MANAGER"), CLIENT("CLIENT");

    final String value;

    RoleCode(String value){
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
    public boolean equals(String value) {
        return this.value.equals(value);
    }

    public boolean isClient(){
        return this == CLIENT;
    }

    public static RoleCode create(String value) {
        return stream(RoleCode.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }
}
