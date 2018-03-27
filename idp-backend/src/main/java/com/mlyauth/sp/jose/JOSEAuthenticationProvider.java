package com.mlyauth.sp.jose;

import com.mlyauth.context.IDPUser;
import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

public class JOSEAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private SPJOSEUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Only JOSEAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
        }

        final JOSEAccessToken credentials = ((JOSEAuthenticationToken) authentication).getCredentials();

        if (credentials == null)
            throw new AuthenticationServiceException("The JOSE Token is not available in the authentication token");

        IDPUser user = userDetailsService.loadUserByJOSE(credentials);
        Object principal = user.getUsername();
        Collection<? extends GrantedAuthority> entitlements = user.getAuthorities();
        final Date expiration = Date.from(credentials.getExpiryTime().atZone(ZoneId.systemDefault()).toInstant());
        ExpiringUsernameAuthenticationToken result = new ExpiringUsernameAuthenticationToken(expiration, principal, credentials, entitlements);
        result.setDetails(user);

        return result;
    }

    public boolean supports(Class aClass) {
        return JOSEAuthenticationToken.class.isAssignableFrom(aClass);
    }

}
