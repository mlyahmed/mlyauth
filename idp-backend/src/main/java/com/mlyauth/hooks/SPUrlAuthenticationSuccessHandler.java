package com.mlyauth.hooks;

import com.mlyauth.context.IContext;
import com.mlyauth.dao.AutoNavigationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AutoNavigation;
import com.mlyauth.domain.Role;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.mlyauth.token.Claims.APPLICATION;

@Component("SPUrlAuthenticationSuccessHandler")
public class SPUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private IContext context;

    @Autowired
    private AutoNavigationDAO autoNavigationDAO;

    public SPUrlAuthenticationSuccessHandler() {
        this.setDefaultTargetUrl("/home.html");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (StringUtils.isNotBlank(context.getAttribute(APPLICATION.getValue()))) {

            sendToTarget(request, response, context.getAttribute(APPLICATION.getValue()));

        } else if(getDefaultTarget() != null){

            sendToTarget(request, response, getDefaultTarget().getAppname());

        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }

    }

    private Application getDefaultTarget(){
        final Role role = context.getPerson().getRole();
        final AutoNavigation autoNavigation = autoNavigationDAO.findByRole(role);
        if(autoNavigation == null) return null;
        return context.getPerson().getApplications().stream()
                .filter(app -> app.getType() == autoNavigation.getApplicationType())
                .findFirst().orElse(null);
    }

    private void sendToTarget(HttpServletRequest request, HttpServletResponse response, String appname) throws IOException {
        getRedirectStrategy().sendRedirect(request, response, "/navigate/forward/to/" + appname);
    }
}
