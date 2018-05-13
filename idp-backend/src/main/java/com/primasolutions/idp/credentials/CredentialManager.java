package com.primasolutions.idp.credentials;

import com.primasolutions.idp.constants.AspectType;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface CredentialManager {

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    Certificate getCertificate();

    Certificate getPeerCertificate(String entityId, AspectType aspectType);

    PublicKey getPeerKey(String entityId, AspectType aspectType);

}
