package com.mlyauth.exception;

public final class TokenNotCipheredException extends IDPException {

    private TokenNotCipheredException(final String message) {
        super(message);
    }

    public static TokenNotCipheredException newInstance() {
        return new TokenNotCipheredException("The token is not cyphered !");
    }
}
