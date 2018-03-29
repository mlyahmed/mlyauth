package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenProcessingStatus;
import com.mlyauth.constants.TokenRefreshMode;
import com.mlyauth.constants.TokenType;
import com.mlyauth.constants.TokenValidationMode;
import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static com.mlyauth.constants.TokenRefreshMode.EACH_TIME;
import static com.mlyauth.constants.TokenValidationMode.STRICT;
import static com.mlyauth.token.Claims.REFRESH_MODE;
import static com.mlyauth.token.Claims.VALIDATION_MODE;
import static java.time.LocalDateTime.now;

public class JOSEAccessToken extends AbstractJOSEToken {

    public JOSEAccessToken(String serialize, PrivateKey privateKey, PublicKey publicKey) {
        super(serialize, privateKey, publicKey);
    }

    public JOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        super(privateKey, publicKey);
        builder = builder.claim(REFRESH_MODE.getValue(), EACH_TIME.name());
        builder = builder.claim(VALIDATION_MODE.getValue(), STRICT.name());
    }

    @Override
    public TokenRefreshMode getRefreshMode() {
        return TokenRefreshMode.valueOf((String) builder.build().getClaim(REFRESH_MODE.getValue()));
    }

    @Override
    public void setRefreshMode(TokenRefreshMode mode) {
        Assert.notNull(mode, "Refresh Mode is null.");
        checkUnmodifiable();
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

    @Override
    public void cypher() {
        setTimes();
        super.cypher();
    }

    private void setTimes() {
        setExpirationTime((getRefreshMode() == TokenRefreshMode.WHEN_EXPIRES) ? 60 * 30 : 60 * 3);
        setEffectiveTime(1);
        setIssuanceTime(1);
    }

    private void setExpirationTime(long seconds){
        Instant expiration = now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant();
        builder = builder.expirationTime(Date.from(expiration));
    }

    private void setEffectiveTime(long seconds){
        builder = builder.notBeforeTime(Date.from(now().minusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void setIssuanceTime(long seconds){
        builder = builder.issueTime(Date.from(now().minusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()));
    }

}
