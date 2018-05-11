package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenType;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneId;
import java.util.Date;

import static java.time.LocalDateTime.now;
import static java.util.Date.from;

public class JOSERefreshToken extends AbstractJOSEToken {

    public static final int THREE_YEARS = 3;

    public JOSERefreshToken(final PrivateKey privateKey, final PublicKey publicKey) {
        super(privateKey, publicKey);
        initTimes();

    }

    public JOSERefreshToken(final String serialized, final PrivateKey privateKey, final PublicKey publicKey) {
        super(serialized, privateKey, publicKey);
    }

    private void initTimes() {
        builder = builder.expirationTime(from(now().plusYears(THREE_YEARS).atZone(ZoneId.systemDefault()).toInstant()))
                .notBeforeTime(new Date())
                .issueTime(new Date());
    }

    @Override
    public TokenType getType() {
        return TokenType.REFRESH;
    }

}
