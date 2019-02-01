package com.primasolutions.idp.credentials;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class CredentialsPair {

    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    public CredentialsPair(final PrivateKey privateKey, final X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }


    public X509Certificate getCertificate() {
        return certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }



}
