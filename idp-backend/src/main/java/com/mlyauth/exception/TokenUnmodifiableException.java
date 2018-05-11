package com.mlyauth.exception;

public final class TokenUnmodifiableException extends IDPException {

    private TokenUnmodifiableException(final String message) {
        super(message);
    }

    public static TokenUnmodifiableException newInstance() {
        return new TokenUnmodifiableException("The token is in a final state !");
    }
}
