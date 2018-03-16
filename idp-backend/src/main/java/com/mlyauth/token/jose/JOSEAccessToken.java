package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenType;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JOSEAccessToken extends AbstractJOSEToken {

    public JOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        super(privateKey, publicKey);
        initTimes();
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
    public TokenType getType() {
        return TokenType.ACCESS;
    }

}
