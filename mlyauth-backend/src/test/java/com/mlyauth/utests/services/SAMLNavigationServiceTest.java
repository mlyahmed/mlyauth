package com.mlyauth.utests.services;

import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.SAMLResponseGenerator;
import com.mlyauth.services.SAMLNavigationService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;

import static org.mockito.Mockito.when;

public class SAMLNavigationServiceTest {

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private SAMLResponseGenerator responseGenerator;

    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    @InjectMocks
    private SAMLNavigationService service;

    @Test
    public void when_create_new_navigation_to_an_assigned_app_then_return_it() throws ConfigurationException {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        final Response response = samlHelper.buildSAMLObject(Response.class);
        Application targetApp = new Application();
        when(applicationDAO.findByAppname("Target")).thenReturn(targetApp);
        when(responseGenerator.generate(targetApp)).thenReturn(response);
    }
}
