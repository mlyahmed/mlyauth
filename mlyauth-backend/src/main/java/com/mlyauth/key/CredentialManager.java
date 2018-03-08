package com.mlyauth.key;

import com.mlyauth.constants.AspectType;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface CredentialManager {

    PrivateKey getLocalPrivateKey();

    PublicKey getLocalPublicKey();

    Certificate getLocalCertificate();

    Certificate getPeerCertificate(String entityId, AspectType aspectType);

    PublicKey getPeerKey(String entityId, AspectType aspectType);

}
