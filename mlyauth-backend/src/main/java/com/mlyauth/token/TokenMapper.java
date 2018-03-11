package com.mlyauth.token;

import com.mlyauth.domain.Token;
import com.mlyauth.token.saml.SAMLAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TokenMapper {

    @Autowired
    private PasswordEncoder encoder;

    public Token toToken(SAMLAccessToken access) {
        if (access == null) return null;

        return Token.newInstance().setStamp(encoder.encode(access.getStamp()));
    }

}
