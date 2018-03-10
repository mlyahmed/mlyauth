package com.mlyauth.sso.sp.jose;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.token.ITokenFactory;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.util.Base64URL;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashSet;

import static com.mlyauth.constants.AspectType.IDP_JOSE;
import static com.mlyauth.domain.Application.newInstance;
import static com.mlyauth.token.IDPClaims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private ITokenFactory tokenFactory;

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

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).addFilters(joseFilter).build();
        applicationCredentials = KeysForTests.generateRSACredential();
        appname = "LinkAssuDev";
        entityId = randomString();
        ssoUrl = "http://localhost/idp/jose/sso";
    }

    @Test
    public void when_post_a_true_access_from_a_defined_idp_then_OK() throws Exception {
        given_a_peer_jose_idp_app();
        given_a_peer_jose_idp_app_attributes();
        given_success_token();
        when_post_the_token();
        then_authenticated();

    }


    @Test
    public void when_post_a_true_fail_from_a_defined_idp_then_Error() throws Exception {
        given_a_peer_jose_idp_app();
        given_a_peer_jose_idp_app_attributes();
        given_fail_token();
        when_post_the_token();
        then_error();

    }


    //TODO: When post and application claim is set. Then navigate to the application

    private void given_a_peer_jose_idp_app() {
        application = newInstance().setAppname(appname).setTitle(appname).setAspects(new HashSet<>(asList(IDP_JOSE)));
        application = applicationDAO.save(application);
    }

    private void given_a_peer_jose_idp_app_attributes() throws CertificateEncodingException {
        final ApplicationAspectAttribute entityIdAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(application.getId())
                        .setAspectCode(IDP_JOSE.name())
                        .setAttributeCode(AspectAttribute.IDP_JOSE_ENTITY_ID.getValue()))
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
    }

    private void given_success_token() {
        token = tokenFactory.createJOSEAccessToken(applicationCredentials.getKey(), credentialManager.getLocalPublicKey());
        token.setId(randomString());
        token.setSubject("1");
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP(randomString());
        token.setState(randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL(ssoUrl);
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(CLIENT_ID.getValue(), randomString());
        token.setClaim(CLIENT_PROFILE.getValue(), randomString());
        token.setClaim(ENTITY_ID.getValue(), randomString());
        token.setClaim(ACTION.getValue(), randomString());
        token.cypher();
    }

    private void given_fail_token() {
        token = tokenFactory.createJOSEAccessToken(applicationCredentials.getKey(), credentialManager.getLocalPublicKey());
        token.setId(randomString());
        token.setSubject("1");
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP(randomString());
        token.setState(randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL(ssoUrl);
        token.setVerdict(TokenVerdict.FAIL);
        token.cypher();
    }

    private void when_post_the_token() throws Exception {
        resultActions = mockMvc.perform(post("/sp/jose/sso")
                .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Bearer " + token.serialize()));
    }

    private void then_authenticated() throws Exception {
        resultActions
                .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", nullValue()))
                .andExpect(redirectedUrl("/home.html"));
    }

    private void then_error() throws Exception {
        resultActions
                .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", notNullValue()))
                .andExpect(forwardedUrl("/error.html"));
    }
}
