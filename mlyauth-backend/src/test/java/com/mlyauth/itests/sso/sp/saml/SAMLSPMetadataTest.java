package com.mlyauth.itests.sso.sp.saml;

import com.mlyauth.itests.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SAMLSPMetadataTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @Ignore
    public void when_request_SP_metadata_then_returned() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/saml/metadata"));
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType("application/samlmetadata+xml"));
    }
}
