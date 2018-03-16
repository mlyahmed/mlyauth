package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenType;
import com.nimbusds.jwt.JWTClaimsSet;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneId;
import java.util.Date;

import static java.time.LocalDateTime.now;
import static java.util.Date.from;
import static org.springframework.util.Assert.notNull;

public class JOSERefreshToken extends AbstractJOSEToken {

    public JOSERefreshToken(PrivateKey privateKey, PublicKey publicKey) {
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        builder = new JWTClaimsSet.Builder();
        initTimes();

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