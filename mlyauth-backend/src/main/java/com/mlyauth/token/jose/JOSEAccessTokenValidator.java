package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.exception.InvalidTokenException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.util.Assert.notNull;

@Component
public class JOSEAccessTokenValidator {

    @Value("${sp.jose.entityId}")
    private String localEntityId;


    public boolean validate(JOSEAccessToken access) {
        notNull(access, "The token is null");
        assertClaimNotBlank(access.getId(), "The token Id is blank");
        assertClaimNotBlank(access.getSubject(), "The token Subject is blank");
        assertScopesNotBlank(access.getScopes(), "The token scopes list is blank");
        assertClaimNotBlank(access.getBP(), "The token BP is blank");
        assertClaimNotBlank(access.getState(), "The token State is blank");
        assertClaimNotBlank(access.getIssuer(), "The token Issuer is blank");
        assertClaimNotBlank(access.getTargetURL(), "The token target URL is blank");
        assertClaimNotBlank(access.getDelegator(), "The token delegator is blank");
        assertClaimNotBlank(access.getDelegate(), "The token delegate is blank");

        if (access.getVerdict() == null)
            throw InvalidTokenException.newInstance("The token verdict is not acceptable");

        if (!localEntityId.equalsIgnoreCase(access.getAudience()))
            throw InvalidTokenException.newInstance("The token audience is not acceptable");

        return true;
    }


    private void assertClaimNotBlank(String claim, String message) {
        if (StringUtils.isBlank(claim))
            throw InvalidTokenException.newInstance(message);
    }

    private void assertScopesNotBlank(Set<TokenScope> scopes, String message) {
        if (scopes == null || scopes.isEmpty())
            throw InvalidTokenException.newInstance(message);
    }
}
