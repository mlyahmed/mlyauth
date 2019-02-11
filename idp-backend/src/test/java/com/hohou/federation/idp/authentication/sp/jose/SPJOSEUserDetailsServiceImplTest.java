package com.hohou.federation.idp.authentication.sp.jose;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthenticationInfoLookuper;
import com.hohou.federation.idp.constants.TokenScope;
import com.hohou.federation.idp.constants.TokenVerdict;
import com.hohou.federation.idp.context.IContextHolder;
import com.hohou.federation.idp.context.IDPUser;
import com.hohou.federation.idp.context.mocks.MockContextHolder;
import com.hohou.federation.idp.credentials.CredentialsPair;
import com.hohou.federation.idp.exception.IDPException;
import com.hohou.federation.idp.person.model.Person;
import com.hohou.federation.idp.person.model.PersonDAO;
import com.hohou.federation.idp.token.Claims;
import com.hohou.federation.idp.token.jose.mocks.MockJOSEAccessToken;
import com.hohou.federation.idp.tools.KeysForTests;
import com.hohou.federation.idp.tools.RandomForTests;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Date;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SPJOSEUserDetailsServiceImplTest {

    private static final int SEXTY_SECONDS = 1000 * 60;

    @Spy
    private IContextHolder context = new MockContextHolder();

    @Mock
    private PersonDAO personDAO;

    @Mock
    private AuthenticationInfoLookuper authenticationInfoLookuper;

    @InjectMocks
    private SPJOSEUserDetailsServiceImpl service;

    private MockJOSEAccessToken token;
    private Person person;
    private Application application;
    private AuthInfo authenticationInfo;
    private IDPUser user;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        given_the_token();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_token_is_null_then_error() {
        service = new SPJOSEUserDetailsServiceImpl();
        service.loadUserByJOSE(null);
    }

    @Test
    public void when_the_person_authentication_info_exists_then_OK() {
        given_the_person_exists();
        when_load_the_user();
        then_the_attributes_are_loaded_in_the_context();
    }

    @Test
    public void when_the_token_is_valid_then_return_the_user_as_person() {
        given_the_person_exists();
        when_load_the_user();
        then_user_is_loaded_as_person();
    }

    @Test
    public void when_the_application_authentication_info_exists_then_OK() {
        given_the_person_exists();
        when_load_the_user();
        then_the_attributes_are_loaded_in_the_context();
    }

    @Test
    public void when_the_token_is_valid_then_return_the_user_as_application() {
        given_the_application_authentication_info_exists();
        when_load_the_user();
        then_user_is_loaded_as_application();
    }

    private void given_the_application_authentication_info_exists() {
        authenticationInfo = AuthInfo.newInstance()
                .setLogin(RandomForTests.randomString())
                .setPassword(RandomForTests.randomString())
                .setExpireAt(new Date(System.currentTimeMillis() + SEXTY_SECONDS));
        application = Application.newInstance();
        application.setAuthenticationInfo(authenticationInfo);
        authenticationInfo.setApplication(application);
        when(authenticationInfoLookuper.byLogin(token.getSubject())).thenReturn(authenticationInfo);
    }

    private void then_user_is_loaded_as_application() {
        assertThat(user, Matchers.notNullValue());
        assertThat(user, Matchers.instanceOf(IDPUser.class));
        assertThat(user.getPerson(), nullValue());
        assertThat(user.getApplication(), equalTo(application));
    }


    @Test(expected = IllegalArgumentException.class)
    public void when_no_authentication_infor_is_found_then_error() {
        when(authenticationInfoLookuper.byLogin(token.getSubject())).thenReturn(null);
        when_load_the_user();
    }

    @Test(expected = IDPException.class)
    public void when_the_authentication_info_is_not_a_person_neither_an_application_then_error() {
        when(authenticationInfoLookuper.byLogin(token.getSubject())).thenReturn(AuthInfo.newInstance());
        when_load_the_user();
    }


    @Test
    public void the_attributes_must_be_loaded_in_the_context() {
        given_the_person_exists();
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
        token.setClaim(Claims.CLIENT_ID.getValue(), null);
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_id_is_empty_then_error() {
        token.setClaim(Claims.CLIENT_ID.getValue(), "");
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_profile_is_null_then_error() {
        token.setClaim(Claims.CLIENT_PROFILE.getValue(), null);
        when_load_the_user();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_client_profile_is_empty_then_error() {
        token.setClaim(Claims.CLIENT_PROFILE.getValue(), "");
        when_load_the_user();
    }

    @Test
    public void when_the_application_claim_is_null_then_OK() {
        given_the_person_exists();
        token.setClaim(Claims.APPLICATION.getValue(), null);
        when_load_the_user();
        then_user_is_loaded_as_person();
    }

    @Test
    public void when_the_application_claim_is_empty_then_OK() {
        given_the_person_exists();
        token.setClaim(Claims.APPLICATION.getValue(), "");
        when_load_the_user();
        then_user_is_loaded_as_person();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_person_is_not_found_then_error() {
        when(personDAO.findByExternalId(token.getSubject())).thenReturn(null);
        when_load_the_user();
    }

    private void given_the_token() {
        CredentialsPair credential = KeysForTests.generateRSACredential();
        token = new MockJOSEAccessToken(credential.getPrivateKey(), credential.getPublicKey());
        token.setStamp(RandomForTests.randomString());
        token.setSubject(RandomForTests.randomString());
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(RandomForTests.randomString());
        token.setAudience(RandomForTests.randomString());
        token.setIssuer(RandomForTests.randomString());
        token.setDelegator(RandomForTests.randomString());
        token.setDelegate(RandomForTests.randomString());
        token.setTargetURL(RandomForTests.randomString());
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(Claims.CLIENT_ID.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.CLIENT_PROFILE.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.ENTITY_ID.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.ACTION.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.APPLICATION.getValue(), RandomForTests.randomString());
    }

    private void given_the_person_exists() {
        authenticationInfo = AuthInfo.newInstance()
                .setLogin(RandomForTests.randomString())
                .setPassword(RandomForTests.randomString())
                .setExpireAt(new Date(System.currentTimeMillis() + (SEXTY_SECONDS)));
        person = Person.newInstance();
        person.setAuthenticationInfo(authenticationInfo);
        authenticationInfo.setPerson(person);
        when(authenticationInfoLookuper.byLogin(token.getSubject())).thenReturn(authenticationInfo);
    }

    private void when_load_the_user() {
        user = service.loadUserByJOSE(token);
    }

    private void then_user_is_loaded_as_person() {
        assertThat(user, Matchers.notNullValue());
        assertThat(user, Matchers.instanceOf(IDPUser.class));
        assertThat(user.getApplication(), nullValue());
        assertThat(user.getPerson(), equalTo(person));
    }

    private void then_the_attributes_are_loaded_in_the_context() {
        assertThat(context.getContext(), Matchers.notNullValue());
        assertThat(context.getAttribute(Claims.CLIENT_ID.getValue()), equalTo(token.getClaim(Claims.CLIENT_ID.getValue())));
        assertThat(context.getAttribute(Claims.CLIENT_PROFILE.getValue()), equalTo(token.getClaim(Claims.CLIENT_PROFILE.getValue())));
        assertThat(context.getAttribute(Claims.ENTITY_ID.getValue()), equalTo(token.getClaim(Claims.ENTITY_ID.getValue())));
        assertThat(context.getAttribute(Claims.ACTION.getValue()), equalTo(token.getClaim(Claims.ACTION.getValue())));
        assertThat(context.getAttribute(Claims.APPLICATION.getValue()), equalTo(token.getClaim(Claims.APPLICATION.getValue())));
    }
}
