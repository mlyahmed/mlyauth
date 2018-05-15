package com.primasolutions.idp.sp.jose;

import com.google.common.base.Stopwatch;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.constants.Direction;
import com.primasolutions.idp.constants.TokenPurpose;
import com.primasolutions.idp.constants.TokenStatus;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.exception.JOSEErrorException;
import com.primasolutions.idp.navigation.Navigation;
import com.primasolutions.idp.navigation.NavigationAttribute;
import com.primasolutions.idp.navigation.NavigationDAO;
import com.primasolutions.idp.token.Token;
import com.primasolutions.idp.token.TokenDAO;
import com.primasolutions.idp.token.TokenMapper;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
import com.primasolutions.idp.token.jose.JOSEAccessTokenValidator;
import com.primasolutions.idp.token.jose.JOSETokenDecoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class SPJOSEProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPJOSEProcessingFilter.class);

    private static final String BEARER_PREFIX = "Bearer";

    @Autowired
    private JOSETokenDecoder tokenDecoder;

    @Autowired
    private IContext context;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private JOSEAccessTokenValidator accessTokenValidator;

    public static final String FILTER_URL = "/sp/jose/sso";

    public SPJOSEProcessingFilter() {
        this(FILTER_URL);
    }

    protected SPJOSEProcessingFilter(final String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setFilterProcessesUrl(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationException {

        final Stopwatch started = Stopwatch.createStarted();
        try {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
                return null;
            }

            final JOSEAccessToken token = tokenDecoder.decodeAccess(getRawBearer(request), AspectType.IDP_JOSE);
            accessTokenValidator.validate(token);

            if (!"SSO".equals(token.getBP()))
                throw JOSEErrorException.newInstance("The Token BP must be SSO");

            if (!getFullURL(request).equals(token.getTargetURL()))
                throw JOSEErrorException.newInstance("The Token Target URL does not match");

            final Authentication auth = getAuthenticationManager().authenticate(new JOSEAuthenticationToken(token));
            traceNavigation(request, started, token);
            return auth;

        } catch (Exception e) {
            LOGGER.error("Incoming JOSE token is invalid", e);
            throw new AuthenticationServiceException("Incoming JOSE token is invalid", e);
        }
    }

    private String getAuthorizationHeader(final HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private String getRawBearer(final HttpServletRequest request) {
        final String asHeader = getAuthorizationHeader(request);
        final String asForm = request.getParameter(BEARER_PREFIX);
        if (asHeader != null && asHeader.startsWith(BEARER_PREFIX + " "))
            return asHeader.substring((BEARER_PREFIX + " ").length());
        else if (StringUtils.isNotBlank(asForm))
            return asForm;
        else
            throw JOSEErrorException.newInstance();
    }

    public String getFullURL(final HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        return request.getQueryString() == null
                ? requestURL.toString() : requestURL.append('?').append(request.getQueryString()).toString();
    }

    private void traceNavigation(final HttpServletRequest req, final Stopwatch started, final JOSEAccessToken token) {
        final Navigation navigation = Navigation.newInstance()
                .setCreatedAt(new Date())
                .setDirection(Direction.INBOUND)
                .setTargetURL(token.getTargetURL())
                .setToken(saveToken(token, req))
                .setSession(this.context.getAuthenticationSession());

        navigation.setAttributes(buildAttributes(req));
        navigation.setTimeConsumed(started.elapsed(TimeUnit.MILLISECONDS));
        navigationDAO.save(navigation);
    }

    private Token saveToken(final JOSEAccessToken accessToken, final HttpServletRequest request) {
        Token token = tokenMapper.toToken(accessToken);
        token.setPurpose(TokenPurpose.NAVIGATION).setSession(context.getAuthenticationSession());
        token.setStatus(TokenStatus.CHECKED);
        token.setChecksum(DigestUtils.sha256Hex(getRawBearer(request)));
        token = tokenDAO.save(token);
        return token;
    }

    private HashSet<NavigationAttribute> buildAttributes(final HttpServletRequest request) {
        return new HashSet<>(asList(NavigationAttribute.newInstance()
                .setCode(BEARER_PREFIX)
                .setAlias(BEARER_PREFIX)
                .setValue(getRawBearer(request))));
    }
}
