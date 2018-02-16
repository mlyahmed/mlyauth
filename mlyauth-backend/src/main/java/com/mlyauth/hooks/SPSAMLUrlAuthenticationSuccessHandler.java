package com.mlyauth.hooks;

import com.mlyauth.security.context.IContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.mlyauth.beans.AttributeBean.SAML_RESPONSE_APP;

public class SPSAMLUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private IContext context;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (context.getAttribute(SAML_RESPONSE_APP.getCode()) != null) {
            getRedirectStrategy().sendRedirect(request, response, "/navigate/saml/to/" + context.getAttribute(SAML_RESPONSE_APP.getCode()));
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }


    }

}
