package com.primasolutions.idp.token.jose;

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
    public JOSEAccessToken newAccessToken(final PrivateKey prKey, final PublicKey pbKey) {
        return new JOSEAccessToken(prKey, pbKey);
    }

    @Bean("JOSEAccessToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSEAccessToken newAccessToken(final String ser, final PrivateKey prKey, final PublicKey pbKey) {
        return new JOSEAccessToken(ser, prKey, pbKey);
    }

    @Bean("JOSERefreshToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSERefreshToken newRefreshToken(final PrivateKey prKey, final PublicKey pbKey) {
        return new JOSERefreshToken(prKey, pbKey);
    }

    @Bean("JOSERefreshToken")
    @Lazy
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JOSERefreshToken newRefreshToken(final String ser, final PrivateKey prKey, final PublicKey pbKey) {
        return new JOSERefreshToken(ser, prKey, pbKey);
    }

}
