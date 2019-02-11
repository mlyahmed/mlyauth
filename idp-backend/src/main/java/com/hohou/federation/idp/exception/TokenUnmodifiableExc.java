package com.hohou.federation.idp.exception;

public final class TokenUnmodifiableExc extends IDPException {

    private TokenUnmodifiableExc(final String message) {
        super(message);
    }

    public static TokenUnmodifiableExc newInstance() {
        return new TokenUnmodifiableExc("The token is in a final state !");
    }
}
