package com.primasolutions.idp.exception;

public final class EncryptionCredentialExc extends IDPException {

    private EncryptionCredentialExc(final Exception e) {
        super(e);
    }

    public static EncryptionCredentialExc newInstance(final Exception e) {
        return new EncryptionCredentialExc(e);
    }
}
