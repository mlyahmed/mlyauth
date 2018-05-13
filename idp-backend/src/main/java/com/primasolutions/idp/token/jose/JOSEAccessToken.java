package com.primasolutions.idp.token.jose;

import com.primasolutions.idp.constants.TokenRefreshMode;
import com.primasolutions.idp.constants.TokenType;
import com.primasolutions.idp.constants.TokenValidationMode;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static com.primasolutions.idp.token.Claims.REFRESH_MODE;
import static com.primasolutions.idp.token.Claims.VALIDATION_MODE;
import static java.time.LocalDateTime.now;

public class JOSEAccessToken extends AbstractJOSEToken {

    public static final int THERTY_MINUTES = 60 * 30;
    public static final int THREE_MINUTES = 60 * 3;

    public JOSEAccessToken(final String serialize, final PrivateKey privateKey, final PublicKey publicKey) {
        super(serialize, privateKey, publicKey);
    }

    public JOSEAccessToken(final PrivateKey privateKey, final PublicKey publicKey) {
        super(privateKey, publicKey);
        builder = builder.claim(REFRESH_MODE.getValue(), TokenRefreshMode.EACH_TIME.name());
        builder = builder.claim(VALIDATION_MODE.getValue(), TokenValidationMode.STRICT.name());
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
        setExpirationTime((getRefreshMode() == TokenRefreshMode.WHEN_EXPIRES) ? THERTY_MINUTES : THREE_MINUTES);
        setEffectiveTime(1);
        setIssuanceTime(1);
    }

    private void setExpirationTime(final long seconds) {
        Instant expiration = now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant();
        builder = builder.expirationTime(Date.from(expiration));
    }

    private void setEffectiveTime(final long sec) {
        builder = builder.notBeforeTime(Date.from(now().minusSeconds(sec).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void setIssuanceTime(final long seconds) {
        builder = builder.issueTime(Date.from(now().minusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()));
    }

}
