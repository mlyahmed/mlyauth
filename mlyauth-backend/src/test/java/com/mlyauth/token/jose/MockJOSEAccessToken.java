package com.mlyauth.token.jose;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

public class MockJOSEAccessToken extends JOSEAccessToken {

    private LocalDateTime expiryTime;
    private LocalDateTime issuanceTime;
    private LocalDateTime effectiveTime;

    public MockJOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        super(privateKey, publicKey);
    }


    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public LocalDateTime getExpiryTime() {
        return expiryTime != null ? expiryTime : super.getExpiryTime();
    }


    public void setIssuanceTime(LocalDateTime issuanceTime) {
        this.issuanceTime = issuanceTime;
    }

    @Override
    public LocalDateTime getIssuanceTime() {
        return issuanceTime != null ? issuanceTime : super.getIssuanceTime();
    }

    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        return effectiveTime != null ? effectiveTime : super.getEffectiveTime();
    }
}
