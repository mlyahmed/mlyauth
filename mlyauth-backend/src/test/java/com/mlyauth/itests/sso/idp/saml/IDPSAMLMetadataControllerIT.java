package com.mlyauth.itests.sso.idp.saml;

import com.mlyauth.itests.AbstractIntegrationTest;
import com.mlyauth.security.saml.SAMLHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class IDPSAMLMetadataControllerIT extends AbstractIntegrationTest {

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private ResultActions resultActions;
    private MvcResult result;


    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();

    }

    @Test
    public void when_request_IDP_metadata_then_returned() throws Exception {
        when_get_IDP_metadata();
        final String metadata = result.getResponse().getContentAsString();
        assertThat(metadata, notNullValue());
    }

    @Test
    public void the_IDP_entity_id_must_be_defined() throws Exception {
        when_get_IDP_metadata();
        EntityDescriptorImpl metadata = samlHelper.toMetadata(result.getResponse().getContentAsString());
        Assert.assertThat(metadata.getEntityID(), equalTo("app4primainsure"));
        Assert.assertThat(metadata.getID(), equalTo("app4primainsure"));
    }

    private void when_get_IDP_metadata() throws Exception {
        resultActions = mockMvc.perform(get("/idp/saml/metadata"));
        result = resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=UTF-8"))
                .andReturn();
    }

}
