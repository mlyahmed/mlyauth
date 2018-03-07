package com.mlyauth.key;

import com.mlyauth.constants.AspectType;
import com.mlyauth.domain.Application;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface CredentialManager {

    PrivateKey getLocalPrivateKey();

    PublicKey getLocalPublicKey();

    Certificate getLocalCertificate();

    Certificate getPeerCertificate(Application app, AspectType aspectType);

}
