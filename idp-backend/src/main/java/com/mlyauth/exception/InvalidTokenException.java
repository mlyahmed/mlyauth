package com.mlyauth.exception;

public class InvalidTokenException extends IDPException {

    private InvalidTokenException(String message) {
        super(message);
    }

    public static InvalidTokenException newInstance(String message) {
        return new InvalidTokenException(message);
    }

}
