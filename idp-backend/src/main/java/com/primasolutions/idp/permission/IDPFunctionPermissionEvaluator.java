package com.primasolutions.idp.permission;

import com.primasolutions.idp.permission.validators.IDPPermissionValidator;
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
    public boolean hasPermission(final Authentication authentication, final Object target, final Object permission) {
        final IDPPermissionValidator validator = validators.get(target.getClass());
        if (validator != null)
            return validator.hasPermission(authentication, target, permission);
        return false;
    }

    @Override
    public boolean hasPermission(final Authentication auth, final Serializable targetId, final String target,
                                 final Object permission) {
        return false;
    }

    @Autowired(required = false)
    public void setBusinessEntityPermissionValidators(final List<IDPPermissionValidator> validators) {
        for (IDPPermissionValidator validator : validators) {
            this.validators.put(validator.support(), validator);
        }
    }

}
