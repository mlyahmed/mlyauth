package com.mlyauth.exception;

public final class IDPSAMLErrorException extends IDPException {

    private IDPSAMLErrorException(final Exception e) {
        super(e);
    }

    public static IDPSAMLErrorException newInstance(final Exception e) {
        return new IDPSAMLErrorException(e);
    }


}
