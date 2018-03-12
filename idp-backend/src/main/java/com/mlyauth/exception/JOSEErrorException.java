package com.mlyauth.exception;

public class JOSEErrorException extends IDPException {

    private JOSEErrorException(Exception e) {
        super(e);
    }

    private JOSEErrorException(String message) {
        super(message);
    }

    public static JOSEErrorException newInstance(Exception e) {
        return new JOSEErrorException(e);
    }

    public static JOSEErrorException newInstance(String message) {
        return new JOSEErrorException(message);
    }


}
