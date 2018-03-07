package com.mlyauth.key;

import com.mlyauth.constants.AspectType;
import com.mlyauth.domain.Application;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public class MockCredentialManager implements CredentialManager {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public MockCredentialManager(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public PrivateKey getLocalPrivateKey() {
        return privateKey;
    }

    @Override
    public PublicKey getLocalPublicKey() {
        return publicKey;
    }

    @Override
    public Certificate getLocalCertificate() {
        return null;
    }

    @Override
    public Certificate getPeerCertificate(Application app, AspectType aspectType) {
        return null;
    }

}
