package com.primasolutions.idp.rs;

import com.primasolutions.idp.sp.jose.JOSEAuthenticationToken;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
import com.primasolutions.idp.token.jose.JOSETokenDecoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JOSEBearerAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JOSEBearerAuthenticationFilter.class);

    public static final String FILTER_URL = "/domain/**";
    public static final String BEARER_PREFIX = "Bearer ";

    private RequestMatcher authenticationRequestMatcher;

    @Autowired
    private JOSETokenDecoder tokenDecoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public JOSEBearerAuthenticationFilter() {
        authenticationRequestMatcher = new AntPathRequestMatcher(FILTER_URL);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest req, final HttpServletResponse res, final FilterChain ch)
            throws IOException, ServletException {

        if (!requiresAuthentication(req)) {
            ch.doFilter(req, res);
            return;
        }

        try {

            if (StringUtils.isNotBlank(getRawBearer(req))) {
                final JOSEAccessToken accessToken = tokenDecoder.decodeAccess(getRawBearer(req));
                final JOSEAuthenticationToken authToken = new JOSEAuthenticationToken(accessToken);
                final Authentication authenticate = authenticationManager.authenticate(authToken);
                SecurityContextHolder.getContext().setAuthentication(authenticate);
            }
            ch.doFilter(req, res);

        } catch (Exception e) {
            LOGGER.error("Error JOSE Bearer authentication : ", e);
            SecurityContextHolder.clearContext();
            res.sendError(HttpStatus.UNAUTHORIZED.value());
        }

    }

    private boolean requiresAuthentication(final HttpServletRequest req) {
        return authenticationRequestMatcher.matches(req);
    }

    private String getRawBearer(final HttpServletRequest req) {
        final String header = getAuthorizationHeader(req);
        if (header != null && header.startsWith(BEARER_PREFIX))
            return header.substring(BEARER_PREFIX.length());
        else
            return null;
    }

    private String getAuthorizationHeader(final HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
