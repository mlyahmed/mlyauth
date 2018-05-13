package com.primasolutions.idp.atests.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primasolutions.idp.atests.world.CurrentPersonHolder;
import com.primasolutions.idp.tools.AccessTokenForTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Component
@Scope("cucumber-glue")
public class RestTestHelper {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrentPersonHolder currentPersonHolder;

    @Autowired
    private AccessTokenForTests accessTokenGenerator;

    public ResultActions performBearerPost(final String endpoint, final Object content) throws Exception {
        return mockMvc.perform(post(endpoint)
                .content(mapper.writeValueAsString(content))
                .header("Authorization", "Bearer " + accessTokenGenerator.generateMasterToken())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

    public ResultActions performBearerPut(final String endpoint, final Object content) throws Exception {
        return mockMvc.perform(put(endpoint)
                .content(mapper.writeValueAsString(content))
                .header("Authorization", "Bearer " + accessTokenGenerator.generateMasterToken())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

    public ResultActions performBearerGet(final String endpoint) throws Exception {
        return mockMvc.perform(get(endpoint)
                .header("Authorization", "Bearer " + accessTokenGenerator.generateMasterToken())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

    public ResultActions performBasicGet(final String endpoint) throws Exception {
        return mockMvc.perform(get(endpoint)
                .with(httpBasic(currentPersonHolder.getUsername(), currentPersonHolder.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

}