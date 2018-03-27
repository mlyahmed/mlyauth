package com.mlyauth.beans;

public class TokenBean {

    private final String serialized;
    private final String expiryTime;

    public TokenBean(String serialized, String expiryTime) {
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
