package com.primasolutions.idp.exception;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class IDPException extends RuntimeException {

    private Collection<AuthError> errors = new LinkedHashSet<>();

    protected IDPException(final Exception e) {
        super(e);
    }

    protected IDPException(final String message) {
        super(message);
    }

    protected IDPException() {

    }

    public static IDPException newInstance() {
        return new IDPException();
    }

    public static IDPException newInstance(final Exception e) {
        return new IDPException(e);
    }

    public static IDPException newInstance(final String message) {
        return new IDPException(message);
    }

    public Collection<AuthError> getErrors() {
        return errors;
    }

    public Collection<String> getErrorCodes() {
        return errors.stream().map(err -> err.getCode()).collect(Collectors.toList());
    }

    public IDPException setErrors(final Collection<AuthError> errors) {
        this.errors = errors;
        return this;
    }
}
