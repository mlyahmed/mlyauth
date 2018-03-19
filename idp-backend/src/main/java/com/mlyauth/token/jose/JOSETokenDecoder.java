package com.mlyauth.token.jose;

import com.mlyauth.constants.AspectType;
import com.mlyauth.credentials.CredentialManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class JOSETokenDecoder {

    @Autowired
    private JOSETokenFactory tokenFactory;

    @Autowired
    private JOSEHelper joseHelper;

    @Autowired
    private CredentialManager credentialManager;

    public JOSERefreshToken decodeRefresh(String serialized, AspectType peerAspect){
        JOSERefreshToken token = tokenFactory.createRefreshToken(serialized, localKey(), peerKey(serialized, peerAspect));
        token.decipher();
        return token;
    }

    public JOSEAccessToken decodeAccess(String serialized, AspectType peerAspect){
        JOSEAccessToken token = tokenFactory.createAccessToken(serialized, localKey(), peerKey(serialized, peerAspect));
        token.decipher();
        return token;
    }

    public JOSEAccessToken decodeAccess(String serialized){
        JOSEAccessToken token = tokenFactory.createAccessToken(serialized, localKey(), localPublicKey());
        token.decipher();
        return token;
    }

    private PrivateKey localKey() {
        return credentialManager.getPrivateKey();
    }

    private PublicKey localPublicKey() {
        return credentialManager.getPublicKey();
    }

    private String issuer(String serialized) {
        return joseHelper.loadIssuer(serialized, localKey());
    }

    private PublicKey peerKey(String serialized, AspectType peerAspect) {
        return credentialManager.getPeerKey(issuer(serialized), peerAspect);
    }
}
