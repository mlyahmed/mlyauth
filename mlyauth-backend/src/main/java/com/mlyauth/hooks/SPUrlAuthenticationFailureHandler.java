package com.mlyauth.hooks;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component("SPUrlAuthenticationFailureHandler")
public class SPUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public SPUrlAuthenticationFailureHandler() {
        setUseForward(true);
        setDefaultFailureUrl("/error.html");
    }
}
