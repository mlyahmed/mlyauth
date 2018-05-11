package com.mlyauth.sp.jose;

import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import static org.springframework.util.Assert.notNull;

public class JOSEAuthenticationToken extends AbstractAuthenticationToken {

    private JOSEAccessToken credentials;

    public JOSEAuthenticationToken(final JOSEAccessToken credentials) {
        super(null);
        notNull(credentials, "JOSEAuthenticationToken requires the credentials parameter to be set");
        this.credentials = credentials;
        setAuthenticated(false);
    }

    @Override
    public JOSEAccessToken getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
