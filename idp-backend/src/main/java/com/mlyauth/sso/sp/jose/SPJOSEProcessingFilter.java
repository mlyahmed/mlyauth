package com.mlyauth.sso.sp.jose;

import com.google.common.base.Stopwatch;
import com.mlyauth.constants.TokenPurpose;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.context.IContext;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.NavigationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Navigation;
import com.mlyauth.domain.NavigationAttribute;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.ITokenFactory;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSEAccessTokenValidator;
import com.mlyauth.token.jose.JOSEHelper;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.mlyauth.constants.AspectType.IDP_JOSE;
import static com.mlyauth.constants.Direction.INBOUND;
import static com.mlyauth.constants.TokenScope.PERSON;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SPJOSEProcessingFilter extends AbstractAuthenticationProcessingFilter {
    protected final static Logger logger = LoggerFactory.getLogger(SPJOSEProcessingFilter.class);

    @Autowired
    private IContext context;

    @Autowired
    private ITokenFactory tokenFactory;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private JOSEHelper joseHelper;

    @Autowired
    private JOSEAccessTokenValidator accessTokenValidator;

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
        final Stopwatch started = Stopwatch.createStarted();
        try {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
                return null;
            }

            checkHeader(getAuthorizationHeader(request));
            final JOSEAccessToken accessToken = reconstituteAccessToken(getAuthorizationHeader(request));
            accessTokenValidator.validate(accessToken);
            checkScope(accessToken);

            if (!"SSO".equals(accessToken.getBP()))
                throw JOSEErrorException.newInstance("The Token BP must be SSO");

            if (!getFullURL(request).equals(accessToken.getTargetURL()))
                throw JOSEErrorException.newInstance("The Token Target URL does not match");

            final Authentication authenticate = getAuthenticationManager().authenticate(new JOSEAuthenticationToken(accessToken));

            traceNavigation(request, started, accessToken);

            return authenticate;

        } catch (Exception e) {
            logger.warn("Incoming JOSE token is invalid", e);
            throw new AuthenticationServiceException("Incoming JOSE token is invalid", e);
        }
    }

    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private void checkHeader(String header) {
        if (header == null || !header.startsWith("Bearer "))
            throw JOSEErrorException.newInstance();
    }

    private JOSEAccessToken reconstituteAccessToken(String header) {
        final JOSEAccessToken token = tokenFactory.createJOSEAccessToken(getToken(header), localKey(), peerKey(header));
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

    private void checkScope(JOSEAccessToken accessToken) {
        if (!CollectionUtils.isEqualCollection(accessToken.getScopes(), new HashSet<TokenScope>(singletonList(PERSON))))
            throw JOSEErrorException.newInstance("The Token scopes list must be [PERSON]");
    }

    public String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        return request.getQueryString() == null
                ? requestURL.toString() : requestURL.append('?').append(request.getQueryString()).toString();
    }

    private void traceNavigation(HttpServletRequest request, Stopwatch started, JOSEAccessToken accessToken) {
        final Navigation navigation = Navigation.newInstance()
                .setCreatedAt(new Date())
                .setDirection(INBOUND)
                .setTargetURL(accessToken.getTargetURL())
                .setToken(saveToken(accessToken))
                .setSession(this.context.getAuthenticationSession());

        navigation.setAttributes(buildAttributes(request));
        navigation.setTimeConsumed(started.elapsed(TimeUnit.MILLISECONDS));
        navigationDAO.save(navigation);
    }

    private Token saveToken(JOSEAccessToken accessToken) {
        Token token = tokenMapper.toToken(accessToken);
        token.setPurpose(TokenPurpose.NAVIGATION).setSession(context.getAuthenticationSession());
        token = tokenDAO.save(token);
        return token;
    }

    private HashSet<NavigationAttribute> buildAttributes(HttpServletRequest request) {
        return new HashSet<>(asList(NavigationAttribute.newInstance()
                .setCode("Bearer")
                .setAlias("Bearer")
                .setValue(getToken(getAuthorizationHeader(request)))));
    }
}
