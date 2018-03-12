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
    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    private IDPMethodSecurityExpressionHandler methodSecurityExpressionHandler = new IDPMethodSecurityExpressionHandler();

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
        return methodSecurityExpressionHandler;
    }

    @Autowired(required = false)
    public void setAuthenticationTrustResolver(AuthenticationTrustResolver trustResolver) {
        methodSecurityExpressionHandler.setTrustResolver(trustResolver);
    }

    @Autowired(required = false)
    public void setPermissionEvaluator(List<PermissionEvaluator> permissionEvaluators) {
        if (permissionEvaluators.size() != 1) {
            logger.debug("Not autwiring PermissionEvaluator since size != 1. Got " + permissionEvaluators);
        }
        methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluators.get(0));
    }
}
