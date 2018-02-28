package com.mlyauth.exception;

public class TokenNotCipheredException extends IDPException {

    private TokenNotCipheredException(String message) {
        super(message);
    }

    public static TokenNotCipheredException newInstance() {
        return new TokenNotCipheredException("The token is not cyphered !");
    }
}