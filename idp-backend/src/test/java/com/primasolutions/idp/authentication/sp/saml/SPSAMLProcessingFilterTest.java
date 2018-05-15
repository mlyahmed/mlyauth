package com.primasolutions.idp.authentication.sp.saml;

import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.navigation.Navigation;
import com.primasolutions.idp.navigation.NavigationDAO;
import com.primasolutions.idp.token.Token;
import com.primasolutions.idp.token.TokenDAO;
import com.primasolutions.idp.token.TokenMapper;
import com.primasolutions.idp.token.saml.SAMLAccessToken;
import com.primasolutions.idp.token.saml.SAMLHelper;
import com.primasolutions.idp.token.saml.SAMLTokenFactory;
import com.primasolutions.idp.tools.KeysForTests;
import com.primasolutions.idp.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.metadata.MetadataMemoryProvider;
import org.springframework.security.saml.processor.SAMLProcessor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.TRACE;

@RunWith(DataProviderRunner.class)
public class SPSAMLProcessingFilterTest {

    @Mock
    private IContext context;

    @Mock
    private TokenMapper tokenMapper;

    @Mock
    private SAMLTokenFactory tokenFactory;

    @Mock
    private TokenDAO tokenDAO;

    @Mock
    private NavigationDAO navigationDAO;

    @Mock
    private KeyManager keyManager;

    @Mock
    private MetadataManager metadataManager;

    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    @Mock
    protected SAMLProcessor processor;

    @Mock
    protected SAMLContextProvider contextProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SAMLAccessToken accessToken;

    @InjectMocks
    private SPSAMLProcessingFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Authentication expectedAuthentication;
    private SAMLMessageContext messageContext;
    private EntityDescriptor idpMetadata;

    @DataProvider
    public static Object[] mothodsNotAllowed() {
        // @formatter:off
        return new HttpMethod[]{GET, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE};
        // @formatter:on
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        DefaultBootstrap.bootstrap();
        set_up_request_and_response();
        set_up_the_the_filter();
        set_up_authentication_manager();
        set_up_idp();
        set_up_message_context();
        set_up_token_dao_and_factory();
    }

    @Test
    @UseDataProvider("mothodsNotAllowed")
    public void when_the_method_is_not_allowed_then_error(final HttpMethod method) {
        request.setMethod(method.name());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.nullValue());
        assertThat(response.getStatus(), Matchers.equalTo(HttpServletResponse.SC_NOT_FOUND));
    }

    @Test
    public void when_the_method_is_POST_then_process() {
        request.setParameter("SAMLResponse", RandomForTests.randomString());
        request.setMethod(HttpMethod.POST.name());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.notNullValue());
        assertThat(authentication, Matchers.equalTo(expectedAuthentication));
    }

    @Test
    public void when_post_a_success_response_then_trace_navigation() {
        request.setParameter("SAMLResponse", RandomForTests.randomString());
        request.setMethod(HttpMethod.POST.name());
        filter.attemptAuthentication(request, response);
        Mockito.verify(tokenDAO).save(Mockito.any(Token.class));
        Mockito.verify(navigationDAO).save(Mockito.any(Navigation.class));
    }

    @Test(expected = AuthenticationException.class)
    public void when_response_processing_error_throws_error_then_throw_saml_error() {
        request.setMethod(HttpMethod.GET.name());
        response = new MockHttpServletResponse() {
            @Override
            public void sendError(final int status, final String errorMessage) throws IOException {
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
        filter.setSAMLProcessor(processor);
        filter.setContextProvider(contextProvider);
        filter.setAuthenticationManager(authenticationManager);
    }

    private void set_up_authentication_manager() {
        expectedAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(Mockito.any())).thenReturn(expectedAuthentication);
    }

    private void set_up_token_dao_and_factory() {
        when(tokenFactory.createAccessToken(anyString(), Mockito.any(Credential.class))).thenReturn(accessToken);
        final Token token = Token.newInstance();
        when(tokenMapper.toToken(accessToken)).thenReturn(token);
        when(tokenDAO.save(token)).thenReturn(token);
        when(navigationDAO.save(Mockito.any(Navigation.class))).thenReturn(Navigation.newInstance());
    }

    private void set_up_message_context() throws MetadataProviderException {
        Endpoint endpoint = new AssertionConsumerServiceBuilder().buildObject("", "", "");
        endpoint.setBinding(SAML2_POST_BINDING_URI);
        endpoint.setLocation("http://localhost/sp/sso/sso");
        RoleDescriptor localEntityMetadata = mock(RoleDescriptor.class);
        when(localEntityMetadata.getEndpoints()).thenReturn(Arrays.asList(endpoint));
        final MetadataMemoryProvider metadataProvider = new MetadataMemoryProvider(idpMetadata);
        MetadataCredentialResolver metadataResolver = new MetadataCredentialResolver(metadataProvider);
        SignatureTrustEngine engine = new ExplicitKeySignatureTrustEngine(metadataResolver,
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        messageContext = new SAMLMessageContext();
        messageContext.setLocalEntityRoleMetadata(localEntityMetadata);
        messageContext.setInboundSAMLBinding(SAML2_POST_BINDING_URI);
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));
        messageContext.setPeerEntityId(idpMetadata.getEntityID());
        messageContext.setLocalTrustEngine(engine);
        when(contextProvider.getLocalEntity(request, response)).thenReturn(messageContext);
        when(contextProvider.getLocalAndPeerEntity(request, response)).thenReturn(messageContext);
        when(metadataManager.getEntityDescriptor(messageContext.getPeerEntityId())).thenReturn(idpMetadata);
    }

    private void set_up_idp() throws SecurityException {
        final Pair<PrivateKey, X509Certificate> localPair = KeysForTests.generateRSACredential();
        final Credential localCredential = samlHelper.toCredential(localPair.getKey(), localPair.getValue());
        when(keyManager.getDefaultCredential()).thenReturn(localCredential);

        final Pair<PrivateKey, X509Certificate> peerPair = KeysForTests.generateRSACredential();
        final Credential peerCredential = samlHelper.toCredential(peerPair.getKey(), peerPair.getValue());

        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
        KeyDescriptor signKeyDescriptor = samlHelper.buildSAMLObject(KeyDescriptor.class);
        signKeyDescriptor.setUse(UsageType.SIGNING);
        signKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(peerCredential));


        idpMetadata = samlHelper.buildSAMLObject(EntityDescriptor.class);
        IDPSSODescriptor idpDescriptor = samlHelper.buildSAMLObject(IDPSSODescriptor.class);
        idpDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        idpDescriptor.getKeyDescriptors().add(signKeyDescriptor);

        idpMetadata.getRoleDescriptors().add(idpDescriptor);
        idpMetadata.setEntityID(RandomForTests.randomString());
    }

}
