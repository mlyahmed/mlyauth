package com.hohou.federation.idp.authentication.sp.basic;

import com.hohou.federation.idp.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
public class BasicAuthControllerIT extends AbstractIntegrationTest {

    public static final String AUTHENTICATION_PATH = "/login.html";
    public static final String FAILED_AUTHENTICATION_URL = "/login-error.html";
    public static final String HOME_URL = "/home";

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void when_user_password_match_then_ok() throws Exception {
        final ResultActions resultActions = mockMvc.perform(post(AUTHENTICATION_PATH)
                .param("username", MASTER_EMAIL)
                .param("password", MASTER_PASSWORD)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .characterEncoding("UTF-8"));
        resultActions.andExpect(status().is(HttpStatus.OK.value())).andExpect(forwardedUrl(HOME_URL));
    }

    @Test
    public void when_user_password_match_then_redirect_to_login() throws Exception {
        final ResultActions resultActions = mockMvc.perform(post(AUTHENTICATION_PATH)
                .param("username", MASTER_EMAIL)
                .param("password", "ddd")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .characterEncoding("UTF-8"));
        resultActions.andExpect(status().is(HttpStatus.FOUND.value()))
                .andExpect(redirectedUrl(FAILED_AUTHENTICATION_URL));
    }
}
