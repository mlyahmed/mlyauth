package com.mlyauth.navigation;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AspectType;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationSession;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.ApplicationNotFoundException;
import com.mlyauth.exception.NotSPSAMLApplicationException;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.token.saml.SAMLAccessTokenProducer;
import com.mlyauth.token.saml.SAMLHelper;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class IDPSAMLNavigationServiceTest {

    public static final String TARGET_APP = "TargetApp";
    public static final String TARGET_APP_URL = "http://application.com/sp/saml/sso";

    @Mock
    private IContext context;

    @Mock
    private TokenMapper tokenMapper;

    @Mock
    private TokenDAO tokenDAO;

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private SAMLAccessTokenProducer responseGenerator;

    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    @InjectMocks
    private IDPSAMLNavigationService service;

    private SAMLAccessToken access;

    private Application application;

    @Before
    public void setup() throws ConfigurationException {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        application = new Application();
        when(context.getAuthenticationSession()).thenReturn(AuthenticationSession.newInstance());
        set_up_access();
        set_up_token();
    }

    private void set_up_access() {
        final Pair<PrivateKey, X509Certificate> pair = KeysForTests.generateRSACredential();
        access = new SAMLAccessToken(samlHelper.toCredential(pair.getKey(), pair.getValue()));
        access.setTargetURL(TARGET_APP_URL);
        access.cypher();
        when(responseGenerator.produce(application)).thenReturn(access);
    }

    private void set_up_token() {
        final Token token = Token.newInstance();
        when(tokenMapper.toToken(this.access)).thenReturn(token);
        when(tokenDAO.save(token)).thenReturn(token);
    }

    @Test
    public void when_create_new_navigation_to_an_assigned_app_then_return_it() {
        application.setAspects(new HashSet<>(Arrays.asList(AspectType.SP_SAML)));
        when(applicationDAO.findByAppname(TARGET_APP)).thenReturn(application);
        final NavigationBean navigation = service.newNavigation(TARGET_APP);
        assertThat(navigation, notNullValue());
        assertThat(navigation.getTarget(), equalTo(TARGET_APP_URL));
        assertThat(navigation.getAttributes(), hasSize(1));
        assertThat(navigation.getAttribute("SAMLResponse"), notNullValue());
        assertThat(navigation.getAttribute("SAMLResponse").getValue(), equalTo(access.serialize()));
    }

    @Test(expected = NotSPSAMLApplicationException.class)
    public void when_create_new_navigation_to_unexisting_then_error() {
        application.setAspects(new HashSet<>(Arrays.asList()));
        when(applicationDAO.findByAppname(TARGET_APP)).thenReturn(application);
        service.newNavigation(TARGET_APP);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void when_application_does_not_exist_then_error() {
        service.newNavigation(TARGET_APP);
    }

    //TODO app saml attributes misses
}
