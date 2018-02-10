package com.mlyauth.security.sso.idp.saml.response;

import com.mlyauth.domain.Application;
import org.springframework.util.Assert;

public class IDPSAMLResponseGenerator {

    public void generate(Application app) {
        Assert.notNull(app, "The application parameter is null");
    }

}
