package com.mlyauth.key;

import com.mlyauth.constants.AspectType;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

public class MockCredentialManager implements CredentialManager {

    private Map<String, Map<AspectType, Certificate>> peerCredentials = new HashMap<>();

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
    public Certificate getPeerCertificate(String entityId, AspectType aspectType) {
        return peerCredentials.get(entityId) != null ? peerCredentials.get(entityId).get(aspectType) : null;
    }

    @Override
    public PublicKey getPeerKey(String entityId, AspectType aspectType) {
        final Certificate certificate = getPeerCertificate(entityId, aspectType);
        return certificate != null ? certificate.getPublicKey() : null;
    }


    public void setPeerCertificate(String entityId, AspectType aspectType, Certificate certificate) {
        final Map<AspectType, Certificate> credentials = peerCredentials.get(entityId);
        if (credentials == null) peerCredentials.put(entityId, new HashMap<>());
        peerCredentials.get(entityId).put(aspectType, certificate);
    }

}
