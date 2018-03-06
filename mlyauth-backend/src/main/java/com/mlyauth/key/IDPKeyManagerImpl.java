package com.mlyauth.key;

import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.domain.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Component
public class IDPKeyManagerImpl implements IDPKeyManager {

    @Autowired
    private KeyManager keyManager;

    @Override
    public PrivateKey getLocalPrivateKey() {
        return keyManager.getDefaultCredential().getPrivateKey();
    }

    @Override
    public PublicKey getLocalPublicKey() {
        return keyManager.getDefaultCredential().getPublicKey();
    }

    @Override
    public Certificate getLocalCertificate() {
        return keyManager.getCertificate(keyManager.getDefaultCredentialName());
    }

    @Override
    public Certificate getPeerCertificate(Application app, AuthAspectType aspectType) {
        return null;
    }

}
