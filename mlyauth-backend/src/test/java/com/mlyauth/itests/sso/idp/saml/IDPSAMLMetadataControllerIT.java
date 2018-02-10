package com.mlyauth.itests.sso.idp.saml;

import com.mlyauth.itests.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class IDPSAMLMetadataControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ParserPool parserPool;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;


    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();

    }

    @Test
    public void when_request_IDP_metadata_then_returned() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/idp/saml/metadata"));
        MvcResult result = resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=UTF-8"))
                .andReturn();

        final String metadata = result.getResponse().getContentAsString();
        assertThat(metadata, notNullValue());
    }


    @Test
    public void the_IDP_entity_id_must_be_defined() throws Exception {

        ResultActions resultActions = mockMvc.perform(get("/idp/saml/metadata"));
        MvcResult result = resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=UTF-8"))
                .andReturn();

        EntityDescriptorImpl metadata = toMetadata(result.getResponse().getContentAsString());
        Assert.assertThat(metadata.getEntityID(), equalTo("app4primainsure"));
        Assert.assertThat(metadata.getID(), equalTo("app4primainsure"));
    }


    private EntityDescriptorImpl toMetadata(String content) throws Exception {
        final Document doc = parserPool.parse(new ByteArrayInputStream(content.getBytes()));
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        XMLObject xmlObject = unmarshaller.unmarshall(doc.getDocumentElement());
        assertThat(xmlObject, instanceOf(EntityDescriptorImpl.class));
        return (EntityDescriptorImpl) xmlObject;
    }
}
