package com.mlyauth.exception;

import java.util.Collection;
import java.util.LinkedHashSet;

public class AuthException extends RuntimeException{

    private Collection<AuthError> errors = new LinkedHashSet<>();

    public Collection<AuthError> getErrors() {
        return errors;
    }

    public void setErrors(Collection<AuthError> errors) {
        this.errors = errors;
    }
}
