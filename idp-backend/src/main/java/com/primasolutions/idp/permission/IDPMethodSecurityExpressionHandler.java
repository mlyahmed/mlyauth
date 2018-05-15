package com.primasolutions.idp.permission;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;

public class IDPMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    @Override
    public StandardEvaluationContext createEvaluationContextInternal(final Authentication auth,
                                                                     final MethodInvocation mi) {
        final StandardEvaluationContext evaluationContext = super.createEvaluationContextInternal(auth, mi);
        final StandardTypeLocator typeLocator = (StandardTypeLocator) evaluationContext.getTypeLocator();
        typeLocator.registerImport(this.getClass().getPackage().getName());
        return evaluationContext;
    }

}
