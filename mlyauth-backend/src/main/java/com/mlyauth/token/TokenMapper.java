package com.mlyauth.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.domain.Token;
import com.mlyauth.domain.TokenClaim;
import com.mlyauth.token.saml.SAMLAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.token.IDPClaims.SCOPES;
import static com.mlyauth.token.IDPClaims.SUBJECT;

@Component
public class TokenMapper {

    @Autowired
    private PasswordEncoder encoder;

    public Token toToken(SAMLAccessToken access) {
        if (access == null) return null;
        return mapToken(access).setClaims(mapClaims(access));
    }

    private Token mapToken(SAMLAccessToken access) {
        return Token.newInstance()
                .setStamp(encoder.encode(access.getStamp()))
                .setIssuanceTime(toDate(access.getIssuanceTime()))
                .setEffectiveTime(toDate(access.getEffectiveTime()))
                .setExpiryTime(toDate(access.getExpiryTime()))
                .setType(access.getType())
                .setNorm(access.getNorm());
    }

    private HashSet<TokenClaim> mapClaims(SAMLAccessToken access) {
        final HashSet<TokenClaim> claims = new HashSet<>();
        claims.add(TokenClaim.newInstance().setCode(SUBJECT.getValue()).setValue(access.getSubject()));
        claims.add(TokenClaim.newInstance().setCode(SCOPES.getValue()).setValue(compactScopes(access.getScopes())));
        return claims;
    }


    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String compactScopes(Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }
}
