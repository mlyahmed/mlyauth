package com.mlyauth.exception;

public class BadSPSAMLAspectAttributeValueException extends IDPException {

    private BadSPSAMLAspectAttributeValueException() {

    }


    private BadSPSAMLAspectAttributeValueException(Exception e) {
        super(e);
    }

    public static BadSPSAMLAspectAttributeValueException newInstance() {
        return new BadSPSAMLAspectAttributeValueException();
    }

    public static BadSPSAMLAspectAttributeValueException newInstance(Exception e) {
        return new BadSPSAMLAspectAttributeValueException(e);
    }
}
