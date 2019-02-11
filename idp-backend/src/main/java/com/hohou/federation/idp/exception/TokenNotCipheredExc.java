package com.hohou.federation.idp.exception;

public final class TokenNotCipheredExc extends IDPException {

    private TokenNotCipheredExc(final String message) {
        super(message);
    }

    public static TokenNotCipheredExc newInstance() {
        return new TokenNotCipheredExc("The token is not cyphered !");
    }
}
