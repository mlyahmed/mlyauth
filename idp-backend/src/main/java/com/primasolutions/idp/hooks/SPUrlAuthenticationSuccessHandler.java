package com.primasolutions.idp.hooks;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.authentication.Role;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.navigation.AutoNavigation;
import com.primasolutions.idp.navigation.AutoNavigationDAO;
import com.primasolutions.idp.token.Claims;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.net.InternetDomainName.from;

@Component("SPUrlAuthenticationSuccessHandler")
public class SPUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPUrlAuthenticationSuccessHandler.class);
    public static final int MAX_AGE = 60 * 60 * 24;

    @Autowired
    private IContext context;

    @Autowired
    private AutoNavigationDAO autoNavigationDAO;

    public SPUrlAuthenticationSuccessHandler() {
        this.setDefaultTargetUrl("/home.html");
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException, ServletException {

        Cookie idToken = new Cookie("IDToken", "red");
        idToken.setDomain("idp.localcloud.mlyahmed.net");
        idToken.setMaxAge(MAX_AGE);
        idToken.setPath("/");
        response.addCookie(idToken);

        LOGGER.info("Cookie domain : " + idToken.getDomain());
        LOGGER.info("Cookie added : " + idToken.getMaxAge());

        if (StringUtils.isNotBlank(context.getAttribute(Claims.APPLICATION.getValue()))) {
            sendToTarget(request, response, context.getAttribute(Claims.APPLICATION.getValue()));
        } else if (getDefaultTarget() != null) {
            sendToTarget(request, response, getDefaultTarget().getAppname());
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }

    }

    private String domain(HttpServletRequest request) {
        return from(request.getServerName()).hasPublicSuffix()
                ? from(request.getServerName()).topPrivateDomain().toString()
                : request.getServerName();
    }

    private Application getDefaultTarget() {
        final Role role = context.getPerson().getRole();
        final AutoNavigation autoNavigation = autoNavigationDAO.findByRole(role);
        if (autoNavigation == null) return null;
        return context.getPerson().getApplications().stream()
                .filter(app -> app.getType().getCode() == autoNavigation.getApplicationType().getCode())
                .findFirst().orElse(null);
    }

    private void sendToTarget(final HttpServletRequest req, final HttpServletResponse res, final String appname)
            throws IOException {
        getRedirectStrategy().sendRedirect(req, res, "/navigate/forward/to/" + appname);
    }
}
