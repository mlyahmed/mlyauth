package com.hohou.federation.idp.token.jose;

import com.hohou.federation.idp.constants.TokenVerdict;
import com.hohou.federation.idp.exception.InvalidTokenExc;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.springframework.util.Assert.notNull;

@Component
public class JOSEAccessTokenValidator {

    @Value("${sp.jose.entityId}")
    protected String localEntityId;


    public boolean validate(final JOSEAccessToken access) {
        notNull(access, "The token is null");
        assertClaimNotBlank(access.getStamp(), "The token Id is blank");
        assertClaimNotBlank(access.getSubject(), "The token Subject is blank");
        assertClaimNotBlank(access.getBP(), "The token BP is blank");
        assertClaimNotBlank(access.getIssuer(), "The token Issuer is blank");
        assertClaimNotBlank(access.getTargetURL(), "The token target URL is blank");
        checkVerdict(access);
        checkAudience(access);
        checkExpiryTime(access);
        checkIssuanceTime(access);
        checkEffectiveTime(access);
        return true;
    }


    private void assertClaimNotBlank(final String claim, final String message) {
        if (StringUtils.isBlank(claim))
            throw InvalidTokenExc.newInstance(message);
    }

    private void checkVerdict(final JOSEAccessToken access) {
        if (access.getVerdict() == null || access.getVerdict() == TokenVerdict.FAIL)
            throw InvalidTokenExc.newInstance("The token verdict is not acceptable");
    }

    private void checkAudience(final JOSEAccessToken access) {
        if (!localEntityId.equalsIgnoreCase(access.getAudience()))
            throw InvalidTokenExc.newInstance("The token audience is not acceptable");
    }

    private void checkExpiryTime(final JOSEAccessToken access) {
        if (LocalDateTime.now().isAfter(access.getExpiryTime()))
            throw InvalidTokenExc.newInstance("The token is expired");
    }

    private void checkIssuanceTime(final JOSEAccessToken access) {
        if (LocalDateTime.now().isBefore(access.getIssuanceTime()))
            throw InvalidTokenExc.newInstance("The token issuance time is inconsistency");
    }

    private void checkEffectiveTime(final JOSEAccessToken access) {
        if (LocalDateTime.now().isBefore(access.getEffectiveTime()))
            throw InvalidTokenExc.newInstance("The token effective time is inconsistency");
    }
}
