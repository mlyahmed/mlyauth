package com.mlyauth.exception;

public class EncryptionCredentialException extends IDPException {

    private EncryptionCredentialException(Exception e) {
        super(e);
    }

    public static EncryptionCredentialException newInstance(Exception e) {
        return new EncryptionCredentialException(e);
    }
}
