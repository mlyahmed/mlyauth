package com.primasolutions.idp.exception;

public final class JOSEErrorExc extends IDPException {

    private JOSEErrorExc(final Exception e) {
        super(e);
    }

    private JOSEErrorExc(final String message) {
        super(message);
    }

    public static JOSEErrorExc newInstance(final Exception e) {
        return new JOSEErrorExc(e);
    }

    public static JOSEErrorExc newInstance(final String message) {
        return new JOSEErrorExc(message);
    }


}
