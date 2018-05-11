package com.mlyauth.exception;

public final class EncryptionCredentialException extends IDPException {

    private EncryptionCredentialException(final Exception e) {
        super(e);
    }

    public static EncryptionCredentialException newInstance(final Exception e) {
        return new EncryptionCredentialException(e);
    }
}
