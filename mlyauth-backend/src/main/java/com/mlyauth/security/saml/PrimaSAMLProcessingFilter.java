package com.mlyauth.security.saml;

import org.opensaml.common.SAMLRuntimeException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PrimaSAMLProcessingFilter extends SAMLProcessingFilter {


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {

            if (!"POST".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
                return null;
            } else {
                return super.attemptAuthentication(request, response);
            }

        } catch (IOException e) {
            throw new SAMLRuntimeException(e);
        } catch (Exception e) {
            throw e;
        }
    }

}
