package com.primasolutions.idp.token.jose;

import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.credentials.CredentialManager;
import com.primasolutions.idp.token.IToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class JOSETokenDecoderImpl {

    @Autowired
    private JOSETokenFactory tokenFactory;

    @Autowired
    private JOSEHelper joseHelper;

    @Autowired
    private CredentialManager credentialManager;

    public JOSERefreshToken decodeRefresh(final String serialized, final AspectType peerAspect) {
        return decipher(tokenFactory.newRefreshToken(serialized, localKey(), peerKey(serialized, peerAspect)));
    }

    public JOSEAccessToken decodeAccess(final String serialized, final AspectType peerAspect) {
        return decipher(tokenFactory.newAccessToken(serialized, localKey(), peerKey(serialized, peerAspect)));
    }

    public JOSEAccessToken decodeAccess(final String serialized) {
        return decipher(tokenFactory.newAccessToken(serialized, localKey(), localPublicKey()));
    }

    private <T extends IToken> T decipher(final T token) {
        token.decipher();
        return token;
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
