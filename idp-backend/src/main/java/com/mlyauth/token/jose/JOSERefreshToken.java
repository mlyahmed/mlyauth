package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenType;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneId;
import java.util.Date;

import static java.time.LocalDateTime.now;
import static java.util.Date.from;

public class JOSERefreshToken extends AbstractJOSEToken {

    public JOSERefreshToken(PrivateKey privateKey, PublicKey publicKey) {
        super(privateKey, publicKey);
        initTimes();

    }

    public JOSERefreshToken(String serialized, PrivateKey privateKey, PublicKey publicKey) {
        super(serialized, privateKey, publicKey);
    }

    private void initTimes() {
        builder = builder.expirationTime(from(now().plusYears(3).atZone(ZoneId.systemDefault()).toInstant()))
                .notBeforeTime(new Date())
                .issueTime(new Date());
    }

    @Override
    public TokenType getType() {
        return TokenType.REFRESH;
    }

}