package com.primasolutions.idp.exception;

public final class InvalidTokenException extends IDPException {

    private InvalidTokenException(final String message) {
        super(message);
    }

    public static InvalidTokenException newInstance(final String message) {
        return new InvalidTokenException(message);
    }

}
