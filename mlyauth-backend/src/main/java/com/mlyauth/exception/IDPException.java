package com.mlyauth.exception;

import java.util.Collection;
import java.util.LinkedHashSet;

public class IDPException extends RuntimeException {

    private Collection<AuthError> errors = new LinkedHashSet<>();

    public static IDPException newInstance() {
        return new IDPException();
    }

    public Collection<AuthError> getErrors() {
        return errors;
    }

    public IDPException setErrors(Collection<AuthError> errors) {
        this.errors = errors;
        return this;
    }
}
