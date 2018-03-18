package com.mlyauth.token.jose;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class JOSETokenFactory {

    @Bean("JOSEAccessToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSEAccessToken createAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        return new JOSEAccessToken(privateKey, publicKey);
    }

    @Bean("JOSEAccessToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSEAccessToken createAccessToken(String seialized, PrivateKey privateKey, PublicKey publicKey) {
        return new JOSEAccessToken(seialized, privateKey, publicKey);
    }

    @Bean("JOSERefreshToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSERefreshToken createRefreshToken(PrivateKey privateKey, PublicKey publicKey) {
        return new JOSERefreshToken(privateKey, publicKey);
    }

    @Bean("JOSERefreshToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSERefreshToken createRefreshToken(String serialized, PrivateKey privateKey, PublicKey publicKey) {
        return new JOSERefreshToken(serialized, privateKey, publicKey);
    }

}
