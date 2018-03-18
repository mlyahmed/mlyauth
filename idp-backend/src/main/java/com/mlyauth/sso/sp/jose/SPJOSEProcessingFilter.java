package com.mlyauth.sso.sp.jose;

import com.google.common.base.Stopwatch;
import com.mlyauth.constants.TokenPurpose;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.NavigationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Navigation;
import com.mlyauth.domain.NavigationAttribute;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSEAccessTokenValidator;
import com.mlyauth.token.jose.JOSETokenDecoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.mlyauth.constants.AspectType.IDP_JOSE;
import static com.mlyauth.constants.Direction.INBOUND;
import static com.mlyauth.constants.TokenScope.PERSON;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

public class SPJOSEProcessingFilter extends AbstractAuthenticationProcessingFilter {
    protected final static Logger logger = LoggerFactory.getLogger(SPJOSEProcessingFilter.class);

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

            final String rawBearer = getRawBearer(request);

            final JOSEAccessToken accessToken = tokenDecoder.decodeAccess(rawBearer, IDP_JOSE);
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

    private Cookie getBearerCookie(HttpServletRequest request) {
        if(request.getCookies() == null) return null;
        return stream(request.getCookies()).filter(c -> "Bearer".equals(c.getName())).findFirst().orElse(null);
    }

    private String getRawBearer(HttpServletRequest request) {
        final String header = getAuthorizationHeader(request);
        final Cookie bearer = getBearerCookie(request);
        if (header != null && header.startsWith("Bearer "))
            return header.substring(7);
        else if(bearer != null && StringUtils.isNotBlank(bearer.getValue()))
            return bearer.getValue();
        else
            throw JOSEErrorException.newInstance();
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
                .setToken(saveToken(accessToken, request))
                .setSession(this.context.getAuthenticationSession());

        navigation.setAttributes(buildAttributes(request));
        navigation.setTimeConsumed(started.elapsed(TimeUnit.MILLISECONDS));
        navigationDAO.save(navigation);
    }

    private Token saveToken(JOSEAccessToken accessToken, HttpServletRequest request) {
        Token token = tokenMapper.toToken(accessToken);
        token.setPurpose(TokenPurpose.NAVIGATION).setSession(context.getAuthenticationSession());
        token.setStatus(TokenStatus.CHECKED);
        token.setChecksum(DigestUtils.sha256Hex(getRawBearer(request)));
        token = tokenDAO.save(token);
        return token;
    }

    private HashSet<NavigationAttribute> buildAttributes(HttpServletRequest request) {
        return new HashSet<>(asList(NavigationAttribute.newInstance()
                .setCode("Bearer")
                .setAlias("Bearer")
                .setValue(getRawBearer(request))));
    }
}
