package com.mlyauth;

import com.mlyauth.credentials.CredentialManager;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@RunWith(DataProviderRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    protected final static String CL_LOGIN = "cl.prima.client.dev";
    protected final static String CL_PASSWORD = "n90014d8o621AXc";
    protected final static String CL_REFRESH_TOKEN_ID = "c810d2fe-5f91-4a41-accc-da88c5028fd3";
    protected final static String CL_ENTITY_ID = "prima.client.dev";
    protected final static String MASTER_EMAIL = "ahmed.elidrissi.attach@gmail.com";
    protected final static String MASTER_PASSWORD = "root";

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();


    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Value("${idp.jose.entityId}")
    protected String idpJoseEntityId;

    @Value("${test.cl-prima-client-dev.private-key}")
    protected File privateKeyFile;

    @Autowired
    protected CredentialManager credManager;

    protected String accessToken;

    protected MockMvc mockMvc;
}
