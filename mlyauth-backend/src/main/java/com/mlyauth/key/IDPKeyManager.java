package com.mlyauth.key;

import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.domain.Application;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface IDPKeyManager {

    PrivateKey getLocalPrivateKey();

    PublicKey getLocalPublicKey();

    Certificate getLocalCertificate();

    Certificate getPeerCertificate(Application app, AuthAspectType aspectType);

}
