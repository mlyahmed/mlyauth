package com.hohou.federation.idp.exception;

public final class BadSPSAMLAspectAttributeValueExc extends IDPException {

    private BadSPSAMLAspectAttributeValueExc() {

    }


    private BadSPSAMLAspectAttributeValueExc(final Exception e) {
        super(e);
    }

    public static BadSPSAMLAspectAttributeValueExc newInstance() {
        return new BadSPSAMLAspectAttributeValueExc();
    }

    public static BadSPSAMLAspectAttributeValueExc newInstance(final Exception e) {
        return new BadSPSAMLAspectAttributeValueExc(e);
    }
}
