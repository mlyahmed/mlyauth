package com.primasolutions.idp.tools;

import com.primasolutions.idp.AbstractIntegrationTest;
import com.primasolutions.idp.credentials.CredentialManager;
import com.primasolutions.idp.token.jose.JOSERefreshToken;
import com.primasolutions.idp.token.jose.JOSETokenFactoryImpl;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;

import static com.primasolutions.idp.tools.KeysForTests.decodeRSAPrivateKey;
import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class AccessTokenForTests {

    private static final String CL_LOGIN = "cl.prima.client.dev";
    private static final String CL_PASSWORD = "n90014d8o621AXc";
    private static final String CL_REFRESH_TOKEN_ID = "c810d2fe-5f91-4a41-accc-da88c5028fd3";
    private static final String CL_ENTITY_ID = "prima.client.dev";
    private static final String JOSE_ACCESS_URI = "/token/jose/access";


    @Value("${idp.jose.entityId}")
    protected String idpJoseEntityId;

    @Value("${test.cl-prima-client-dev.private-key}")
    protected File privateKeyFile;

    @Autowired
    protected CredentialManager credManager;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JOSETokenFactoryImpl tokenFactory;


    public final String generateMasterToken()  {

        try {
            final PrivateKey privateKey = decodeRSAPrivateKey(new String(Files.readAllBytes(privateKeyFile.toPath())));
            JOSERefreshToken refreshToken = tokenFactory.newRefreshToken(privateKey, credManager.getPublicKey());
            refreshToken.setStamp(CL_REFRESH_TOKEN_ID);
            refreshToken.setSubject(AbstractIntegrationTest.MASTER_EMAIL);
            refreshToken.setIssuer(CL_ENTITY_ID);
            refreshToken.setAudience(idpJoseEntityId);
            refreshToken.cypher();

            final ResultActions result = mockMvc.perform(post(JOSE_ACCESS_URI)
                    .content(refreshToken.serialize())
                    .with(httpBasic(CL_LOGIN, CL_PASSWORD))
                    .contentType("text/plain;charset=UTF-8"));
            result.andExpect(status().isCreated());

            JSONParser parser = new JSONParser(MODE_JSON_SIMPLE);
            JSONObject jsonObject = (JSONObject) parser.parse(result.andReturn().getResponse().getContentAsString());
            return jsonObject.getAsString("access_token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
