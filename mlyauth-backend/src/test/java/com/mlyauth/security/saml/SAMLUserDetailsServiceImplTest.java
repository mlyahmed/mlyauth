package com.mlyauth.security.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;

import java.util.LinkedList;
import java.util.List;

public class SAMLUserDetailsServiceImplTest {


    private SAMLUserDetailsServiceImpl service;

    @Before
    public void setup() throws Exception {
        service = new SAMLUserDetailsServiceImpl();
        DefaultBootstrap.bootstrap();
    }


    @Test(expected = IllegalArgumentException.class)
    public void when_credential_is_null_then_exception() {
        service.loadUserBySAML(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_attributes_is_empty_then_error() {
        NameID nameID = OpenSAMLUtils.buildSAMLObject(NameID.class);
        Assertion assertion = OpenSAMLUtils.buildSAMLObject(Assertion.class);
        List<Attribute> attributes = new LinkedList<>();
        final SAMLCredential credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
        service.loadUserBySAML(credential);
    }
}