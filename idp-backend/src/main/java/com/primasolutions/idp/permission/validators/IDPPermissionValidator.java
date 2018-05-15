package com.primasolutions.idp.permission.validators;

import org.springframework.security.core.Authentication;

public interface IDPPermissionValidator<T> {

    boolean hasPermission(Authentication authentication, T target, Object permission);

    Class<T> support();

}
