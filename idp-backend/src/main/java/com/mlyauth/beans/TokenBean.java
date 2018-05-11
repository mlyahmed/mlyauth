package com.mlyauth.beans;

public class TokenBean {

    private final String serialized;
    private final String expiryTime;

    public TokenBean(final String serialized, final String expiryTime) {
        this.serialized = serialized;
        this.expiryTime = expiryTime;
    }

    public String getSerialized() {
        return serialized;
    }

    public String getExpiryTime() {
        return expiryTime;
    }
}
