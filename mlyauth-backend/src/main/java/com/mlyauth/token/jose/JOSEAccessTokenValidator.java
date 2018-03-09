package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.exception.InvalidTokenException;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

import static org.springframework.util.Assert.notNull;

public class JOSEAccessTokenValidator {
    public boolean validate(JOSEAccessToken access) {
        notNull(access, "The token is null");
        assertClaimNotBlank(access.getId(), "The token Id is blank");
        assertClaimNotBlank(access.getSubject(), "The token Subject is blank");
        assertScopesNotBlank(access.getScopes(), "The token scopes list is blank");
        assertClaimNotBlank(access.getBP(), "The token BP is blank");
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
