package com.mlyauth.token.jose;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.constants.ApplicationTypeCode;
import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.constants.AuthenticationInfoStatus;
import com.mlyauth.constants.TokenPurpose;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.ApplicationTypeDAO;
import com.mlyauth.dao.AuthenticationInfoByLoginDAO;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.AppAspAttr;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.AuthenticationInfoByLogin;
import com.mlyauth.domain.Token;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.util.Base64URL;
import javafx.util.Pair;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static com.mlyauth.constants.AspectAttribute.CL_JOSE_CONTEXT;
import static com.mlyauth.constants.AspectAttribute.CL_JOSE_ENTITY_ID;
import static com.mlyauth.constants.AspectAttribute.RS_JOSE_CONTEXT;
import static com.mlyauth.constants.AspectAttribute.RS_JOSE_ENTITY_ID;
import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JOSEAccessTokenIT extends AbstractIntegrationTest {

    private static final String CLIENT_SPACE_APP_LOGIN = "cl.clientSpace";
    private static final String CLIENT_SPACE_APP_PASSWORD = "BrsAssu84;";
    private static final String CLIENT_APP_ENTITY_ID = "clientSpace";
    private static final String POLICY_APP_LOGIN = "rs.policy";
    private static final String POLICY_APP_PASSWORD = "BrsAssu84;";
    private static final String POLICY_APP_ENTITY_ID = "policy";
    private static final int ONE_MINUTE = 1000 * 30 * 60;

    @Value("${idp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private JOSETokenFactory tokenFactory;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private CredentialManager credManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationTypeDAO applicationTypeDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    private AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;
    private Application clientSpace;
    private Pair<PrivateKey, X509Certificate> clientCred;
    private JOSERefreshToken clientRefreshToken;
    private JOSERefreshToken clientQueryToken;
    private Application policy;
    private Pair<PrivateKey, X509Certificate> policyCred;
    private JOSEAccessToken accessToken;
    private String serialized;

    @Test
    public void when_a_registered_client_asks_an_access_then_return_it() throws Exception {
        given_the_client_space_application();
        given_the_client_space_with_client_aspect();
        given_the_client_space_refresh_token_is_ready();
        given_the_policy_application();
        given_the_policy_with_resource_server_aspect();
        given_a_token_to_ask_access_to_the_policy_app();
        when_then_client_space_ask_an_access_token();
        then_an_access_token_is_returned();
        and_the_access_token_returned_is_built_to_policy();
        and_the_access_token_returned_is_traced();
    }

    @Test
    public void when_a_registered_client_asks_and_access_to_the_IDP_then_return_it() throws Exception {
        given_the_client_space_application();
        given_the_client_space_with_client_aspect();
        given_the_client_space_refresh_token_is_ready();
        given_a_token_to_ask_access_to_the_IDP_app();
        when_then_client_space_ask_an_access_token();
        then_an_access_token_is_returned();
        and_the_access_token_returned_is_built_to_the_IDP();
        and_the_access_token_returned_is_traced();
    }

    @Test
    public void when_the_resource_server_check_a_valid_access_token_then_return_OK() throws Exception {
        given_the_client_space_application();
        given_the_client_space_with_client_aspect();
        given_the_client_space_refresh_token_is_ready();
        given_the_policy_application();
        given_the_policy_with_resource_server_aspect();
        given_a_token_to_ask_access_to_the_policy_app();
        when_then_client_space_ask_an_access_token();
        then_an_access_token_is_returned();
        when_policy_checks_the_access();
        then_the_IDP_returns_OK();
    }

    private void then_the_IDP_returns_OK() throws Exception {
        resultActions.andExpect(status().isOk());
    }

    private void when_policy_checks_the_access() throws Exception {
        resultActions = mockMvc.perform(post("/token/jose/access/_check")
                .content(serialized)
                .with(httpBasic(POLICY_APP_LOGIN, POLICY_APP_PASSWORD))
                .contentType("text/plain;charset=UTF-8"));
    }

    private void given_the_client_space_application() {
        AuthenticationInfo clientSpaceAuthInfo = AuthenticationInfo.newInstance()
                .setLogin(CLIENT_SPACE_APP_LOGIN)
                .setExpireAt(new Date(System.currentTimeMillis() + ONE_MINUTE))
                .setPassword(passwordEncoder.encode(CLIENT_SPACE_APP_PASSWORD))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        clientSpace = Application.newInstance()
                .setType(applicationTypeDAO.findOne(ApplicationTypeCode.CLIENT_SPACE))
                .setAppname("clientSpace")
                .setAspects(new HashSet<>(Arrays.asList(CL_JOSE)))
                .setTitle("The Client Space")
                .setAuthenticationInfo(clientSpaceAuthInfo);

        clientSpaceAuthInfo.setApplication(clientSpace);
        applicationDAO.save(clientSpace);
        final AuthenticationInfo authInfo = authenticationInfoDAO.saveAndFlush(clientSpaceAuthInfo);
        authInfoByLoginDAO.saveAndFlush(AuthenticationInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));

    }

    private void given_the_client_space_with_client_aspect() throws CertificateEncodingException {
        clientCred = KeysForTests.generateRSACredential();

        final AppAspAttr entityId = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_ENTITY_ID.getValue()))
                .setValue(CLIENT_APP_ENTITY_ID);

        final AppAspAttr context = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_CONTEXT.getValue()))
                .setValue("http://client.boursorama.assurances.com");


        final AppAspAttr certificate = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(AspectAttribute.CL_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(clientCred.getValue().getEncoded()).toString());

        appAspectAttrDAO.save(asList(entityId, context, certificate));
    }

    private void given_the_client_space_refresh_token_is_ready() {
        final X509Certificate certificate = clientCred.getValue();
        clientRefreshToken = tokenFactory.newRefreshToken(credManager.getPrivateKey(), certificate.getPublicKey());
        clientRefreshToken.setStamp(UUID.randomUUID().toString());
        clientRefreshToken.setAudience(CLIENT_APP_ENTITY_ID);
        clientRefreshToken.setVerdict(TokenVerdict.SUCCESS);
        clientRefreshToken.cypher();
        Token token = tokenMapper.toToken(clientRefreshToken);
        token.setPurpose(TokenPurpose.DELEGATION);
        token.setApplication(clientSpace);
        token.setChecksum(DigestUtils.sha256Hex(clientRefreshToken.serialize()));
        token.setStatus(TokenStatus.READY);
        tokenDAO.save(token);
    }

    private void given_the_policy_application() {
        AuthenticationInfo policyAuthInfo = AuthenticationInfo.newInstance()
                .setLogin(POLICY_APP_LOGIN)
                .setExpireAt(new Date(System.currentTimeMillis() + ONE_MINUTE))
                .setPassword(passwordEncoder.encode(POLICY_APP_PASSWORD))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        policy = Application.newInstance()
                .setType(applicationTypeDAO.findOne(ApplicationTypeCode.POLICY))
                .setAppname("policy")
                .setAspects(new HashSet<>(Arrays.asList(RS_JOSE)))
                .setTitle("The Prima Policy")
                .setAuthenticationInfo(policyAuthInfo);

        policyAuthInfo.setApplication(policy);
        applicationDAO.save(policy);
        final AuthenticationInfo authInfo = authenticationInfoDAO.saveAndFlush(policyAuthInfo);
        authInfoByLoginDAO.saveAndFlush(AuthenticationInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));

    }

    private void given_the_policy_with_resource_server_aspect() throws CertificateEncodingException {
        policyCred = KeysForTests.generateRSACredential();

        final AppAspAttr policyEntityIdAttribute = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_ENTITY_ID.getValue()))
                .setValue(POLICY_APP_ENTITY_ID);

        final AppAspAttr policyContextAttribute = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_CONTEXT.getValue()))
                .setValue("http://policy.sgi.prima-solutions.com");


        final AppAspAttr policyCertificateAttribute = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(AspectAttribute.RS_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(policyCred.getValue().getEncoded()).toString());


        appAspectAttrDAO.save(asList(policyEntityIdAttribute, policyContextAttribute, policyCertificateAttribute));
    }

    private void given_a_token_to_ask_access_to_the_policy_app() {
        clientQueryToken = tokenFactory.newRefreshToken(clientCred.getKey(), credManager.getPublicKey());
        clientQueryToken.setIssuer(CLIENT_APP_ENTITY_ID);
        clientQueryToken.setAudience(POLICY_APP_ENTITY_ID);
        clientQueryToken.setStamp(clientRefreshToken.getStamp());
        clientQueryToken.setDelegator(randomString());
        clientQueryToken.cypher();

    }

    private void given_a_token_to_ask_access_to_the_IDP_app() {
        clientQueryToken = tokenFactory.newRefreshToken(clientCred.getKey(), credManager.getPublicKey());
        clientQueryToken.setIssuer(CLIENT_APP_ENTITY_ID);
        clientQueryToken.setAudience(localEntityId);
        clientQueryToken.setStamp(clientRefreshToken.getStamp());
        clientQueryToken.cypher();
    }

    private void when_then_client_space_ask_an_access_token() throws Exception {
        resultActions = mockMvc.perform(post("/token/jose/access")
                .content(clientQueryToken.serialize())
                .with(httpBasic(CLIENT_SPACE_APP_LOGIN, CLIENT_SPACE_APP_PASSWORD))
                .contentType("text/plain;charset=UTF-8"));
    }

    private void then_an_access_token_is_returned() throws Exception {
        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        JSONParser parser = new JSONParser(MODE_JSON_SIMPLE);
        JSONObject jsonObject = (JSONObject) parser.parse(resultActions.andReturn().getResponse().getContentAsString());
        serialized = jsonObject.getAsString("serialized");
        assertThat(serialized, notNullValue());
    }

    private void and_the_access_token_returned_is_built_to_policy() {
        accessToken = tokenFactory.newAccessToken(serialized, policyCred.getKey(), credManager.getPublicKey());
        accessToken.decipher();
        assertThat(accessToken.getIssuer(), Matchers.equalTo(localEntityId));
        assertThat(accessToken.getAudience(), Matchers.equalTo(POLICY_APP_ENTITY_ID));
    }

    private void and_the_access_token_returned_is_built_to_the_IDP() {
        accessToken = tokenFactory.newAccessToken(serialized, credManager.getPrivateKey(), credManager.getPublicKey());
        accessToken.decipher();
        assertThat(accessToken.getIssuer(), Matchers.equalTo(localEntityId));
        assertThat(accessToken.getAudience(), Matchers.equalTo(localEntityId));
    }

    private void and_the_access_token_returned_is_traced() {
        final Token tracedToken = tokenDAO.findByChecksum(DigestUtils.sha256Hex(serialized));
        assertThat(tracedToken, notNullValue());
    }

    //TODO when the CLIENT passes bad refresh ID then error
    //TODO when the CLIENT passes bad RS ID then error
}
