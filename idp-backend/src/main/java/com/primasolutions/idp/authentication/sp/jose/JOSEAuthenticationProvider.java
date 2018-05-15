package com.primasolutions.idp.authentication.sp.jose;

import com.primasolutions.idp.context.IDPUser;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
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
    public Authentication authenticate(final Authentication auth) throws AuthenticationException {

        if (!supports(auth.getClass())) {
            final String errorMsg = "Only JOSEAuthenticationToken is supported, " + auth.getClass() + " was attempted";
            throw new IllegalArgumentException(errorMsg);
        }

        final JOSEAccessToken cred = ((JOSEAuthenticationToken) auth).getCredentials();

        if (cred == null)
            throw new AuthenticationServiceException("The JOSE Token is not available in the authentication token");

        IDPUser user = userDetailsService.loadUserByJOSE(cred);
        Object prs = user.getUsername();
        Collection<? extends GrantedAuthority> grants = user.getAuthorities();
        final Date exp = Date.from(cred.getExpiryTime().atZone(ZoneId.systemDefault()).toInstant());
        ExpiringUsernameAuthenticationToken rs = new ExpiringUsernameAuthenticationToken(exp, prs, cred, grants);
        rs.setDetails(user);

        return rs;
    }

    public boolean supports(final Class aClass) {
        return JOSEAuthenticationToken.class.isAssignableFrom(aClass);
    }

}
