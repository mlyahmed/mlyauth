package com.hohou.federation.idp.hooks;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component("SPUrlAuthenticationFailureHandler")
public class SPUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public SPUrlAuthenticationFailureHandler() {
        setUseForward(true);
        setDefaultFailureUrl("/401.html");
    }
}
