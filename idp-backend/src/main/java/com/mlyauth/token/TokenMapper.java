package com.mlyauth.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.domain.Token;
import com.mlyauth.domain.TokenClaim;
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

    public Token toToken(IDPToken token) {
        if (token == null) return null;
        return mapToken(token).setClaims(mapClaims(token));
    }

    private Token mapToken(IDPToken token) {
        return Token.newInstance()
                .setStamp(encoder.encode(token.getStamp()))
                .setIssuanceTime(toDate(token.getIssuanceTime()))
                .setEffectiveTime(toDate(token.getEffectiveTime()))
                .setExpiryTime(toDate(token.getExpiryTime()))
                .setType(token.getType())
                .setNorm(token.getNorm());
    }

    private HashSet<TokenClaim> mapClaims(IDPToken token) {
        final HashSet<TokenClaim> claims = new HashSet<>();
        if (StringUtils.isNotBlank(token.getSubject()))
            claims.add(TokenClaim.newInstance().setCode(SUBJECT.getValue()).setValue(token.getSubject()));

        if (CollectionUtils.isNotEmpty(token.getScopes()))
            claims.add(TokenClaim.newInstance().setCode(SCOPES.getValue()).setValue(compactScopes(token.getScopes())));

        if (StringUtils.isNotBlank(token.getBP()))
            claims.add(TokenClaim.newInstance().setCode(BP.getValue()).setValue(token.getBP()));

        if (StringUtils.isNotBlank(token.getState()))
            claims.add(TokenClaim.newInstance().setCode(STATE.getValue()).setValue(token.getState()));

        if (StringUtils.isNotBlank(token.getIssuer()))
            claims.add(TokenClaim.newInstance().setCode(ISSUER.getValue()).setValue(token.getIssuer()));

        if (StringUtils.isNotBlank(token.getAudience()))
            claims.add(TokenClaim.newInstance().setCode(AUDIENCE.getValue()).setValue(token.getAudience()));

        if (StringUtils.isNotBlank(token.getTargetURL()))
            claims.add(TokenClaim.newInstance().setCode(TARGET_URL.getValue()).setValue(token.getTargetURL()));

        if (StringUtils.isNotBlank(token.getDelegator()))
            claims.add(TokenClaim.newInstance().setCode(DELEGATOR.getValue()).setValue(token.getDelegator()));

        if (StringUtils.isNotBlank(token.getDelegate()))
            claims.add(TokenClaim.newInstance().setCode(DELEGATE.getValue()).setValue(token.getDelegate()));

        if (token.getVerdict() != null)
            claims.add(TokenClaim.newInstance().setCode(VERDICT.getValue()).setValue(token.getVerdict().name()));

        return claims;
    }


    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String compactScopes(Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }
}
