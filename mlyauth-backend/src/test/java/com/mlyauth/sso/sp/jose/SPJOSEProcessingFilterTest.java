package com.mlyauth.sso.sp.jose;

import com.mlyauth.constants.AspectType;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.credentials.MockCredentialManager;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSEHelper;
import com.mlyauth.token.jose.MockJOSEAccessTokenValidator;
import com.mlyauth.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;

import static com.mlyauth.constants.TokenScope.POLICY;
import static com.mlyauth.tools.KeysForTests.generateRSACredential;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(DataProviderRunner.class)
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

    @DataProvider
    public static Object[] methodsNotAllowed() {
        // @formatter:off
        return new HttpMethod[]{GET, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE};
        // @formatter:on
    }

    @Test
    @UseDataProvider("methodsNotAllowed")
    public void when_the_method_is_not_POST_then_error(HttpMethod method) {
        request.setMethod(method.name());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.nullValue());
        assertThat(response.getStatus(), Matchers.equalTo(HttpServletResponse.SC_NOT_FOUND));
    }

    @Test
    public void when_post_a_valid_token_then_process() {
        given_valid_token();
        final Authentication authentication = when_attempt_authentication();
        assertThat(authentication, Matchers.notNullValue());
        assertThat(authentication, Matchers.equalTo(expectedAuthentication));
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_scopes_list_is_null_then_error() {
        given_scopes_list_is_null();
        when_attempt_authentication();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_scopes_list_is_more_then_one_then_error() {
        given_scopes_list_is_more_then_one();
        when_attempt_authentication();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_scopes_list_is_not_person_then_error() {
        given_the_scopes_list_is_not_person();
        when_attempt_authentication();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_BP_is_not_SSO_then_error() {
        given_token_with_bp_not_as_sso();
        when_attempt_authentication();
    }



    //TODO Test Target URL
    //TODO Test token is not Bearer


    private void set_up_credentials() {
        localCredential = generateRSACredential();
        peerCredential = generateRSACredential();
        credentialManager = new MockCredentialManager(localCredential.getKey(), localCredential.getValue().getPublicKey());
        credentialManager.setPeerCertificate(PEER_IDP_ID, AspectType.IDP_JOSE, peerCredential.getValue());
    }

    private void set_up_authentication_manager() {
        expectedAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(Mockito.any())).thenReturn(expectedAuthentication);
    }
    private void set_up_token() {
        token = new JOSEAccessToken(peerCredential.getKey(), credentialManager.getLocalPublicKey());
    }
    private void set_up_request_response() {
        request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
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

    private void given_valid_token() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(singletonList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.cypher();
    }

    private void given_scopes_list_is_null() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(null);
        token.setBP("SSO");
        token.cypher();
    }

    private void given_scopes_list_is_more_then_one() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        token.setBP("SSO");
        token.cypher();
    }

    private void given_the_scopes_list_is_not_person() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(singletonList(POLICY)));
        token.setBP("SSO");
        token.cypher();
    }

    private void given_token_with_bp_not_as_sso() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(singletonList(TokenScope.PERSON)));
        token.setBP(RandomForTests.randomString());
        token.cypher();
    }

    private Authentication when_attempt_authentication() {
        request.addHeader("Authorization", "Bearer " + token.serialize());
        return filter.attemptAuthentication(request, response);
    }
}