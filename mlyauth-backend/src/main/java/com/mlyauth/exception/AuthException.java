package com.mlyauth.exception;

import java.util.Collection;
import java.util.LinkedHashSet;

public class AuthException extends RuntimeException{

    private Collection<AuthError> errors = new LinkedHashSet<>();

    public static AuthException newInstance() {
        return new AuthException();
    }

    public Collection<AuthError> getErrors() {
        return errors;
    }

    public AuthException setErrors(Collection<AuthError> errors) {
        this.errors = errors;
        return this;
    }
}
