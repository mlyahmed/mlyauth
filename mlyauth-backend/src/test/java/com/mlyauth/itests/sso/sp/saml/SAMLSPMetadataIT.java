package com.mlyauth.itests.sso.sp.saml;

import com.mlyauth.itests.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.signature.SignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Document;

import javax.servlet.Filter;
import java.io.ByteArrayInputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SAMLSPMetadataIT extends AbstractIntegrationTest {

    public static final String SP_ENTITY_ID = "primainsure4sgi";
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Autowired
    private Filter samlFilter;
    @Autowired
    private Filter metadataGeneratorFilter;
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    @Autowired
    private KeyManager keyManeger;

    @Autowired
    private ParserPool parserPool;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).addFilters(metadataGeneratorFilter, samlFilter).build();

    }

    @Test
    public void when_request_SP_metadata_then_returned() throws Exception {
        String content = when_get_sp_metadata();
        assertThat(content, notNullValue());
    }

    @Test
    public void the_metadata_must_be_signed() throws Exception {
        String content = when_get_sp_metadata();
        EntityDescriptorImpl metadata = toMetadata(content);
        assertThat(metadata.isSigned(), equalTo(true));
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(metadata.getSignature());
        SignatureValidator sigValidator = new SignatureValidator(keyManeger.getDefaultCredential());
        sigValidator.validate(metadata.getSignature());
    }

    @Test
    public void the_SP_entity_id_must_be_defined() throws Exception {
        String content = when_get_sp_metadata();
        EntityDescriptorImpl metadata = toMetadata(content);
        assertThat(metadata, notNullValue());
        assertThat(metadata.getEntityID(), equalTo(SP_ENTITY_ID));
        assertThat(metadata.getID(), equalTo(SP_ENTITY_ID));
    }

    @Test
    public void the_metadata_must_expose_a_SP_assertion_consumer_service() throws Exception {
        String content = when_get_sp_metadata();
        EntityDescriptorImpl metadata = toMetadata(content);
        SPSSODescriptor spssoDescriptor = metadata.getSPSSODescriptor(SAML20P_NS);
        assertThat(spssoDescriptor, notNullValue());
        assertThat(spssoDescriptor.getDefaultAssertionConsumerService(), notNullValue());
        assertThat(spssoDescriptor.getDefaultAssertionConsumerService().getBinding(), equalTo(SAML2_POST_BINDING_URI));
        assertThat(spssoDescriptor.getDefaultAssertionConsumerService().getLocation(), notNullValue());
    }

    private EntityDescriptorImpl toMetadata(String content) throws Exception {
        final Document doc = parserPool.parse(new ByteArrayInputStream(content.getBytes()));
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        XMLObject xmlObject = unmarshaller.unmarshall(doc.getDocumentElement());
        assertThat(xmlObject, instanceOf(EntityDescriptorImpl.class));
        return (EntityDescriptorImpl) xmlObject;
    }

    private String when_get_sp_metadata() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/saml/sp/metadata"));
        MvcResult result = resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/samlmetadata+xml"))
                .andReturn();

        return result.getResponse().getContentAsString();
    }

}
