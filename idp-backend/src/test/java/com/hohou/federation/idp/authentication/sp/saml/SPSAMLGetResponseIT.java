package com.hohou.federation.idp.authentication.sp.saml;

import com.hohou.federation.idp.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SPSAMLGetResponseIT extends AbstractIntegrationTest {

    @Autowired
    private Filter samlFilter;

    @Autowired
    private Filter metadataGeneratorFilter;

    @Autowired
    private SAMLBootstrap samlBootstrap;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        samlBootstrap.postProcessBeanFactory(null);
        this.mockMvc = webAppContextSetup(this.wac).addFilters(metadataGeneratorFilter, samlFilter).build();
    }

    @Test
    public void when_get_response_then_404() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/sp/saml/sso"));
        resultActions.andExpect(status().isNotFound());
    }
}
