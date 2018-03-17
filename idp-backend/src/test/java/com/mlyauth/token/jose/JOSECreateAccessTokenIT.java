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
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static com.mlyauth.constants.AspectAttribute.*;
import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static java.util.Arrays.asList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JOSECreateAccessTokenIT extends AbstractIntegrationTest {

    @Value("${idp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private ITokenFactory tokenFactory;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private CredentialManager credentialManager;

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

    @Test
    public void when_a_registered_client_asks_an_access_then_return_it() throws Exception {

        // Client space
        AuthenticationInfo clientSpaceAuthInfo = AuthenticationInfo.newInstance()
                .setLogin("cl.clientSpace")
                .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 30 * 60)))
                .setPassword(passwordEncoder.encode("BrsAssu84;"))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        Application clientSpace = Application.newInstance()
                .setAppname("clientSpace")
                .setAspects(new HashSet<>(Arrays.asList(CL_JOSE)))
                .setTitle("The Client Space")
                .setAuthenticationInfo(clientSpaceAuthInfo);

        clientSpaceAuthInfo.setApplication(clientSpace);
        applicationDAO.save(clientSpace);
        authenticationInfoDAO.save(clientSpaceAuthInfo);

        final Pair<PrivateKey, X509Certificate> clientSpaceCredentials = KeysForTests.generateRSACredential();

        final ApplicationAspectAttribute clientSpaceEntityIdAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(CL_JOSE.name())
                        .setAttributeCode(CL_JOSE_ENTITY_ID.getValue()))
                .setValue("clientSpace");

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
                .setValue(Base64URL.encode(clientSpaceCredentials.getValue().getEncoded()).toString());


        appAspectAttrDAO.save(asList(clientSpaceEntityIdAttribute, clientSpaceContextAttribute, clientSpaceCertificateAttribute));


        // The Prima Policy
        AuthenticationInfo policyAuthInfo = AuthenticationInfo.newInstance()
                .setLogin("rs.policy")
                .setExpireAt(new Date(System.currentTimeMillis() + (1000 * 30 * 60)))
                .setPassword(passwordEncoder.encode("BrsAssu84;"))
                .setStatus(AuthenticationInfoStatus.ACTIVE)
                .setEffectiveAt(new Date());
        Application policy = Application.newInstance()
                .setAppname("policy")
                .setAspects(new HashSet<>(Arrays.asList(RS_JOSE)))
                .setTitle("The Prima Policy")
                .setAuthenticationInfo(policyAuthInfo);

        policyAuthInfo.setApplication(policy);
        applicationDAO.save(policy);
        authenticationInfoDAO.save(policyAuthInfo);

        final Pair<PrivateKey, X509Certificate> policyCredentials = KeysForTests.generateRSACredential();

        final ApplicationAspectAttribute policyEntityIdAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(clientSpace.getId())
                        .setAspectCode(RS_JOSE.name())
                        .setAttributeCode(RS_JOSE_ENTITY_ID.getValue()))
                .setValue("policy");

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
                .setValue(Base64URL.encode(policyCredentials.getValue().getEncoded()).toString());


        appAspectAttrDAO.save(asList(policyEntityIdAttribute, policyContextAttribute, policyCertificateAttribute));


        final JOSERefreshToken refreshToken = tokenFactory.createJOSERefreshToken(credentialManager.getLocalPrivateKey(), clientSpaceCredentials.getValue().getPublicKey());
        refreshToken.setStamp(UUID.randomUUID().toString());
        refreshToken.setAudience("clientSpace");
        refreshToken.setVerdict(TokenVerdict.SUCCESS);
        refreshToken.cypher();
        final String serialized = refreshToken.serialize();
        Token token = tokenMapper.toToken(refreshToken);
        token.setPurpose(TokenPurpose.DELEGATION);
        token.setApplication(clientSpace);
        token.setChecksum(DigestUtils.sha256Hex(serialized));
        token.setStatus(TokenStatus.READY);
        tokenDAO.save(token);

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer("clientSpace")
                .audience("policy")
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .jwtID(refreshToken.getStamp());

        SignedJWT tokenSigned = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).customParam("iss", "clientSpace").build(), claims.build());
        tokenSigned.sign(new RSASSASigner(clientSpaceCredentials.getKey()));
        JWEObject tokenEncrypted = new JWEObject(new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build(), new Payload(tokenSigned));

        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) credentialManager.getLocalPublicKey()));


        resultActions = mockMvc.perform(post("/token/jose/access")
                .content(tokenEncrypted.serialize())
                .with(httpBasic("cl.clientSpace", "BrsAssu84;"))
                .contentType("text/plain;charset=UTF-8"));


        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));

        String content = resultActions.andReturn().getResponse().getContentAsString();

        final JOSEAccessToken accessToken = tokenFactory.createJOSEAccessToken(content, policyCredentials.getKey(), credentialManager.getLocalPublicKey());
        accessToken.decipher();
        Assert.assertThat(accessToken.getIssuer(), Matchers.equalTo(localEntityId));


    }


    //TODO when the CLIENT passes bad refresh ID then error

    //TODO when the CLIENT passes bad RS ID then error

}
