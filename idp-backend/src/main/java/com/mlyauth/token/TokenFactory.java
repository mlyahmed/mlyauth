package com.mlyauth.token;

import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.saml.SAMLAccessToken;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class TokenFactory implements ITokenFactory {

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Override
    public SAMLAccessToken createSAMLAccessToken(Credential credential) {
        return new SAMLAccessToken(credential);
    }

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Override
    public SAMLAccessToken createSAMLAccessToken(String seialized, Credential credential) {
        return new SAMLAccessToken(seialized, credential);
    }

    @Bean
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Override
    public JOSEAccessToken createJOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        return new JOSEAccessToken(privateKey, publicKey);
    }

}
