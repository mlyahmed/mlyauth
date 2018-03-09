package com.mlyauth.sso.sp.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.context.MockContextHolder;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.token.jose.MockJOSEAccessToken;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;

import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SPJOSEUserDetailsServiceImplTest {

    @Spy
    private IContextHolder contextHolder = new MockContextHolder();

    @Mock
    private PersonDAO personDAO;

    @InjectMocks
    private SPJOSEUserDetailsServiceImpl service;

    private MockJOSEAccessToken token;
    private Person person;
    private AuthenticationInfo authenticationInfo;
    private IDPUser user;

    @Test(expected = IllegalArgumentException.class)
    public void when_the_token_is_null_then_error() {
        SPJOSEUserDetailsServiceImpl service = new SPJOSEUserDetailsServiceImpl();
        service.loadUserByJOSE(null);
    }

    @Test
    public void when_the_token_is_valid_then_return_the_user() {
        MockitoAnnotations.initMocks(this);
        given_the_token();
        given_the_person_exists();
        user = service.loadUserByJOSE(token);
        then_user_is_loaded();
    }

    private void given_the_token() {
        Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        token = new MockJOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
        token.setId(randomString());
        token.setSubject(randomString());
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP(randomString());
        token.setState(randomString());
        token.setAudience(randomString());
        token.setIssuer(randomString());
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL(randomString());
        token.setVerdict(TokenVerdict.SUCCESS);
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

    private void then_user_is_loaded() {
        assertThat(user, Matchers.notNullValue());
        assertThat(user, Matchers.instanceOf(IDPUser.class));
        assertThat(user.getPerson(), Matchers.equalTo(person));
    }
}