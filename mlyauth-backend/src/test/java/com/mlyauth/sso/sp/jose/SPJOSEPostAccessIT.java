package com.mlyauth.sso.sp.jose;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.constants.AspectType;
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
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;

import static com.mlyauth.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
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

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).addFilters(joseFilter).build();
    }

    @Test
    public void when_post_a_true_access_from_a_defined_idp_then_OK() throws Exception {
        final Pair<PrivateKey, X509Certificate> applicationCredentials = KeysForTests.generateRSACredential();
        Application application = Application.newInstance()
                .setAppname("LinkAssuDev")
                .setTitle("Application")
                .setAspects(new HashSet<>(Arrays.asList(AspectType.IDP_JOSE)));
        application = applicationDAO.save(application);

        final ApplicationAspectAttribute linkAssuID = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(application.getId())
                        .setAspectCode(AspectType.IDP_JOSE.name())
                        .setAttributeCode(AspectAttribute.IDP_JOSE_ENTITY_ID.getValue()))
                .setValue("LinkAssuDev");

        final ApplicationAspectAttribute linkAssuSSOURL = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(application.getId())
                        .setAspectCode(AspectType.IDP_JOSE.name())
                        .setAttributeCode(AspectAttribute.IDP_JOSE_SSO_URL.getValue()))
                .setValue("http://localhost/idp/jose/sso");

        final ApplicationAspectAttribute linkAssuCertificate = ApplicationAspectAttribute.newInstance()
                .setId(ApplicationAspectAttributeId.newInstance()
                        .setApplicationId(application.getId())
                        .setAspectCode(AspectType.IDP_JOSE.name())
                        .setAttributeCode(AspectAttribute.IDP_JOSE_ENCRYPTION_CERTIFICATE.getValue()))
                .setValue(Base64URL.encode(applicationCredentials.getValue().getEncoded()).toString());


        appAspectAttrDAO.save(Arrays.asList(linkAssuID, linkAssuSSOURL, linkAssuCertificate));


        final JOSEAccessToken token = tokenFactory.createJOSEAccessToken(applicationCredentials.getKey()
                , (RSAPublicKey) credentialManager.getLocalPublicKey());

        token.setId(randomString());
        token.setSubject("1");
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.PERSON)));
        token.setBP(randomString());
        token.setState(randomString());
        token.setAudience(localEntityId);
        token.setIssuer("LinkAssuDev");
        token.setDelegator(randomString());
        token.setDelegate(randomString());
        token.setTargetURL("http://localhost/sp/jose/sso");
        token.setVerdict(TokenVerdict.SUCCESS);
        token.cypher();

        resultActions = mockMvc.perform(post("/sp/jose/sso")
                .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Bearer " + token.serialize()));

        resultActions
                .andExpect(request().attribute("SPRING_SECURITY_LAST_EXCEPTION", nullValue()))
                .andExpect(redirectedUrl("/home.html"));

    }
}
