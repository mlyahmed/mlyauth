package com.mlyauth.exception;

public class EncryptionCredentialError extends IDPException {

    private EncryptionCredentialError(Exception e) {
        super(e);
    }

    public static EncryptionCredentialError newInstance(Exception e) {
        return new EncryptionCredentialError(e);
    }
}
