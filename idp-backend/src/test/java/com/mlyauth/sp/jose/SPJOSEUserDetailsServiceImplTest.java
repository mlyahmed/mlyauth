package com.mlyauth.sp.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.context.MockContextHolder;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.token.jose.MockJOSEAccessToken;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static com.google.common.collect.Sets.newHashSet;
import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SPJOSEUserDetailsServiceImplTest {

    @Spy
    private IContextHolder context = new MockContextHolder();

    @Mock
    private PersonDAO personDAO;

    @InjectMocks
    private SPJOSEUserDetailsServiceImpl service;

    private MockJOSEAccessToken token;
    private Person person;
    private AuthenticationInfo authenticationInfo;
    private IDPUser user;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        given_the_token();
        given_the_person_exists();
        given_the_application_is_assigned_to_the_person();
    }

    private void given_the_application_is_assigned_to_the_person() {
        person.setApplications(newHashSet(Application.newInstance().setAppname(token.getClaim(APPLICATION.getValue()))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_token_is_null_then_error() {
        SPJOSEUserDetailsServiceImpl service = new SPJOSEUserDetailsServiceImpl();
        service.loadUserByJOSE(null);
    }

    @Test
    public void when_the_token_is_valid_then_return_the_user() {
        when_load_the_user();
        then_user_is_loaded();
    }

    @Test
    public void the_attributes_must_be_loaded_in_the_context() {
        when_load_the_user();
        then_the_attributes_are_loaded_in_the_context();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_subject_is_null_then_error() {
        token.setSubject(null);
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_subject_is_empty_then_error() {
        token.setSubject("");
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_id_is_null_then_error() {
        token.setClaim(CLIENT_ID.getValue(), null);
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_id_is_empty_then_error() {
        token.setClaim(CLIENT_ID.getValue(), "");
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_profile_is_null_then_error() {
        token.setClaim(CLIENT_PROFILE.getValue(), null);
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_profile_is_empty_then_error() {
        token.setClaim(CLIENT_PROFILE.getValue(), "");
        when_load_the_user();
    }

    @Test
    public void when_the_application_claim_is_null_then_OK() {
        token.setClaim(APPLICATION.getValue(), null);
        when_load_the_user();
        then_user_is_loaded();
    }

    @Test
    public void when_the_application_claim_is_empty_then_OK() {
        token.setClaim(APPLICATION.getValue(), "");
        when_load_the_user();
        then_user_is_loaded();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_person_is_not_found_then_error() {
        when(personDAO.findByExternalId(token.getSubject())).thenReturn(null);
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_application_is_not_assigned_to_the_person_then_error() {
        person.setApplications(Collections.emptySet());
        when_load_the_user();
    }

    private void given_the_token() {
        Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        token = new MockJOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
        token.setStamp(randomString());
        token.setSubject(randomString());
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(randomString());
        token.setAudience(randomString());
        token.setIssuer(randomString());
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL(randomString());
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(CLIENT_ID.getValue(), randomString());
        token.setClaim(CLIENT_PROFILE.getValue(), randomString());
        token.setClaim(ENTITY_ID.getValue(), randomString());
        token.setClaim(ACTION.getValue(), randomString());
        token.setClaim(APPLICATION.getValue(), randomString());
    }

    private void given_the_person_exists() {
        authenticationInfo = AuthenticationInfo.newInstance()
                .setLogin(randomString())
                .setPassword(randomString())
                .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 60)));
        person = new Person();
        person.setAuthenticationInfo(authenticationInfo);
        when(personDAO.findByExternalId(token.getSubject())).thenReturn(person);
    }

    private void when_load_the_user() {
        user = service.loadUserByJOSE(token);
    }

    private void then_user_is_loaded() {
        assertThat(user, Matchers.notNullValue());
        assertThat(user, Matchers.instanceOf(IDPUser.class));
        assertThat(user.getPerson(), equalTo(person));
    }

    private void then_the_attributes_are_loaded_in_the_context() {
        assertThat(context.getContext(), Matchers.notNullValue());
        assertThat(context.getAttribute(CLIENT_ID.getValue()), equalTo(token.getClaim(CLIENT_ID.getValue())));
        assertThat(context.getAttribute(CLIENT_PROFILE.getValue()), equalTo(token.getClaim(CLIENT_PROFILE.getValue())));
        assertThat(context.getAttribute(ENTITY_ID.getValue()), equalTo(token.getClaim(ENTITY_ID.getValue())));
        assertThat(context.getAttribute(ACTION.getValue()), equalTo(token.getClaim(ACTION.getValue())));
        assertThat(context.getAttribute(APPLICATION.getValue()), equalTo(token.getClaim(APPLICATION.getValue())));
    }
}