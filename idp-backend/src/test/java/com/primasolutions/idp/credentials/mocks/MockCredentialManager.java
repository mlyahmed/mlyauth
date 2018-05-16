package com.primasolutions.idp.credentials.mocks;

import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.credentials.CredentialManager;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

public class MockCredentialManager implements CredentialManager {

    private Map<String, Map<AspectType, Certificate>> peerCredentials = new HashMap<>();

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public MockCredentialManager(final PrivateKey privateKey, final PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public Certificate getCertificate() {
        return null;
    }

    @Override
    public Certificate getPeerCertificate(final String entityId, final AspectType aspectType) {
        return peerCredentials.get(entityId) != null ? peerCredentials.get(entityId).get(aspectType) : null;
    }

    @Override
    public PublicKey getPeerKey(final String entityId, final AspectType aspectType) {
        final Certificate certificate = getPeerCertificate(entityId, aspectType);
        return certificate != null ? certificate.getPublicKey() : null;
    }


    public void setPeerCertificate(final String entityId, final AspectType aspectType, final Certificate certificate) {
        final Map<AspectType, Certificate> credentials = peerCredentials.get(entityId);
        if (credentials == null) peerCredentials.put(entityId, new HashMap<>());
        peerCredentials.get(entityId).put(aspectType, certificate);
    }

}
