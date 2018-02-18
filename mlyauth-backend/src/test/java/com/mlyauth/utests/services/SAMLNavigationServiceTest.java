package com.mlyauth.utests.services;

import com.mlyauth.beans.AuthNavigation;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.exception.ApplicationNotFound;
import com.mlyauth.exception.NotSPSAMLApplication;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.SAMLResponseGenerator;
import com.mlyauth.services.SAMLNavigationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.opensaml.xml.util.Base64.encodeBytes;

public class SAMLNavigationServiceTest {

    public static final String TARGET_APP = "TargetApp";
    public static final String TARGET_APP_URL = "http://application.com/sp/saml/sso";

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private SAMLResponseGenerator responseGenerator;

    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    @InjectMocks
    private SAMLNavigationService service;

    private Response response;
    private Application application;

    @Before
    public void setup() throws ConfigurationException {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        application = new Application();
        response = samlHelper.buildSAMLObject(Response.class);
        response.setDestination(TARGET_APP_URL);
        when(responseGenerator.generate(application)).thenReturn(response);
    }

    @Test
    public void when_create_new_navigation_to_an_assigned_app_then_return_it() {
        application.setAspects(new HashSet<>(Arrays.asList(AuthAspectType.SP_SAML)));
        when(applicationDAO.findByAppname(TARGET_APP)).thenReturn(application);
        final AuthNavigation authNavigation = service.newNavigation(TARGET_APP);
        assertThat(authNavigation, notNullValue());
        assertThat(authNavigation.getTarget(), equalTo(TARGET_APP_URL));
        assertThat(authNavigation.getAttributes(), hasSize(1));
        assertThat(authNavigation.getAttribute("SAMLResponse"), notNullValue());
        assertThat(authNavigation.getAttribute("SAMLResponse").getValue(), equalTo(encodeBytes(samlHelper.toString(response).getBytes())));
    }

    @Test(expected = NotSPSAMLApplication.class)
    public void when_create_new_navigation_to_unexisting_then_error() {
        application.setAspects(new HashSet<>(Arrays.asList()));
        when(applicationDAO.findByAppname(TARGET_APP)).thenReturn(application);
        service.newNavigation(TARGET_APP);
    }

    @Test(expected = ApplicationNotFound.class)
    public void when_application_does_not_exist_then_error() {
        service.newNavigation(TARGET_APP);
    }

    // app saml attributes misses
}
