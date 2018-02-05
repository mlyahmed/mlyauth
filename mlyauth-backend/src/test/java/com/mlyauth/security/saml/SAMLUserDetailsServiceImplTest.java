package com.mlyauth.security.saml;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;

import java.util.LinkedList;
import java.util.List;

import static com.mlyauth.beans.AttributeBean.*;
import static com.mlyauth.security.saml.OpenSAMLUtils.buildStringAttribute;

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

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void when_the_client_id_attribute_is_null_then_error() {
        NameID nameID = OpenSAMLUtils.buildSAMLObject(NameID.class);
        Assertion assertion = OpenSAMLUtils.buildSAMLObject(Assertion.class);
        final List<Attribute> attributes = new LinkedList<>();
        attributes.add(buildStringAttribute(SAML_RESPONSE_CLIENT_ID.getCode(), "BVCG15487"));
        attributes.add(buildStringAttribute(SAML_RESPONSE_PROFILE.getCode(), "CL"));
        attributes.add(buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), "BA0000000000001"));
        attributes.add(buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), "S"));
        attributes.add(buildStringAttribute(SAML_RESPONSE_APP.getCode(), "policy"));
        final SAMLCredential credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
        service.loadUserBySAML(credential);
    }



}