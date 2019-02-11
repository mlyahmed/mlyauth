package com.hohou.federation.idp.token.jose;

import com.hohou.federation.idp.AbstractIntegrationTest;
import com.hohou.federation.idp.application.AppAspAttr;
import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.application.ApplicationAspectAttributeDAO;
import com.hohou.federation.idp.application.ApplicationAspectAttributeId;
import com.hohou.federation.idp.application.ApplicationDAO;
import com.hohou.federation.idp.application.ApplicationTypeDAO;
import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthInfoByLogin;
import com.hohou.federation.idp.authentication.AuthInfoByLoginDAO;
import com.hohou.federation.idp.authentication.AuthInfoDAO;
import com.hohou.federation.idp.constants.ApplicationTypeCode;
import com.hohou.federation.idp.constants.AspectAttribute;
import com.hohou.federation.idp.constants.AspectType;
import com.hohou.federation.idp.constants.AuthInfoStatus;
import com.hohou.federation.idp.constants.TokenPurpose;
import com.hohou.federation.idp.constants.TokenStatus;
import com.hohou.federation.idp.constants.TokenVerdict;
import com.hohou.federation.idp.credentials.CredentialManager;
import com.hohou.federation.idp.credentials.CredentialsPair;
import com.hohou.federation.idp.token.Token;
import com.hohou.federation.idp.token.TokenDAO;
import com.hohou.federation.idp.token.TokenMapper;
import com.hohou.federation.idp.tools.KeysForTests;
import com.hohou.federation.idp.tools.RandomForTests;
import com.nimbusds.jose.util.Base64URL;
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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static com.hohou.federation.idp.constants.AspectAttribute.CL_JOSE_CONTEXT;
import static com.hohou.federation.idp.constants.AspectAttribute.CL_JOSE_ENTITY_ID;
import static com.hohou.federation.idp.constants.AspectAttribute.RS_JOSE_CONTEXT;
import static com.hohou.federation.idp.constants.AspectAttribute.RS_JOSE_ENTITY_ID;
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
    private JOSETokenFactoryImpl tokenFactory;

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
    private AuthInfoDAO authInfoDAO;

    @Autowired
    private AuthInfoByLoginDAO authInfoByLoginDAO;

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;
    private Application clientSpace;
    private CredentialsPair clientCred;
    private JOSERefreshToken clientRefreshToken;
    private JOSERefreshToken clientQueryToken;
    private Application policy;
    private CredentialsPair policyCred;
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
        AuthInfo clientSpaceAuthInfo = AuthInfo.newInstance()
                .setLogin(CLIENT_SPACE_APP_LOGIN)
                .setExpireAt(new Date(System.currentTimeMillis() + ONE_MINUTE))
                .setPassword(passwordEncoder.encode(CLIENT_SPACE_APP_PASSWORD))
                .setStatus(AuthInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        clientSpace = Application.newInstance()
                .setType(applicationTypeDAO.findById(ApplicationTypeCode.CLIENT_SPACE).get())
                .setAppname("clientSpace")
                .setAspects(new HashSet<>(Arrays.asList(AspectType.CL_JOSE)))
                .setTitle("The Client Space")
                .setAuthenticationInfo(clientSpaceAuthInfo);

        clientSpaceAuthInfo.setApplication(clientSpace);
        applicationDAO.save(clientSpace);
        final AuthInfo authInfo = authInfoDAO.saveAndFlush(clientSpaceAuthInfo);
        authInfoByLoginDAO.saveAndFlush(AuthInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));

    }

    private void given_the_client_space_with_client_aspect() throws CertificateEncodingException {
        clientCred = KeysForTests.generateRSACredential();

        final AppAspAttr entityId = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(AspectType.CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_ENTITY_ID.getValue()))
                .setValue(CLIENT_APP_ENTITY_ID);

        final AppAspAttr context = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(AspectType.CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_CONTEXT.getValue()))
                .setValue("http://client.boursorama.assurances.com");


        final AppAspAttr certificate = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(AspectType.CL_JOSE.name())
                        .setAttributeCode(AspectAttribute.CL_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(clientCred.getCertificate().getEncoded()).toString());

        appAspectAttrDAO.saveAll(asList(entityId, context, certificate));
    }

    private void given_the_client_space_refresh_token_is_ready() {
        final X509Certificate certificate = clientCred.getCertificate();
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
        AuthInfo policyAuthInfo = AuthInfo.newInstance()
                .setLogin(POLICY_APP_LOGIN)
                .setExpireAt(new Date(System.currentTimeMillis() + ONE_MINUTE))
                .setPassword(passwordEncoder.encode(POLICY_APP_PASSWORD))
                .setStatus(AuthInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        policy = Application.newInstance()
                .setType(applicationTypeDAO.findById(ApplicationTypeCode.POLICY).get())
                .setAppname("policy")
                .setAspects(new HashSet<>(Arrays.asList(AspectType.RS_JOSE)))
                .setTitle("The Prima Policy")
                .setAuthenticationInfo(policyAuthInfo);

        policyAuthInfo.setApplication(policy);
        applicationDAO.save(policy);
        final AuthInfo authInfo = authInfoDAO.saveAndFlush(policyAuthInfo);
        authInfoByLoginDAO.saveAndFlush(AuthInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));

    }

    private void given_the_policy_with_resource_server_aspect() throws CertificateEncodingException {
        policyCred = KeysForTests.generateRSACredential();

        final AppAspAttr policyEntityIdAttribute = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(AspectType.RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_ENTITY_ID.getValue()))
                .setValue(POLICY_APP_ENTITY_ID);

        final AppAspAttr policyContextAttribute = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(AspectType.RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_CONTEXT.getValue()))
                .setValue("http://policy.sgi.prima-solutions.com");


        final AppAspAttr policyCertificateAttribute = AppAspAttr.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(AspectType.RS_JOSE.name())
                        .setAttributeCode(AspectAttribute.RS_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(policyCred.getCertificate().getEncoded()).toString());


        appAspectAttrDAO.saveAll(asList(policyEntityIdAttribute, policyContextAttribute, policyCertificateAttribute));
    }

    private void given_a_token_to_ask_access_to_the_policy_app() {
        clientQueryToken = tokenFactory.newRefreshToken(clientCred.getPrivateKey(), credManager.getPublicKey());
        clientQueryToken.setIssuer(CLIENT_APP_ENTITY_ID);
        clientQueryToken.setAudience(POLICY_APP_ENTITY_ID);
        clientQueryToken.setStamp(clientRefreshToken.getStamp());
        clientQueryToken.setDelegator(RandomForTests.randomString());
        clientQueryToken.cypher();

    }

    private void given_a_token_to_ask_access_to_the_IDP_app() {
        clientQueryToken = tokenFactory.newRefreshToken(clientCred.getPrivateKey(), credManager.getPublicKey());
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
        serialized = jsonObject.getAsString("access_token");
        assertThat(serialized, notNullValue());
    }

    private void and_the_access_token_returned_is_built_to_policy() {
        accessToken = tokenFactory.newAccessToken(serialized, policyCred.getPrivateKey(), credManager.getPublicKey());
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
