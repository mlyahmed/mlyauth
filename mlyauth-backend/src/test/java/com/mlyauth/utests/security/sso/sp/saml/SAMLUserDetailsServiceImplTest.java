package com.mlyauth.utests.security.sso.sp.saml;

import com.google.common.collect.Sets;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.IContextHolder;
import com.mlyauth.security.context.PrimaUser;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.sp.SAMLUserDetailsServiceImpl;
import com.mlyauth.utests.security.context.MockContextHolder;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.mlyauth.beans.AttributeBean.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SAMLUserDetailsServiceImplTest {


    public static final String CLIENT_ID = "BVCG15487";
    public static final String USER_PROFILE = "CL";
    public static final String PRESTATION_ID = "BA0000000000001";
    public static final String ACTION = "S";
    public static final String APPLICATION_CODE = "policy";
    public static final String USERNAME = "ahmed.elidrissi";

    @Spy
    private IContextHolder contextHolder = new MockContextHolder();

    @Mock
    private PersonDAO personDAO;

    @InjectMocks
    private SAMLUserDetailsServiceImpl service;

    private SAMLHelper samlLHelper = new SAMLHelper();

    private NameID nameID;
    private Assertion assertion;
    private List<Attribute> attributes;
    private SAMLCredential credential;
    private Person person;
    private AuthenticationInfo authenticationInfo;
    private Object user;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        nameID = samlLHelper.buildSAMLObject(NameID.class);
        assertion = samlLHelper.buildSAMLObject(Assertion.class);
        attributes = new LinkedList<>();
    }


    @After
    public void tearsDown() {
        ((MockContextHolder) contextHolder).resetMock();
    }

    @Test
    public void when_all_good_then_return_user() {
        given_credential_with_all_attributes();
        given_the_person_exists();
        given_the_application_is_assigned_to_the_person();
        when_load_user();
        then_user_is_loaded();
    }


    @Test
    public void when_the_application_attribute_is_absent_then_user_is_returned() {
        given_all_credential_attributes_except_app();
        given_the_person_exists();
        when_load_user();
        then_user_is_loaded();
    }

    @Test
    public void the_attributes_must_be_loaded_in_the_context() {
        given_credential_with_all_attributes();
        given_the_person_exists();
        given_the_application_is_assigned_to_the_person();
        when_load_user();
        assertThat(contextHolder.getContext(), Matchers.notNullValue());
        assertThat(contextHolder.getAttribute(SAML_RESPONSE_CLIENT_ID.getCode()), Matchers.equalTo(CLIENT_ID));
        assertThat(contextHolder.getAttribute(SAML_RESPONSE_PROFILE.getCode()), Matchers.equalTo(USER_PROFILE));
        assertThat(contextHolder.getAttribute(SAML_RESPONSE_PRESTATION_ID.getCode()), Matchers.equalTo(PRESTATION_ID));
        assertThat(contextHolder.getAttribute(SAML_RESPONSE_ACTION.getCode()), Matchers.equalTo(ACTION));
        assertThat(contextHolder.getAttribute(SAML_RESPONSE_APP.getCode()), Matchers.equalTo(APPLICATION_CODE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_credential_is_null_then_exception() {
        service.loadUserBySAML(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_attributes_list_is_empty_then_error() {
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
        when_load_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_id_attribute_is_null_then_error() {
        given_all_credential_attributes_except_the_client_id();
        when_load_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_person_is_not_found_then_error() {
        given_credential_with_all_attributes();
        given_the_person_does_not_exist();
        when_load_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_profile_code_attribute_is_absent_then_error() {
        given_all_credential_attributes_except_the_profile_code();
        given_the_person_exists();
        when_load_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_application_code_attribute_exists_and_the_application_is_not_assigned_to_the_client_then_error() {
        given_credential_with_all_attributes();
        given_the_person_exists();
        person.setApplications(Collections.emptySet());
        when_load_user();
    }

    private void given_the_application_is_assigned_to_the_person() {
        person.setApplications(Sets.newHashSet(Application.newInstance().setAppname(APPLICATION_CODE)));
    }

    private void given_credential_with_all_attributes() {
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_CLIENT_ID.getCode(), CLIENT_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PROFILE.getCode(), USER_PROFILE));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), PRESTATION_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), ACTION));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_APP.getCode(), APPLICATION_CODE));
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
    }

    private void given_all_credential_attributes_except_app() {
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_CLIENT_ID.getCode(), CLIENT_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PROFILE.getCode(), USER_PROFILE));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), PRESTATION_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), ACTION));
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
    }

    private void given_all_credential_attributes_except_the_profile_code() {
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_CLIENT_ID.getCode(), CLIENT_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), PRESTATION_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), ACTION));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_APP.getCode(), APPLICATION_CODE));
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
    }

    private void given_all_credential_attributes_except_the_client_id() {
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PROFILE.getCode(), USER_PROFILE));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_PRESTATION_ID.getCode(), PRESTATION_ID));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_ACTION.getCode(), ACTION));
        attributes.add(samlLHelper.buildStringAttribute(SAML_RESPONSE_APP.getCode(), APPLICATION_CODE));
        credential = new SAMLCredential(nameID, assertion, null, null, attributes, null, null);
    }

    private void given_the_person_exists() {
        authenticationInfo = AuthenticationInfo.newInstance()
                .setLogin(USERNAME)
                .setPassword(RandomStringUtils.random(20, true, true));
        person = new Person();
        person.setAuthenticationInfo(authenticationInfo);
        when(personDAO.findByExternalId(CLIENT_ID)).thenReturn(person);
    }

    private void given_the_person_does_not_exist() {
        when(personDAO.findByExternalId(CLIENT_ID)).thenReturn(null);
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