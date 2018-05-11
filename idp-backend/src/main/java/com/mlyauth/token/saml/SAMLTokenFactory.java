package com.mlyauth.token.saml;

import org.opensaml.xml.security.credential.Credential;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
public class SAMLTokenFactory {

    @Bean("SAMLAccessToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SAMLAccessToken createAccessToken(final Credential credential) {
        return new SAMLAccessToken(credential);
    }

    @Bean("SAMLAccessToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SAMLAccessToken createAccessToken(final String serialized, final Credential credential) {
        return new SAMLAccessToken(serialized, credential);
    }

}
