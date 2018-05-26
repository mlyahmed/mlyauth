package com.primasolutions.idp.exception;

public final class InvalidTokenExc extends IDPException {

    private InvalidTokenExc(final String message) {
        super(message);
    }

    public static InvalidTokenExc newInstance(final String message) {
        return new InvalidTokenExc(message);
    }

}
