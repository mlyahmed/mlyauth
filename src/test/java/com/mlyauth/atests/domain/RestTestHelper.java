package com.mlyauth.atests.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlyauth.atests.world.CurrentPersonHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Component
@Scope("cucumber-glue")
public class RestTestHelper {

    final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrentPersonHolder currentPersonHolder;

    public ResultActions performPost(String endpoint, Object content) throws Exception {
        return mockMvc.perform(post(endpoint)
                .content(mapper.writeValueAsString(content))
                .with(httpBasic(currentPersonHolder.getUsername(), currentPersonHolder.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

    public ResultActions performPut(String endpoint, Object content) throws Exception {
        return mockMvc.perform(put(endpoint)
                .content(mapper.writeValueAsString(content))
                .with(httpBasic(currentPersonHolder.getUsername(), currentPersonHolder.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

    public ResultActions performGet(String endpoint) throws Exception {
        return mockMvc.perform(get(endpoint)
                .with(httpBasic(currentPersonHolder.getUsername(), currentPersonHolder.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }
}
