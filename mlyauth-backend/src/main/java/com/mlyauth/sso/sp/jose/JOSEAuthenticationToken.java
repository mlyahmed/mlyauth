package com.mlyauth.sso.sp.jose;

import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

public class JOSEAuthenticationToken extends AbstractAuthenticationToken {

    private JOSEAccessToken credentials;

    public JOSEAuthenticationToken(JOSEAccessToken credentials) {
        super(null);
        Assert.notNull(credentials, "JOSEAuthenticationToken requires the credentials parameter to be set");
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
