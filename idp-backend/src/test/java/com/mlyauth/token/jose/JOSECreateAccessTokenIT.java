package com.mlyauth.token.jose;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.constants.*;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.*;
import com.mlyauth.token.ITokenFactory;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.util.Base64URL;
import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static com.mlyauth.constants.AspectAttribute.*;
import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JOSECreateAccessTokenIT extends AbstractIntegrationTest {

    public static final String CLIENT_SPACE_APP_LOGIN = "cl.clientSpace";
    public static final String CLIENT_SPACE_APP_PASSWORD = "BrsAssu84;";
    public static final String CLIENT_APP_ENTITY_ID = "clientSpace";
    public static final String POLICY_APP_LOGIN = "rs.policy";
    public static final String POLICY_APP_PASSWORD = "BrsAssu84;";
    public static final String POLICY_APP_ENTITY_ID = "policy";

    @Value("${idp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private ITokenFactory tokenFactory;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private CredentialManager credManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;
    private Application clientSpace;
    private Pair<PrivateKey, X509Certificate> clientCred;
    private JOSERefreshToken clientRefreshToken;
    private Application policy;
    private Pair<PrivateKey, X509Certificate> policyCred;
    private JOSEAccessToken accessToken;
    private String serializedAccessToken;
    private JOSERefreshToken clientQueryToken;

    @Test
    public void when_a_registered_client_asks_an_access_then_return_it() throws Exception {
        given_the_client_space_application();
        given_the_client_space_with_client_aspect();
        given_the_client_space_refresh_token_is_ready();
        given_the_policy_application();
        given_the_policy_with_resource_server_aspect();
        given_a_token_to_ask_access_to_the_policy_app();
        when_then_client_space_ask_an_access_token();

        resultActions.andExpect(status().isCreated()).andExpect(content().contentType("text/plain;charset=UTF-8"));
        serializedAccessToken = resultActions.andReturn().getResponse().getContentAsString();
        accessToken = tokenFactory.createJOSEAccessToken(serializedAccessToken, policyCred.getKey(), credManager.getLocalPublicKey());
        accessToken.decipher();
        assertThat(accessToken.getIssuer(), Matchers.equalTo(localEntityId));
        assertThat(accessToken.getAudience(), Matchers.equalTo(POLICY_APP_ENTITY_ID));
    }

    private void given_the_client_space_refresh_token_is_ready() {
        clientRefreshToken = tokenFactory.createJOSERefreshToken(credManager.getLocalPrivateKey(), clientCred.getValue().getPublicKey());
        clientRefreshToken.setStamp(UUID.randomUUID().toString());
        clientRefreshToken.setAudience(CLIENT_APP_ENTITY_ID);
        clientRefreshToken.setVerdict(TokenVerdict.SUCCESS);
        clientRefreshToken.cypher();
        final String serialized = clientRefreshToken.serialize();
        Token token = tokenMapper.toToken(clientRefreshToken);
        token.setPurpose(TokenPurpose.DELEGATION);
        token.setApplication(clientSpace);
        token.setChecksum(DigestUtils.sha256Hex(serialized));
        token.setStatus(TokenStatus.READY);
        tokenDAO.save(token);
    }

    private void given_the_client_space_application() {
        AuthenticationInfo clientSpaceAuthInfo = AuthenticationInfo.newInstance()
                .setLogin(CLIENT_SPACE_APP_LOGIN)
                .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 30 * 60)))
                .setPassword(passwordEncoder.encode(CLIENT_SPACE_APP_PASSWORD))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        clientSpace = Application.newInstance()
                .setAppname("clientSpace")
                .setAspects(new HashSet<>(Arrays.asList(CL_JOSE)))
                .setTitle("The Client Space")
                .setAuthenticationInfo(clientSpaceAuthInfo);

        clientSpaceAuthInfo.setApplication(clientSpace);
        applicationDAO.save(clientSpace);
        authenticationInfoDAO.save(clientSpaceAuthInfo);
    }

    private void given_the_client_space_with_client_aspect() throws CertificateEncodingException {
        clientCred = KeysForTests.generateRSACredential();

        final ApplicationAspectAttribute clientSpaceEntityIdAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_ENTITY_ID.getValue()))
                .setValue(CLIENT_APP_ENTITY_ID);

        final ApplicationAspectAttribute clientSpaceContextAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_CONTEXT.getValue()))
                .setValue("http://client.boursorama.assurances.com");


        final ApplicationAspectAttribute clientSpaceCertificateAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(AspectAttribute.CL_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(clientCred.getValue().getEncoded()).toString());

        appAspectAttrDAO.save(asList(clientSpaceEntityIdAttribute, clientSpaceContextAttribute, clientSpaceCertificateAttribute));
    }

    private void given_the_policy_application() {
        AuthenticationInfo policyAuthInfo = AuthenticationInfo.newInstance()
                .setLogin(POLICY_APP_LOGIN)
                .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 30 * 60)))
                .setPassword(passwordEncoder.encode(POLICY_APP_PASSWORD))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        policy = Application.newInstance()
                .setAppname("policy")
                .setAspects(new HashSet<>(Arrays.asList(RS_JOSE)))
                .setTitle("The Prima Policy")
                .setAuthenticationInfo(policyAuthInfo);

        policyAuthInfo.setApplication(policy);
        applicationDAO.save(policy);
        authenticationInfoDAO.save(policyAuthInfo);
    }

    private void given_the_policy_with_resource_server_aspect() throws CertificateEncodingException {
        policyCred = KeysForTests.generateRSACredential();

        final ApplicationAspectAttribute policyEntityIdAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_ENTITY_ID.getValue()))
                .setValue(POLICY_APP_ENTITY_ID);

        final ApplicationAspectAttribute policyContextAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_CONTEXT.getValue()))
                .setValue("http://policy.sgi.prima-solutions.com");


        final ApplicationAspectAttribute policyCertificateAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(AspectAttribute.RS_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(policyCred.getValue().getEncoded()).toString());


        appAspectAttrDAO.save(asList(policyEntityIdAttribute, policyContextAttribute, policyCertificateAttribute));
    }

    private void given_a_token_to_ask_access_to_the_policy_app() {
        clientQueryToken = tokenFactory.createJOSERefreshToken(clientCred.getKey(), credManager.getLocalPublicKey());
        clientQueryToken.setIssuer(CLIENT_APP_ENTITY_ID);
        clientQueryToken.setAudience(POLICY_APP_ENTITY_ID);
        clientQueryToken.setStamp(clientRefreshToken.getStamp());
        clientQueryToken.setDelegator(randomString());
        clientQueryToken.cypher();

    }

    private void when_then_client_space_ask_an_access_token() throws Exception {
        resultActions = mockMvc.perform(post("/token/jose/access")
                .content(clientQueryToken.serialize())
                .with(httpBasic(CLIENT_SPACE_APP_LOGIN, CLIENT_SPACE_APP_PASSWORD))
                .contentType("text/plain;charset=UTF-8"));
    }




    //TODO when the CLIENT passes bad refresh ID then error
    //TODO when the CLIENT passes bad RS ID then error
}
