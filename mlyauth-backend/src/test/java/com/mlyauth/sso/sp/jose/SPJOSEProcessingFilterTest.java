package com.mlyauth.sso.sp.jose;

import com.mlyauth.constants.AspectType;
import com.mlyauth.credentials.MockCredentialManager;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSEHelper;
import com.mlyauth.token.jose.MockJOSEAccessTokenValidator;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static com.mlyauth.tools.KeysForTests.generateRSACredential;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class SPJOSEProcessingFilterTest {

    public static final String PEER_IDP_ID = "peerId";

    @Mock
    private AuthenticationManager authenticationManager;

    private SPJOSEProcessingFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockCredentialManager credentialManager;

    private Authentication expectedAuthentication;

    private JOSEAccessToken token;
    private Pair<PrivateKey, X509Certificate> localCredential;
    private Pair<PrivateKey, X509Certificate> peerCredential;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        set_up_authentication_manager();
        set_up_credentials();
        set_up_token();
        set_up_request_response();
        set_up_filter();
    }

    @Test
    public void when_post_a_valid_token_then_process() {
        token.setIssuer(PEER_IDP_ID);
        token.cypher();
        request.setMethod(HttpMethod.POST.name());
        request.addHeader("Authorization", "Bearer " + token.serialize());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.notNullValue());
        assertThat(authentication, Matchers.equalTo(expectedAuthentication));
    }

    private void set_up_authentication_manager() {
        expectedAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(Mockito.any())).thenReturn(expectedAuthentication);
    }

    private void set_up_credentials() {
        localCredential = generateRSACredential();
        peerCredential = generateRSACredential();
        credentialManager = new MockCredentialManager(localCredential.getKey(), localCredential.getValue().getPublicKey());
        credentialManager.setPeerCertificate(PEER_IDP_ID, AspectType.IDP_JOSE, peerCredential.getValue());
    }

    private void set_up_token() {
        token = new JOSEAccessToken(peerCredential.getKey(), credentialManager.getLocalPublicKey());
    }

    private void set_up_request_response() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI("/sp/jose/sso");
    }

    private void set_up_filter() {
        filter = new SPJOSEProcessingFilter();
        setField(filter, "credentialManager", credentialManager);
        setField(filter, "joseHelper", new JOSEHelper());
        setField(filter, "accessTokenValidator", new MockJOSEAccessTokenValidator(true));
        setField(filter, "authenticationManager", authenticationManager);
    }
}