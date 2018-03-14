package com.primasolutions.idp.sample.idp.jose;

import com.primasolutions.idp.sample.idp.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IDPJOSETokenInitializer {

    @Value("${idp.jose.entityId}")
    private String localEntityId;

    @Value("${sp.jose.entityId}")
    private String peerEntityId;

    @Value("${sp.jose.endpoint}")
    private String targetURL;

    public Token newToken() {
        Token token = new Token();
        token.setId(UUID.randomUUID().toString());
        token.setSubject("1");
        token.setScopes("PERSON");
        token.setBp("SSO");
        token.setState(UUID.randomUUID().toString());
        token.setIssuer(localEntityId);
        token.setAudience(peerEntityId);
        token.setTargetURL(targetURL);
        token.setDelegator("1");
        token.setDelegate(localEntityId);
        token.setVerdict("SUCCESS");
        token.setClientId("1");
        token.setClientProfile("CL");
        token.setEntityId(UUID.randomUUID().toString());
        token.setAction("S");
        token.setApplication("");
        token.setNorm("JOSE");
        return token;
    }
}
