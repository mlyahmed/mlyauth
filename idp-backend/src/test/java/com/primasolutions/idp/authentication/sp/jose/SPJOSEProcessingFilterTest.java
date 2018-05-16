package com.primasolutions.idp.authentication.sp.jose;

import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.context.mocks.MockContext;
import com.primasolutions.idp.credentials.mocks.MockCredentialManager;
import com.primasolutions.idp.navigation.Navigation;
import com.primasolutions.idp.navigation.NavigationDAO;
import com.primasolutions.idp.token.Token;
import com.primasolutions.idp.token.TokenDAO;
import com.primasolutions.idp.token.TokenMapper;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
import com.primasolutions.idp.token.jose.JOSEHelper;
import com.primasolutions.idp.token.jose.JOSETokenDecoder;
import com.primasolutions.idp.token.jose.JOSETokenFactory;
import com.primasolutions.idp.token.jose.mocks.MockJOSEAccessTokenValidator;
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

import static com.primasolutions.idp.constants.TokenScope.POLICY;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.TRACE;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(DataProviderRunner.class)
public class SPJOSEProcessingFilterTest {

    public static final String PEER_IDP_ID = "peerId";
    public static final int SERVER_PORT = 443;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenDAO tokenDAO;

    @Mock
    private NavigationDAO navigationDAO;

    private SPJOSEProcessingFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockCredentialManager credentialManager;

    private Authentication expectedAuthentication;

    private JOSEAccessToken token;
    private Pair<PrivateKey, X509Certificate> localCred;
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
    public void when_the_method_is_not_POST_then_error(final HttpMethod method) {
        request.setMethod(method.name());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.nullValue());
        assertThat(response.getStatus(), Matchers.equalTo(HttpServletResponse.SC_NOT_FOUND));
    }

    @Test
    public void when_post_in_header_a_valid_bearer_token_then_process() {
        given_valid_token();
        given_the_target_url_does_match();
        final Authentication authentication = when_attempt_authentication_as_in_header_bearer();
        assertThat(authentication, Matchers.notNullValue());
        assertThat(authentication, Matchers.equalTo(expectedAuthentication));
    }

    @Test
    public void when_post_as_form_a_valid_bearer_token_then_process() {
        given_valid_token();
        given_the_target_url_does_match();
        request.setParameter("Bearer", token.serialize());
        final Authentication authentication = filter.attemptAuthentication(request, response);
        assertThat(authentication, Matchers.notNullValue());
        assertThat(authentication, Matchers.equalTo(expectedAuthentication));
    }

    @Test
    public void when_post_as_valid_bearer_token_then_trace_navigation() {
        given_valid_token();
        given_the_target_url_does_match();
        when_attempt_authentication_as_in_header_bearer();
        verify(tokenDAO).save(Mockito.any(Token.class));
        verify(navigationDAO).save(Mockito.any(Navigation.class));
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_scopes_list_is_null_then_error() {
        given_scopes_list_is_null();
        when_attempt_authentication_as_in_header_bearer();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_scopes_list_is_more_then_one_then_error() {
        given_scopes_list_is_more_then_one();
        when_attempt_authentication_as_in_header_bearer();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_scopes_list_is_not_person_then_error() {
        given_the_scopes_list_is_not_person();
        when_attempt_authentication_as_in_header_bearer();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_BP_is_not_SSO_then_error() {
        given_token_with_bp_not_as_sso();
        when_attempt_authentication_as_in_header_bearer();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_target_url_does_not_match_the_request_then_error() {
        given_valid_token();
        given_the_target_url_does_not_match();
        when_attempt_authentication_as_in_header_bearer();
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_token_type_is_not_bearer_then_error() {
        given_valid_token();
        given_the_target_url_does_match();
        request.addHeader("Authorization", RandomForTests.randomString() + " " + token.serialize());
        filter.attemptAuthentication(request, response);
    }

    @Test(expected = AuthenticationException.class)
    public void when_the_authorization_header_is_missing_then_error() {
        given_valid_token();
        given_the_target_url_does_match();
        filter.attemptAuthentication(request, response);
    }

    private void set_up_credentials() {
        localCred = KeysForTests.generateRSACredential();
        peerCredential = KeysForTests.generateRSACredential();
        credentialManager = new MockCredentialManager(localCred.getKey(), localCred.getValue().getPublicKey());
        credentialManager.setPeerCertificate(PEER_IDP_ID, AspectType.IDP_JOSE, peerCredential.getValue());
    }

    private void set_up_authentication_manager() {
        expectedAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(Mockito.any())).thenReturn(expectedAuthentication);
    }

    private void set_up_token() {
        token = new JOSEAccessToken(peerCredential.getKey(), credentialManager.getPublicKey());
    }

    private void set_up_request_response() {
        request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        response = new MockHttpServletResponse();
        request.setRequestURI("/sp/jose/sso");
    }

    private void set_up_filter() {
        final TokenMapper tokenMapper = new TokenMapper();

        final JOSETokenDecoder tokenDecoder = new JOSETokenDecoder();
        setField(tokenDecoder, "tokenFactory", new JOSETokenFactory());
        setField(tokenDecoder, "joseHelper", new JOSEHelper());
        setField(tokenDecoder, "credentialManager", credentialManager);


        filter = new SPJOSEProcessingFilter();
        setField(filter, "accessTokenValidator", new MockJOSEAccessTokenValidator(true));
        setField(filter, "authenticationManager", authenticationManager);
        setField(filter, "tokenMapper", tokenMapper);
        setField(filter, "context", new MockContext());
        setField(filter, "tokenDAO", tokenDAO);
        setField(filter, "navigationDAO", navigationDAO);
        setField(filter, "tokenDecoder", tokenDecoder);
    }
    private void given_valid_token() {
        token.setStamp(RandomForTests.randomString());
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(singletonList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setTargetURL("https://sp.prima-idp.com/sp/jose/sso");
        token.cypher();
    }

    private void given_scopes_list_is_null() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(null);
        token.setBP("SSO");
        token.setTargetURL("https://sp.prima-idp.com/sp/jose/sso");
        token.cypher();
    }

    private void given_scopes_list_is_more_then_one() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        token.setBP("SSO");
        token.setTargetURL("https://sp.prima-idp.com/sp/jose/sso");
        token.cypher();
    }

    private void given_the_scopes_list_is_not_person() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(singletonList(POLICY)));
        token.setBP("SSO");
        token.setTargetURL("https://sp.prima-idp.com/sp/jose/sso");
        token.cypher();
    }

    private void given_token_with_bp_not_as_sso() {
        token.setIssuer(PEER_IDP_ID);
        token.setScopes(new HashSet<>(singletonList(TokenScope.PERSON)));
        token.setBP(RandomForTests.randomString());
        token.setTargetURL("https://sp.prima-idp.com/sp/jose/sso");
        token.cypher();
    }

    private void given_the_target_url_does_match() {
        request.setScheme("https");
        request.setServerName("sp.prima-idp.com");
        request.setServerPort(SERVER_PORT);
        request.setRequestURI("/sp/jose/sso");
    }

    private void given_the_target_url_does_not_match() {
        request.setScheme("http");
        request.setServerName(RandomForTests.randomString());
        request.setServerPort(SERVER_PORT);
        request.setRequestURI(RandomForTests.randomString());
    }

    private Authentication when_attempt_authentication_as_in_header_bearer() {
        request.addHeader("Authorization", "Bearer " + token.serialize());
        return filter.attemptAuthentication(request, response);
    }
}
