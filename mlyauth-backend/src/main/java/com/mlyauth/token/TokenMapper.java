package com.mlyauth.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.domain.Token;
import com.mlyauth.domain.TokenClaim;
import com.mlyauth.token.saml.SAMLAccessToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.token.IDPClaims.*;

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
        if (StringUtils.isNotBlank(access.getSubject()))
            claims.add(TokenClaim.newInstance().setCode(SUBJECT.getValue()).setValue(access.getSubject()));

        if (CollectionUtils.isNotEmpty(access.getScopes()))
            claims.add(TokenClaim.newInstance().setCode(SCOPES.getValue()).setValue(compactScopes(access.getScopes())));

        if (StringUtils.isNotBlank(access.getBP()))
            claims.add(TokenClaim.newInstance().setCode(BP.getValue()).setValue(access.getBP()));

        if (StringUtils.isNotBlank(access.getState()))
            claims.add(TokenClaim.newInstance().setCode(STATE.getValue()).setValue(access.getState()));

        if (StringUtils.isNotBlank(access.getIssuer()))
            claims.add(TokenClaim.newInstance().setCode(ISSUER.getValue()).setValue(access.getIssuer()));

        if (StringUtils.isNotBlank(access.getAudience()))
            claims.add(TokenClaim.newInstance().setCode(AUDIENCE.getValue()).setValue(access.getAudience()));

        if (StringUtils.isNotBlank(access.getTargetURL()))
            claims.add(TokenClaim.newInstance().setCode(TARGET_URL.getValue()).setValue(access.getTargetURL()));

        if (StringUtils.isNotBlank(access.getDelegator()))
            claims.add(TokenClaim.newInstance().setCode(DELEGATOR.getValue()).setValue(access.getDelegator()));

        if (StringUtils.isNotBlank(access.getDelegate()))
            claims.add(TokenClaim.newInstance().setCode(DELEGATE.getValue()).setValue(access.getDelegate()));

        if (access.getVerdict() != null)
            claims.add(TokenClaim.newInstance().setCode(VERDICT.getValue()).setValue(access.getVerdict().name()));

        return claims;
    }


    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String compactScopes(Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }
}
