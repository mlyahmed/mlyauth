package com.hohou.federation.idp.sample.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SampleClientJOSETokenInitializer {

    @Value("${cl.jose.entityId}")
    private String localEntityId;

    @Value("${cl.jose.refreshId}")
    private String refreshId;


    public SampleClientToken newToken() {
        SampleClientToken token = new SampleClientToken();
        token.setId(refreshId);
        token.setIssuer(localEntityId);
        token.setAudience("local-policy");
        token.setDelegate(localEntityId);
        token.setDelegator("gestF");
        token.setNorm("JOSE");
        return token;
    }



}
