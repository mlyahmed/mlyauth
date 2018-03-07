package com.mlyauth.exception;

public class TokenUnmodifiableException extends IDPException {

    private TokenUnmodifiableException(String message) {
        super(message);
    }

    public static TokenUnmodifiableException newInstance() {
        return new TokenUnmodifiableException("The token is in a final state !");
    }
}
