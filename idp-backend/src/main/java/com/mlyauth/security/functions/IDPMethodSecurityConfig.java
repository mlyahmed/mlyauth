package com.mlyauth.security.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.List;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class IDPMethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    private IDPMethodSecurityExpressionHandler securityExpressionHandler = new IDPMethodSecurityExpressionHandler();

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return securityExpressionHandler;
    }

    @Autowired(required = false)
    public void setAuthenticationTrustResolver(final AuthenticationTrustResolver trustResolver) {
        securityExpressionHandler.setTrustResolver(trustResolver);
    }

    @Autowired(required = false)
    public void setPermissionEvaluator(final List<PermissionEvaluator> permissionEvaluators) {
        if (permissionEvaluators.size() != 1) {
            logger.debug("Not autwiring PermissionEvaluator since size != 1. Got " + permissionEvaluators);
        }
        securityExpressionHandler.setPermissionEvaluator(permissionEvaluators.get(0));
    }
}
