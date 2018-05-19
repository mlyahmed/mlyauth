package com.primasolutions.idp.authentication.sp.jose;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.authentication.AuthInfo;
import com.primasolutions.idp.authentication.AuthenticationInfoLookuper;
import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.context.IContextHolder;
import com.primasolutions.idp.context.IDPUser;
import com.primasolutions.idp.context.mocks.MockContextHolder;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.Person;
import com.primasolutions.idp.person.PersonDAO;
import com.primasolutions.idp.token.jose.mocks.MockJOSEAccessToken;
import com.primasolutions.idp.tools.KeysForTests;
import com.primasolutions.idp.tools.RandomForTests;
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
import java.util.Date;
import java.util.HashSet;

import static com.primasolutions.idp.token.Claims.ACTION;
import static com.primasolutions.idp.token.Claims.APPLICATION;
import static com.primasolutions.idp.token.Claims.CLIENT_ID;
import static com.primasolutions.idp.token.Claims.CLIENT_PROFILE;
import static com.primasolutions.idp.token.Claims.ENTITY_ID;
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
        given_the_person_exists();
        token.setClaim(APPLICATION.getValue(), null);
        when_load_the_user();
        then_user_is_loaded_as_person();
    }

    @Test
    public void when_the_application_claim_is_empty_then_OK() {
        given_the_person_exists();
        token.setClaim(APPLICATION.getValue(), "");
        when_load_the_user();
        then_user_is_loaded_as_person();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_person_is_not_found_then_error() {
        when(personDAO.findByExternalId(token.getSubject())).thenReturn(null);
        when_load_the_user();
    }

    private void given_the_token() {
        Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        token = new MockJOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
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
        token.setClaim(CLIENT_ID.getValue(), RandomForTests.randomString());
        token.setClaim(CLIENT_PROFILE.getValue(), RandomForTests.randomString());
        token.setClaim(ENTITY_ID.getValue(), RandomForTests.randomString());
        token.setClaim(ACTION.getValue(), RandomForTests.randomString());
        token.setClaim(APPLICATION.getValue(), RandomForTests.randomString());
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
        assertThat(context.getAttribute(CLIENT_ID.getValue()), equalTo(token.getClaim(CLIENT_ID.getValue())));
        assertThat(context.getAttribute(CLIENT_PROFILE.getValue()), equalTo(token.getClaim(CLIENT_PROFILE.getValue())));
        assertThat(context.getAttribute(ENTITY_ID.getValue()), equalTo(token.getClaim(ENTITY_ID.getValue())));
        assertThat(context.getAttribute(ACTION.getValue()), equalTo(token.getClaim(ACTION.getValue())));
        assertThat(context.getAttribute(APPLICATION.getValue()), equalTo(token.getClaim(APPLICATION.getValue())));
    }
}
