package com.mlyauth.security.token;

import org.opensaml.saml2.core.Response;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
public class TokenFactory {

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IDPToken<Response> createFreshSAMLResponseToken(Credential credential) {
        return new SAMLResponseToken(credential);
    }

}
