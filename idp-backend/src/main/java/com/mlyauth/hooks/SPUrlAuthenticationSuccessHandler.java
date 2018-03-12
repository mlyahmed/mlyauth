package com.mlyauth.hooks;

import com.mlyauth.context.IContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.mlyauth.token.IDPClaims.APPLICATION;

@Component("SPUrlAuthenticationSuccessHandler")
public class SPUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private IContext context;

    public SPUrlAuthenticationSuccessHandler() {
        this.setDefaultTargetUrl("/home.html");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (context.getAttribute(APPLICATION.getValue()) != null) {
            getRedirectStrategy().sendRedirect(request, response, "/navigate/forward/to/" + context.getAttribute(APPLICATION.getValue()));
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }

    }

}
