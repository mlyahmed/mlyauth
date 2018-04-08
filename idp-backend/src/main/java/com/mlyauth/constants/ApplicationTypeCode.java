package com.mlyauth.constants;

import static java.util.Arrays.stream;

public enum ApplicationTypeCode implements IStringEnum {
    IDP("IDP"), POLICY("POLICY"), CLAIMS("CLAIMS"), CLIENT_SPACE("CLIENT_SPACE"), SEFAS("SEFAS"), STORE("STORE");

    private final String value;

    ApplicationTypeCode(String value){
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


    public boolean isPolicy(){
        return this == POLICY;
    }

    public static ApplicationTypeCode create(String value) {
        return stream(ApplicationTypeCode.values()).filter(v -> v.value.equals(value)).findFirst().orElse(null);
    }
}
