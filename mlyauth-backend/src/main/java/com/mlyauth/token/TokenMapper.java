package com.mlyauth.token;

import com.mlyauth.domain.Token;
import com.mlyauth.token.saml.SAMLAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class TokenMapper {

    @Autowired
    private PasswordEncoder encoder;

    public Token toToken(SAMLAccessToken access) {
        if (access == null) return null;

        return Token.newInstance()
                .setStamp(encoder.encode(access.getStamp()))
                .setIssuanceTime(toDate(access.getIssuanceTime()))
                .setEffectiveTime(toDate(access.getEffectiveTime()))
                .setExpiryTime(toDate(access.getExpiryTime()))
                .setType(access.getType())
                .setNorm(access.getNorm())
                ;
    }


    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }
}
