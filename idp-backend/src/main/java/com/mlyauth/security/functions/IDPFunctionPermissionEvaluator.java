package com.mlyauth.security.functions;

import com.mlyauth.security.functions.validators.IDPPermissionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class IDPFunctionPermissionEvaluator implements PermissionEvaluator {

    private Map<Class<?>, IDPPermissionValidator> validators = new LinkedHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        final IDPPermissionValidator validator = validators.get(targetDomainObject.getClass());
        if (validator != null)
            return validator.hasPermission(authentication, targetDomainObject, permission);
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String target, Object permission) {
        return false;
    }

    @Autowired(required = false)
    public void setBusinessEntityPermissionValidators(List<IDPPermissionValidator> validators) {
        for (IDPPermissionValidator validator : validators) {
            this.validators.put(validator.support(), validator);
        }
    }

}
