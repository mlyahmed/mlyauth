package com.primasolutions.idp.sample.client;

public class SampleClientTokenBean {
    private final String serialized;
    private final String expiryTime;
    private long elapsed;

    public SampleClientTokenBean(String serialized, String expiryTime) {
        this.serialized = serialized;
        this.expiryTime = expiryTime;
    }

    public String getSerialized() {
        return serialized;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }
}
