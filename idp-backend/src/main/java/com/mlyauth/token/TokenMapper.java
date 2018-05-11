package com.mlyauth.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.domain.Token;
import com.mlyauth.domain.TokenClaim;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.domain.TokenClaim.newInstance;
import static com.mlyauth.token.Claims.ACTION;
import static com.mlyauth.token.Claims.APPLICATION;
import static com.mlyauth.token.Claims.AUDIENCE;
import static com.mlyauth.token.Claims.BP;
import static com.mlyauth.token.Claims.CLIENT_ID;
import static com.mlyauth.token.Claims.CLIENT_PROFILE;
import static com.mlyauth.token.Claims.DELEGATE;
import static com.mlyauth.token.Claims.DELEGATOR;
import static com.mlyauth.token.Claims.ENTITY_ID;
import static com.mlyauth.token.Claims.ISSUER;
import static com.mlyauth.token.Claims.SCOPES;
import static com.mlyauth.token.Claims.STATE;
import static com.mlyauth.token.Claims.SUBJECT;
import static com.mlyauth.token.Claims.TARGET_URL;
import static com.mlyauth.token.Claims.VERDICT;

@Component
public class TokenMapper {

    public Token toToken(IToken token) {
        if (token == null) return null;
        return mapToken(token).setClaims(mapClaims(token));
    }

    private Token mapToken(IToken token) {
        return Token.newInstance()
                .setValidationMode(token.getValidationMode()) //TODO: To be covered
                .setRefreshMode(token.getRefreshMode()) //TODO: To be covered
                .setStamp(DigestUtils.sha256Hex(token.getStamp()))
                .setIssuanceTime(toDate(token.getIssuanceTime()))
                .setEffectiveTime(toDate(token.getEffectiveTime()))
                .setExpiryTime(toDate(token.getExpiryTime()))
                .setType(token.getType())
                .setNorm(token.getNorm());
    }

    private HashSet<TokenClaim> mapClaims(IToken token) {
        final HashSet<TokenClaim> claims = new HashSet<>();
        if (StringUtils.isNotBlank(token.getSubject()))
            claims.add(newInstance().setCode(SUBJECT.getValue()).setValue(token.getSubject()));

        if (CollectionUtils.isNotEmpty(token.getScopes()))
            claims.add(newInstance().setCode(SCOPES.getValue()).setValue(compactScopes(token.getScopes())));

        if (StringUtils.isNotBlank(token.getBP()))
            claims.add(newInstance().setCode(BP.getValue()).setValue(token.getBP()));

        if (StringUtils.isNotBlank(token.getState()))
            claims.add(newInstance().setCode(STATE.getValue()).setValue(token.getState()));

        if (StringUtils.isNotBlank(token.getIssuer()))
            claims.add(newInstance().setCode(ISSUER.getValue()).setValue(token.getIssuer()));

        if (StringUtils.isNotBlank(token.getAudience()))
            claims.add(newInstance().setCode(AUDIENCE.getValue()).setValue(token.getAudience()));

        if (StringUtils.isNotBlank(token.getTargetURL()))
            claims.add(newInstance().setCode(TARGET_URL.getValue()).setValue(token.getTargetURL()));

        if (StringUtils.isNotBlank(token.getDelegator()))
            claims.add(newInstance().setCode(DELEGATOR.getValue()).setValue(token.getDelegator()));

        if (StringUtils.isNotBlank(token.getDelegate()))
            claims.add(newInstance().setCode(DELEGATE.getValue()).setValue(token.getDelegate()));

        if (token.getVerdict() != null)
            claims.add(newInstance().setCode(VERDICT.getValue()).setValue(token.getVerdict().name()));

        if (StringUtils.isNotBlank(token.getClaim(CLIENT_ID.getValue())))
            claims.add(newInstance().setCode(CLIENT_ID.getValue()).setValue(token.getClaim(CLIENT_ID.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(CLIENT_PROFILE.getValue())))
            claims.add(newInstance().setCode(CLIENT_PROFILE.getValue()).setValue(token.getClaim(CLIENT_PROFILE.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(ENTITY_ID.getValue())))
            claims.add(newInstance().setCode(ENTITY_ID.getValue()).setValue(token.getClaim(ENTITY_ID.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(ACTION.getValue())))
            claims.add(newInstance().setCode(ACTION.getValue()).setValue(token.getClaim(ACTION.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(APPLICATION.getValue())))
            claims.add(newInstance().setCode(APPLICATION.getValue()).setValue(token.getClaim(APPLICATION.getValue())));

        return claims;
    }


    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String compactScopes(Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }
}
