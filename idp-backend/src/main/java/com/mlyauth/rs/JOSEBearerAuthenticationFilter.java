package com.mlyauth.rs;

import com.mlyauth.sp.jose.JOSEAuthenticationToken;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSETokenDecoder;
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
    private static Logger logger = LoggerFactory.getLogger(JOSEBearerAuthenticationFilter.class);

    public static final String FILTER_URL = "/domain/**";

    private RequestMatcher authenticationRequestMatcher;

    @Autowired
    private JOSETokenDecoder tokenDecoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public JOSEBearerAuthenticationFilter(){
        authenticationRequestMatcher = new AntPathRequestMatcher(FILTER_URL);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!requiresAuthentication(request)) {
            chain.doFilter(request, response);
            return;
        }

        try {

            if(StringUtils.isNotBlank(getRawBearer(request))) {
                final JOSEAccessToken accessToken = tokenDecoder.decodeAccess(getRawBearer(request));
                final JOSEAuthenticationToken authToken = new JOSEAuthenticationToken(accessToken);
                final Authentication authenticate = authenticationManager.authenticate(authToken);
                SecurityContextHolder.getContext().setAuthentication(authenticate);
            }
            chain.doFilter(request, response);

        }catch(Exception e){
            logger.error("Error JOSE Bearer authentication : ", e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value());
        }

    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        return authenticationRequestMatcher.matches(request);
    }

    private String getRawBearer(HttpServletRequest request) {
        final String header = getAuthorizationHeader(request);
        if (header != null && header.startsWith("Bearer "))
            return header.substring(7);
        else
            return null;
    }

    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}