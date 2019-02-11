package com.hohou.federation.idp.exception;

public final class IDPSAMLErrorExc extends IDPException {

    private IDPSAMLErrorExc(final Exception e) {
        super(e);
    }

    public static IDPSAMLErrorExc newInstance(final Exception e) {
        return new IDPSAMLErrorExc(e);
    }


}
