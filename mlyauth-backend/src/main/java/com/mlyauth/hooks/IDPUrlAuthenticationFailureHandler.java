package com.mlyauth.hooks;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component("IDPUrlAuthenticationFailureHandler")
public class IDPUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public IDPUrlAuthenticationFailureHandler() {
        setUseForward(true);
        setDefaultFailureUrl("/error.html");
    }
}
