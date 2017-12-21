package com.mlyauth.itests.controllers;

import com.mlyauth.itests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasicAuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void when_user_password_match_then_ok() throws Exception {
        final ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .param("username", "root")
                .param("password", "root")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .characterEncoding("UTF-8"));
        resultActions.andExpect(status().is(HttpStatus.OK.value())).andExpect(forwardedUrl("/index.html"))
        ;
    }

    @Test
    public void when_user_password_match_then_redirect_to_login() throws Exception {
        final ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .param("username", "root")
                .param("password", "ddd")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .characterEncoding("UTF-8"));
        resultActions.andExpect(status().is(HttpStatus.OK.value())).andExpect(forwardedUrl("/person/login?error=true"));
    }
}
