package com.mlyauth.exception;

import java.util.Collection;
import java.util.LinkedHashSet;

public class IDPException extends RuntimeException {

    private Collection<AuthError> errors = new LinkedHashSet<>();

    protected IDPException(Exception e) {
        super(e);
    }

    protected IDPException(String message) {
        super(message);
    }

    protected IDPException() {

    }

    public static IDPException newInstance() {
        return new IDPException();
    }

    public static IDPException newInstance(Exception e) {
        return new IDPException(e);
    }

    public Collection<AuthError> getErrors() {
        return errors;
    }

    public IDPException setErrors(Collection<AuthError> errors) {
        this.errors = errors;
        return this;
    }
}
