package com.mlyauth.sso.sp.jose;

import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSEHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.mlyauth.constants.AspectType.IDP_JOSE;

public class SPJOSEProcessingFilter extends AbstractAuthenticationProcessingFilter {
    protected final static Logger logger = LoggerFactory.getLogger(SPJOSEProcessingFilter.class);

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private JOSEHelper joseHelper;


    public static final String FILTER_URL = "/sp/jose/sso";

    public SPJOSEProcessingFilter() {
        this(FILTER_URL);
    }

    protected SPJOSEProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setFilterProcessesUrl(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {

            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer "))
                throw JOSEErrorException.newInstance();
            final JOSEAccessToken accessToken = reconstituteAccessToken(header);
            //validate token
            return getAuthenticationManager().authenticate(new JOSEAuthenticationToken(accessToken));

        } catch (Exception e) {
            logger.debug("Incoming JOSE token is invalid", e);
            throw new AuthenticationServiceException("Incoming JOSE token is invalid", e);
        }
    }

    private JOSEAccessToken reconstituteAccessToken(String header) {
        final JOSEAccessToken token = new JOSEAccessToken(getToken(header), localKey(), peerKey(header));
        token.decipher();
        return token;
    }

    private PrivateKey localKey() {
        return credentialManager.getLocalPrivateKey();
    }

    private PublicKey peerKey(String header) {
        return credentialManager.getPeerKey(issuer(header), IDP_JOSE);
    }

    private String issuer(String header) {
        return joseHelper.loadIssuer(getToken(header), localKey());
    }

    private String getToken(String header) {
        return header.substring(7);
    }



}
