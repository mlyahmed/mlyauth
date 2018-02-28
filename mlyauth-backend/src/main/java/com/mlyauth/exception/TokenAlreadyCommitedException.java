package com.mlyauth.exception;

public class TokenAlreadyCommitedException extends IDPException {

    private TokenAlreadyCommitedException(String message) {
        super(message);
    }

    public static TokenAlreadyCommitedException newInstance() {
        return new TokenAlreadyCommitedException("The token is in a final state !");
    }
}
