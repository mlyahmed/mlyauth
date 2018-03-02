package com.mlyauth.exception;

public class JOSEErrorException extends IDPException {

    private JOSEErrorException(Exception e) {
        super(e);
    }

    public static JOSEErrorException newInstance(Exception e) {
        return new JOSEErrorException(e);
    }


}
