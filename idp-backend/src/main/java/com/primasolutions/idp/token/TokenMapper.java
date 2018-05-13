package com.primasolutions.idp.token;

import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.domain.Token;
import com.primasolutions.idp.domain.TokenClaim;
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

@Component
public class TokenMapper {

    public Token toToken(final IToken token) {
        if (token == null) return null;
        return mapToken(token).setClaims(mapClaims(token));
    }

    private Token mapToken(final IToken token) {
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

    //CHECKSTYLE:OFF
    private HashSet<TokenClaim> mapClaims(final IToken token) {
        final HashSet<TokenClaim> claims = new HashSet<>();
        if (StringUtils.isNotBlank(token.getSubject()))
            claims.add(TokenClaim.newInstance().setCode(Claims.SUBJECT.getValue()).setValue(token.getSubject()));

        if (CollectionUtils.isNotEmpty(token.getScopes()))
            claims.add(TokenClaim.newInstance().setCode(Claims.SCOPES.getValue()).setValue(compactScopes(token.getScopes())));

        if (StringUtils.isNotBlank(token.getBP()))
            claims.add(TokenClaim.newInstance().setCode(Claims.BP.getValue()).setValue(token.getBP()));

        if (StringUtils.isNotBlank(token.getState()))
            claims.add(TokenClaim.newInstance().setCode(Claims.STATE.getValue()).setValue(token.getState()));

        if (StringUtils.isNotBlank(token.getIssuer()))
            claims.add(TokenClaim.newInstance().setCode(Claims.ISSUER.getValue()).setValue(token.getIssuer()));

        if (StringUtils.isNotBlank(token.getAudience()))
            claims.add(TokenClaim.newInstance().setCode(Claims.AUDIENCE.getValue()).setValue(token.getAudience()));

        if (StringUtils.isNotBlank(token.getTargetURL()))
            claims.add(TokenClaim.newInstance().setCode(Claims.TARGET_URL.getValue()).setValue(token.getTargetURL()));

        if (StringUtils.isNotBlank(token.getDelegator()))
            claims.add(TokenClaim.newInstance().setCode(Claims.DELEGATOR.getValue()).setValue(token.getDelegator()));

        if (StringUtils.isNotBlank(token.getDelegate()))
            claims.add(TokenClaim.newInstance().setCode(Claims.DELEGATE.getValue()).setValue(token.getDelegate()));

        if (token.getVerdict() != null)
            claims.add(TokenClaim.newInstance().setCode(Claims.VERDICT.getValue()).setValue(token.getVerdict().name()));

        if (StringUtils.isNotBlank(token.getClaim(Claims.CLIENT_ID.getValue())))
            claims.add(TokenClaim.newInstance().setCode(Claims.CLIENT_ID.getValue()).setValue(token.getClaim(Claims.CLIENT_ID.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(Claims.CLIENT_PROFILE.getValue())))
            claims.add(TokenClaim.newInstance().setCode(Claims.CLIENT_PROFILE.getValue())
                    .setValue(token.getClaim(Claims.CLIENT_PROFILE.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(Claims.ENTITY_ID.getValue())))
            claims.add(TokenClaim.newInstance().setCode(Claims.ENTITY_ID.getValue()).setValue(token.getClaim(Claims.ENTITY_ID.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(Claims.ACTION.getValue())))
            claims.add(TokenClaim.newInstance().setCode(Claims.ACTION.getValue()).setValue(token.getClaim(Claims.ACTION.getValue())));

        if (StringUtils.isNotBlank(token.getClaim(Claims.APPLICATION.getValue())))
            claims.add(TokenClaim.newInstance().setCode(Claims.APPLICATION.getValue()).setValue(token.getClaim(Claims.APPLICATION.getValue())));

        return claims;
    }
    //CHECKSTYLE:ON

    private Date toDate(final LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String compactScopes(final Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }
}
