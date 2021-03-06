package com.hohou.federation.idp.authentication.sp.jose;

import com.google.common.collect.Iterators;
import com.hohou.federation.idp.AbstractIntegrationTest;
import com.hohou.federation.idp.application.AppAspAttr;
import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.application.ApplicationAspectAttributeDAO;
import com.hohou.federation.idp.application.ApplicationAspectAttributeId;
import com.hohou.federation.idp.application.ApplicationDAO;
import com.hohou.federation.idp.application.ApplicationTypeDAO;
import com.hohou.federation.idp.constants.ApplicationTypeCode;
import com.hohou.federation.idp.constants.AspectAttribute;
import com.hohou.federation.idp.constants.AspectType;
import com.hohou.federation.idp.constants.Direction;
import com.hohou.federation.idp.constants.TokenScope;
import com.hohou.federation.idp.constants.TokenVerdict;
import com.hohou.federation.idp.credentials.CredentialManager;
import com.hohou.federation.idp.credentials.CredentialsPair;
import com.hohou.federation.idp.exception.JOSEErrorExc;
import com.hohou.federation.idp.navigation.Navigation;
import com.hohou.federation.idp.navigation.NavigationDAO;
import com.hohou.federation.idp.token.Claims;
import com.hohou.federation.idp.token.jose.JOSEAccessToken;
import com.hohou.federation.idp.token.jose.JOSETokenFactoryImpl;
import com.hohou.federation.idp.tools.KeysForTests;
import com.hohou.federation.idp.tools.RandomForTests;
import com.nimbusds.jose.util.Base64URL;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.HashSet;

import static com.hohou.federation.idp.constants.AspectAttribute.IDP_JOSE_ENTITY_ID;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SPJOSEPostAccessIT extends AbstractIntegrationTest {

    @Value("${sp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private ApplicationTypeDAO applicationTypeDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private JOSETokenFactoryImpl tokenFactory;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Filter joseFilter;

    private MockMvc mockMvc;
    private ResultActions resultActions;

    private Application application;
    private CredentialsPair applicationCredentials;
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
        entityId = RandomForTests.randomString();
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
        assertThat(navigation.getDirection(), equalTo(Direction.INBOUND));
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
        application = Application.newInstance()
                .setType(applicationTypeDAO.findById(ApplicationTypeCode.STORE).get())
                .setAppname(appname)
                .setTitle(appname)
                .setAspects(new HashSet<>(asList(AspectType.IDP_JOSE)));
        application = applicationDAO.save(application);
    }

    private void given_a_peer_jose_idp_app_attributes() {
        try {
            final AppAspAttr entityIdAttribute = AppAspAttr.newInstance()
                    .setId(ApplicationAspectAttributeId.newInstance()
                            .setApplicationId(application.getId())
                            .setAspectCode(AspectType.IDP_JOSE.name())
                            .setAttributeCode(IDP_JOSE_ENTITY_ID.getValue()))
                    .setValue(entityId);

            final AppAspAttr ssoUrlAttribute = AppAspAttr.newInstance()
                    .setId(ApplicationAspectAttributeId.newInstance()
                            .setApplicationId(application.getId())
                            .setAspectCode(AspectType.IDP_JOSE.name())
                            .setAttributeCode(AspectAttribute.IDP_JOSE_SSO_URL.getValue()))
                    .setValue(ssoUrl);

            final AppAspAttr certificateAttribute = AppAspAttr.newInstance()
                    .setId(ApplicationAspectAttributeId.newInstance()
                            .setApplicationId(application.getId())
                            .setAspectCode(AspectType.IDP_JOSE.name())
                            .setAttributeCode(AspectAttribute.IDP_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                    .setValue(Base64URL.encode(applicationCredentials.getCertificate().getEncoded()).toString());


            appAspectAttrDAO.saveAll(asList(entityIdAttribute, ssoUrlAttribute, certificateAttribute));
        } catch (Exception e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    private void given_success_token() {
        token = tokenFactory.newAccessToken(applicationCredentials.getPrivateKey(), credentialManager.getPublicKey());
        token.setStamp(RandomForTests.randomString());
        token.setSubject(MASTER_EMAIL);
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(RandomForTests.randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(RandomForTests.randomString());
        token.setDelegate(RandomForTests.randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(Claims.CLIENT_ID.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.CLIENT_PROFILE.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.ENTITY_ID.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.ACTION.getValue(), RandomForTests.randomString());
        token.cypher();
    }

    private void given_fail_token() {
        token = tokenFactory.newAccessToken(applicationCredentials.getPrivateKey(), credentialManager.getPublicKey());
        token.setStamp(RandomForTests.randomString());
        token.setSubject(MASTER_EMAIL);
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(RandomForTests.randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(RandomForTests.randomString());
        token.setDelegate(RandomForTests.randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.FAIL);
        token.cypher();
    }

    private void given_success_token_with_application_claim_already_assigned() {
        token = tokenFactory.newAccessToken(applicationCredentials.getPrivateKey(), credentialManager.getPublicKey());
        token.setStamp(RandomForTests.randomString());
        token.setSubject(MASTER_EMAIL);
        token.setScopes(new HashSet<>(asList(TokenScope.PERSON)));
        token.setBP("SSO");
        token.setState(RandomForTests.randomString());
        token.setAudience(localEntityId);
        token.setIssuer(entityId);
        token.setDelegator(RandomForTests.randomString());
        token.setDelegate(RandomForTests.randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setClaim(Claims.CLIENT_ID.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.CLIENT_PROFILE.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.ENTITY_ID.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.ACTION.getValue(), RandomForTests.randomString());
        token.setClaim(Claims.APPLICATION.getValue(), "PolicyDev");
        token.cypher();
    }

    private void when_post_the_token() {
        try {
            serialized = token.serialize();
            resultActions = mockMvc.perform(post("/sp/jose/sso")
                    .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                    .header("Authorization", "Bearer " + serialized));
        } catch (Exception e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    private void then_authenticated() {
        try {
            resultActions
                    .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", nullValue()))
                    .andExpect(redirectedUrl("/home.html"));
        } catch (Exception e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    private void then_navigate_to_the_app() {
        try {
            resultActions
                    .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", nullValue()))
                    .andExpect(redirectedUrl("/navigate/forward/to/PolicyDev"));
        } catch (Exception e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    private void then_error() {
        try {
            resultActions
                    .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", notNullValue()))
                    .andExpect(forwardedUrl("/401.html"));
        } catch (Exception e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }
}
