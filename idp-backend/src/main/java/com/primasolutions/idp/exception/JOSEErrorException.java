package com.primasolutions.idp.exception;

public final class JOSEErrorException extends IDPException {

    private JOSEErrorException(final Exception e) {
        super(e);
    }

    private JOSEErrorException(final String message) {
        super(message);
    }

    public static JOSEErrorException newInstance(final Exception e) {
        return new JOSEErrorException(e);
    }

    public static JOSEErrorException newInstance(final String message) {
        return new JOSEErrorException(message);
    }


}
