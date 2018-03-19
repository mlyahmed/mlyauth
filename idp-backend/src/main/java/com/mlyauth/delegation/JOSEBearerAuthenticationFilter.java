package com.mlyauth.delegation;

import com.mlyauth.sso.sp.jose.JOSEAuthenticationToken;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSETokenDecoder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JOSEBearerAuthenticationFilter extends GenericFilterBean {

    public static final String FILTER_URL = "/domain/**";

    private RequestMatcher requiresAuthenticationRequestMatcher;

    @Autowired
    private JOSETokenDecoder tokenDecoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public JOSEBearerAuthenticationFilter(){
        requiresAuthenticationRequestMatcher = new AntPathRequestMatcher(FILTER_URL);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request)) {
            chain.doFilter(request, response);
            return;
        }

        try {

            final String rawBearer = getRawBearer(request);
            if(StringUtils.isNotBlank(rawBearer)) {
                final JOSEAccessToken accessToken = tokenDecoder.decodeAccess(rawBearer);
                final Authentication authenticate = authenticationManager.authenticate(new JOSEAuthenticationToken(accessToken));
                SecurityContextHolder.getContext().setAuthentication(authenticate);
            }

            chain.doFilter(request, response);
        }catch(Exception e){
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value());
        }

    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        return requiresAuthenticationRequestMatcher.matches(request);
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