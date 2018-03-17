package com.mlyauth.token;

import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSERefreshToken;
import com.mlyauth.token.saml.SAMLAccessToken;
import org.opensaml.xml.security.credential.Credential;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface ITokenFactory {

    SAMLAccessToken createSAMLAccessToken(Credential credential);

    SAMLAccessToken createSAMLAccessToken(String seialized, Credential credential);

    JOSEAccessToken createJOSEAccessToken(PrivateKey privateKey, PublicKey publicKey);

    JOSEAccessToken createJOSEAccessToken(String seialized, PrivateKey privateKey, PublicKey publicKey);

    JOSERefreshToken createJOSERefreshToken(PrivateKey privateKey, PublicKey publicKey);

    JOSERefreshToken createJOSERefreshToken(String seialized, PrivateKey privateKey, PublicKey publicKey);
}
