package com.mlyauth.exception;

public class NotSPSAMLApplicationException extends IDPException {

    public static NotSPSAMLApplicationException newInstance() {
        return new NotSPSAMLApplicationException();
    }
}
