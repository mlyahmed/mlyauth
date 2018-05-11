package com.mlyauth.exception;

public final class BadSPSAMLAspectAttributeValueException extends IDPException {

    private BadSPSAMLAspectAttributeValueException() {

    }


    private BadSPSAMLAspectAttributeValueException(final Exception e) {
        super(e);
    }

    public static BadSPSAMLAspectAttributeValueException newInstance() {
        return new BadSPSAMLAspectAttributeValueException();
    }

    public static BadSPSAMLAspectAttributeValueException newInstance(final Exception e) {
        return new BadSPSAMLAspectAttributeValueException(e);
    }
}
