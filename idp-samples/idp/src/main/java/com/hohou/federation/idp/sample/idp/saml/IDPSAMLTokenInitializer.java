package com.hohou.federation.idp.sample.idp.saml;

import com.hohou.federation.idp.sample.idp.SampleIDPToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IDPSAMLTokenInitializer {

    @Value("${idp.saml.entityId}")
    private String localEntityId;

    @Value("${sp.saml.entityId}")
    private String peerEntityId;

    @Value("${sp.saml.endpoint}")
    private String targetURL;

    public SampleIDPToken newToken() {
        SampleIDPToken token = new SampleIDPToken();
        token.setId(UUID.randomUUID().toString());
        token.setSubject("1");
        token.setScopes("");
        token.setBp("");
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
        token.setNorm("SAML");
        return token;
    }
}
