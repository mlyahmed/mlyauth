package com.mlyauth.tools;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.token.jose.JOSERefreshToken;
import com.mlyauth.token.jose.JOSETokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;

import static com.mlyauth.tools.KeysForTests.decodeRSAPrivateKey;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class AccessTokenForTests {

    private final static String CL_LOGIN = "cl.prima.client.dev";
    private final static String CL_PASSWORD = "n90014d8o621AXc";
    private final static String CL_REFRESH_TOKEN_ID = "c810d2fe-5f91-4a41-accc-da88c5028fd3";
    private final static String CL_ENTITY_ID = "prima.client.dev";


    @Value("${idp.jose.entityId}")
    protected String idpJoseEntityId;

    @Value("${test.cl-prima-client-dev.private-key}")
    protected File privateKeyFile;

    @Autowired
    protected CredentialManager credManager;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JOSETokenFactory tokenFactory;


    public final String generateToken()  {

        try{
            final PrivateKey privateKey = decodeRSAPrivateKey(new String(Files.readAllBytes(privateKeyFile.toPath())));
            JOSERefreshToken refreshToken = tokenFactory.createRefreshToken(privateKey, credManager.getPublicKey());
            refreshToken.setStamp(CL_REFRESH_TOKEN_ID);
            refreshToken.setSubject(AbstractIntegrationTest.MASTER_EXTERNAL_ID);
            refreshToken.setIssuer(CL_ENTITY_ID);
            refreshToken.setAudience(idpJoseEntityId);
            refreshToken.cypher();

            final ResultActions result = mockMvc.perform(post("/token/jose/access")
                    .content(refreshToken.serialize())
                    .with(httpBasic(CL_LOGIN, CL_PASSWORD))
                    .contentType("text/plain;charset=UTF-8"));
            return result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

}
