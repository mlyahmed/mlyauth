package com.mlyauth.utests.security.saml;

import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.security.PrimaUser;
import com.mlyauth.security.saml.OpenSAMLUtils;
import com.mlyauth.security.saml.SAMLUserDetailsServiceImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;

import java.util.LinkedList;
import java.util.List;

import static com.mlyauth.beans.AttributeBean.*;
import static com.mlyauth.security.saml.OpenSAMLUtils.buildStringAttribute;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SAMLUserDetailsServiceImplTest {


    public static final String CLIENT_ID = "BVCG15487";
    public static final String USER_PROFILE = "CL";
    public static final String PRESTATION_ID = "BA0000000000001";
    public static final String ACTION = "S";
    public static final String APPLICATION_CODE = "policy";
    public static final String USERNAME = "ahmed.elidrissi";


    @Mock
    private PersonDAO personDAO;

    @InjectMocks
    private SAMLUserDetailsServiceImpl service;

    private NameID nameID;
    private Assertion assertion;
    private List<Attribute> attributes;
    private SAMLCredential credential;
    private Person person;
    private Object user;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        nameID = OpenSAMLUtils.buildSAMLObject(NameID.class);
        assertion = OpenSAMLUtils.buildSAMLObject(Assertion.class);
        attributes = new LinkedList<>();
    }


    @Test
    public void when_all_good_then_return_user() {
        given_credential_with_all_attributes();
        given_the_person_exists();
        when_load_user();
        then_user_is_loaded();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_credential_is_null_then_exception() {
        service.loadUserBySAML(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_attributes_is_empty_then_error() {
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
        when_load_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_id_attribute_is_null_then_error() {
        given_assert_with_all_attributes_but_client_id();
        when_load_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_person_is_not_found_then_error() {
        given_credential_with_all_attributes();
        when(personDAO.findByExternalId(CLIENT_ID)).thenReturn(person);
        when_load_user();
    }

    private void given_assert_with_all_attributes_but_client_id() {
        attributes.add(buildStringAttribute(SAML_RESPONSE_PROFILE.getCode(), USER_PROFILE));
        attributes.add(buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), PRESTATION_ID));
        attributes.add(buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), ACTION));
        attributes.add(buildStringAttribute(SAML_RESPONSE_APP.getCode(), APPLICATION_CODE));
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
    }

    private void given_the_person_exists() {
        person = new Person();
        person.setUsername(USERNAME);
        person.setPassword(RandomStringUtils.random(20, true, true));
        when(personDAO.findByExternalId(CLIENT_ID)).thenReturn(person);
    }

    private void given_credential_with_all_attributes() {
        attributes.add(buildStringAttribute(SAML_RESPONSE_CLIENT_ID.getCode(), CLIENT_ID));
        attributes.add(buildStringAttribute(SAML_RESPONSE_PROFILE.getCode(), USER_PROFILE));
        attributes.add(buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), PRESTATION_ID));
        attributes.add(buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), ACTION));
        attributes.add(buildStringAttribute(SAML_RESPONSE_APP.getCode(), APPLICATION_CODE));
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
    }

    private void when_load_user() {
        user = service.loadUserBySAML(credential);
    }

    private void then_user_is_loaded() {
        assertThat(user, Matchers.notNullValue());
        assertThat(user, Matchers.instanceOf(PrimaUser.class));
        assertThat(((PrimaUser) user).getPerson(), Matchers.equalTo(person));
    }
}