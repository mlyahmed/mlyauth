package com.mlyauth.sp.jose;

import com.google.common.collect.Iterators;
import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.constants.ApplicationType;
import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.NavigationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.domain.Navigation;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.jose.JOSETokenFactory;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.util.Base64URL;
import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;

import static com.mlyauth.constants.AspectAttribute.IDP_JOSE_ENTITY_ID;
import static com.mlyauth.constants.AspectType.IDP_JOSE;
import static com.mlyauth.constants.Direction.INBOUND;
import static com.mlyauth.domain.Application.newInstance;
import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SPJOSEPostAccessIT extends AbstractIntegrationTest {

    @Value("${sp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private JOSETokenFactory tokenFactory;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter joseFilter;

    private MockMvc mockMvc;
    private ResultActions resultActions;

    private Application application;
    private Pair<PrivateKey, X509Certificate> applicationCredentials;
    private String appname;
    private String entityId;
    private String ssoUrl;

    private JOSEAccessToken token;
    private String serialized;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).addFilters(joseFilter).build();
        applicationCredentials = KeysForTests.generateRSACredential();
        appname = "LinkAssuDev";
        entityId = randomString();
        ssoUrl = "http://localhost/idp/jose/sso";
    }

    @Test
    public void when_post_a_true_access_from_a_defined_idp_then_OK() {
        given_a_peer_jose_idp_app();
        given_a_peer_jose_idp_app_attributes();
        given_success_token();
        when_post_the_token();
        then_authenticated();
    }

    @Test
    public void when_post_a_true_access_from_a_defined_idp_then_trace_navigation() {
        navigationDAO.deleteAll();
        given_a_peer_jose_idp_app();
        given_a_peer_jose_idp_app_attributes();
        given_success_token();
        when_post_the_token();

        final Iterable<Navigation> navigations = navigationDAO.findAll();
        assertThat(navigations, notNullValue());
        assertThat(Iterators.size(navigations.iterator()), equalTo(1));

        final Navigation navigation = Iterators.getLast(navigations.iterator());
        assertThat(navigation.getCreatedAt(), notNullValue());
        assertThat(navigation.getDirection(), equalTo(INBOUND));
        assertThat(navigation.getTargetURL(), equalTo(token.getTargetURL()));
        assertThat(navigation.getToken(), notNullValue());
        assertThat(navigation.getToken().getChecksum(), equalTo(DigestUtils.sha256Hex(serialized)));
        assertThat(navigation.getSession(), notNullValue());
    }

    @Test
    public void when_post_success_token_with_application_claim_then_navigate_to_it() {
        given_a_peer_jose_idp_app();
        given_a_peer_jose_idp_app_attributes();
        given_success_token_with_application_claim_already_assigned();
        when_post_the_token();
        then_navigate_to_the_app();
    }

    @Test
    public void when_post_a_true_fail_from_a_defined_idp_then_Error() {
        given_a_peer_jose_idp_app();
        given_a_peer_jose_idp_app_attributes();
        given_fail_token();
        when_post_the_token();
        then_error();

    }

    private void given_a_peer_jose_idp_app() {
        application = newInstance()
                .setType(ApplicationType.STORE)
                .setAppname(appname)
                .setTitle(appname)
                .setAspects(new HashSet<>(asList(IDP_JOSE)));
        application = applicationDAO.save(application);
    }

    private void given_a_peer_jose_idp_app_attributes() {
        try {
            final ApplicationAspectAttribute entityIdAttribute = ApplicationAspectAttribute.newInstance()
                    .setId(ApplicationAspectAttributeId.newInstance()
                            .setApplicationId(application.getId())
                            .setAspectCode(IDP_JOSE.name())
                            .setAttributeCode(IDP_JOSE_ENTITY_ID.getValue()))
                    .setValue(entityId);

            final ApplicationAspectAttribute ssoUrlAttribute = ApplicationAspectAttribute.newInstance()
                    .setId(ApplicationAspectAttributeId.newInstance()
                            .setApplicationId(application.getId())
                            .setAspectCode(IDP_JOSE.name())
                            .setAttributeCode(AspectAttribute.IDP_JOSE_SSO_URL.getValue()))
                    .setValue(ssoUrl);

            final ApplicationAspectAttribute certificateAttribute = ApplicationAspectAttribute.newInstance()
                    .setId(ApplicationAspectAttributeId.newInstance()
                            .setApplicationId(application.getId())
                            .setAspectCode(IDP_JOSE.name())
                            .setAttributeCode(AspectAttribute.IDP_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                    .setValue(Base64URL.encode(applicationCredentials.getValue().getEncoded()).toString());


            appAspectAttrDAO.save(asList(entityIdAttribute, ssoUrlAttribute, certificateAttribute));
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private void given_success_token() {
        token = tokenFactory.createAccessToken(applicationCredentials.getKey(), credentialManager.getPublicKey());
        token.setStamp(randomString());
        token.setSubject(MASTER_EMAIL);
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(CLIENT_ID.getValue(), randomString());
        token.setClaim(CLIENT_PROFILE.getValue(), randomString());
        token.setClaim(ENTITY_ID.getValue(), randomString());
        token.setClaim(ACTION.getValue(), randomString());
        token.cypher();
    }

    private void given_fail_token() {
        token = tokenFactory.createAccessToken(applicationCredentials.getKey(), credentialManager.getPublicKey());
        token.setStamp(randomString());
        token.setSubject(MASTER_EMAIL);
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.FAIL);
        token.cypher();
    }

    private void given_success_token_with_application_claim_already_assigned() {
        token = tokenFactory.createAccessToken(applicationCredentials.getKey(), credentialManager.getPublicKey());
        token.setStamp(randomString());
        token.setSubject(MASTER_EMAIL);
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(CLIENT_ID.getValue(), randomString());
        token.setClaim(CLIENT_PROFILE.getValue(), randomString());
        token.setClaim(ENTITY_ID.getValue(), randomString());
        token.setClaim(ACTION.getValue(), randomString());
        token.setClaim(APPLICATION.getValue(), "PolicyDev");
        token.cypher();
    }

    private void when_post_the_token() {
        try {
            serialized = token.serialize();
            resultActions = mockMvc.perform(post("/sp/jose/sso")
                    .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                    .header("Authorization", "Bearer " + serialized));
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private void then_authenticated() {
        try {
            resultActions
                    .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", nullValue()))
                    .andExpect(redirectedUrl("/home.html"));
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private void then_navigate_to_the_app() {
        try {
            resultActions
                    .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", nullValue()))
                    .andExpect(redirectedUrl("/navigate/forward/to/PolicyDev"));
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private void then_error() {
        try {
            resultActions
                    .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", notNullValue()))
                    .andExpect(forwardedUrl("/401.html"));
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }
}
