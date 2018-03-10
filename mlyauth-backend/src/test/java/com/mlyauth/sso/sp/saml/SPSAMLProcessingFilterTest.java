package com.mlyauth.sso.sp.saml;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.common.SAMLRuntimeException;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.processor.SAMLProcessor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;
import static org.springframework.http.HttpMethod.*;

@RunWith(DataProviderRunner.class)
public class SPSAMLProcessingFilterTest {

    @Mock
    protected SAMLProcessor processor;

    @Mock
    protected SAMLContextProvider contextProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private SPSAMLProcessingFilter filter;
    private Authentication expectedAuthentication;

    @DataProvider
    public static Object[] mothodsNotAllowed() {
        // @formatter:off
        return new HttpMethod[]{GET, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE};
        // @formatter:on
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        set_up_request_and_response();
        set_up_the_the_filter();
        set_up_saml_message_context();
        set_up_authentication_manager();
    }

    @Test
    @UseDataProvider("mothodsNotAllowed")
    public void when_the_method_is_not_allowed_then_error(HttpMethod method) {
        request.setMethod(method.name());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.nullValue());
        assertThat(response.getStatus(), Matchers.equalTo(HttpServletResponse.SC_NOT_FOUND));
    }


    @Test
    public void when_the_method_is_POST_then_process() throws Exception {
        request.setMethod(HttpMethod.POST.name());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.notNullValue());
        assertThat(authentication, Matchers.equalTo(expectedAuthentication));
    }

    @Test(expected = SAMLRuntimeException.class)
    public void when_response_processing_error__throws_error_then_throw_saml_error() {
        request.setMethod(HttpMethod.GET.name());
        response = new MockHttpServletResponse() {
            @Override
            public void sendError(int status, String errorMessage) throws IOException {
                throw new IOException();
            }
        };
        filter.attemptAuthentication(request, response);
    }

    private void set_up_request_and_response() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI("/sp/sso/sso");
    }

    private void set_up_the_the_filter() {
        filter = new SPSAMLProcessingFilter();
        filter.setSAMLProcessor(processor);
        filter.setContextProvider(contextProvider);
        filter.setAuthenticationManager(authenticationManager);
    }

    private void set_up_saml_message_context() throws MetadataProviderException {
        Endpoint endpoint = new AssertionConsumerServiceBuilder().buildObject("", "", "");
        endpoint.setBinding(SAML2_POST_BINDING_URI);
        endpoint.setLocation("http://localhost/sp/sso/sso");
        SAMLMessageContext context = new SAMLMessageContext();
        RoleDescriptor localEntityMetadata = mock(RoleDescriptor.class);
        when(localEntityMetadata.getEndpoints()).thenReturn(Arrays.asList(endpoint));
        context.setLocalEntityRoleMetadata(localEntityMetadata);
        context.setInboundSAMLBinding(SAML2_POST_BINDING_URI);
        context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
        when(contextProvider.getLocalEntity(request, response)).thenReturn(context);
    }

    private void set_up_authentication_manager() {
        expectedAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(Mockito.any())).thenReturn(expectedAuthentication);
    }

}
