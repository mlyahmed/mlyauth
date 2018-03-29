package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenProcessingStatus;
import com.mlyauth.constants.TokenRefreshMode;
import com.mlyauth.constants.TokenType;
import com.mlyauth.constants.TokenValidationMode;
import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.mlyauth.constants.TokenRefreshMode.EACH_TIME;
import static com.mlyauth.constants.TokenValidationMode.STRICT;
import static com.mlyauth.token.Claims.REFRESH_MODE;
import static com.mlyauth.token.Claims.VALIDATION_MODE;

public class JOSEAccessToken extends AbstractJOSEToken {

    public JOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        super(privateKey, publicKey);
        initModes();
        initTimes();
    }

    private void initModes() {
        builder = builder.claim(REFRESH_MODE.getValue(), EACH_TIME.name());
        builder = builder.claim(VALIDATION_MODE.getValue(), STRICT.name());
    }

    private void initTimes() {
        Instant threeMinutesAfter = LocalDateTime.now().plusSeconds(179).atZone(ZoneId.systemDefault()).toInstant();
        Instant aSecondAgo = LocalDateTime.now().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant();
        builder = builder.expirationTime(Date.from(threeMinutesAfter))
                .notBeforeTime(Date.from(aSecondAgo))
                .issueTime(Date.from(aSecondAgo));
    }

    public JOSEAccessToken(String serialize, PrivateKey privateKey, PublicKey publicKey) {
        super(serialize, privateKey, publicKey);
    }

    @Override
    public TokenRefreshMode getRefreshMode() {
        return TokenRefreshMode.valueOf((String) builder.build().getClaim(REFRESH_MODE.getValue()));
    }

    @Override
    public void setRefreshMode(TokenRefreshMode mode) {
        Assert.notNull(mode, "Refresh Mode is null.");
        builder = builder.claim(REFRESH_MODE.getValue(), mode.name());
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public TokenValidationMode getValidationMode() {
        return TokenValidationMode.valueOf((String) builder.build().getClaim(VALIDATION_MODE.getValue()));
    }

    @Override
    public void setValidationMode(TokenValidationMode mode) {
        Assert.notNull(mode, "Refresh Mode is null.");
        checkUnmodifiable();
        builder = builder.claim(VALIDATION_MODE.getValue(), mode.name());
        if(mode == TokenValidationMode.STRICT) setRefreshMode(EACH_TIME);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public TokenType getType() {
        return TokenType.ACCESS;
    }

}
