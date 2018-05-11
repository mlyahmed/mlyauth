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

    public JOSERefreshToken decodeRefresh(final String serialized, final AspectType peerAspect) {
        JOSERefreshToken tkn = tokenFactory.createRefreshToken(serialized, localKey(), peerKey(serialized, peerAspect));
        tkn.decipher();
        return tkn;
    }

    public JOSEAccessToken decodeAccess(final String serialized, final AspectType peerAspect) {
        JOSEAccessToken tkn = tokenFactory.createAccessToken(serialized, localKey(), peerKey(serialized, peerAspect));
        tkn.decipher();
        return tkn;
    }

    public JOSEAccessToken decodeAccess(final String serialized) {
        JOSEAccessToken tkn = tokenFactory.createAccessToken(serialized, localKey(), localPublicKey());
        tkn.decipher();
        return tkn;
    }

    private PrivateKey localKey() {
        return credentialManager.getPrivateKey();
    }

    private PublicKey localPublicKey() {
        return credentialManager.getPublicKey();
    }

    private String issuer(final String serialized) {
        return joseHelper.loadIssuer(serialized, localKey());
    }

    private PublicKey peerKey(final String serialized, final AspectType peerAspect) {
        return credentialManager.getPeerKey(issuer(serialized), peerAspect);
    }
}
