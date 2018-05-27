package com.primasolutions.idp.token;

public class AccessTokenBean {

    private final String serialized;
    private final String expiryTime;

    public AccessTokenBean(final String serialized, final String expiryTime) {
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
