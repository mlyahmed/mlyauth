package com.primasolutions.idp.credentials;

import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

@Data
public class CredentialsPair {

    private final PrivateKey privateKey;
    private final X509Certificate certificate;


    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

}
