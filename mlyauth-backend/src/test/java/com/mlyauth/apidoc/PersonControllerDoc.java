package com.mlyauth.apidoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlyauth.itests.AbstractIntegrationTest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PersonControllerDoc extends AbstractIntegrationTest {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    protected ObjectMapper mapper;

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)).build();
    }


    @Test
    public void when_person_creation_is_ok_then_document_it() throws Exception {

        Map<String, Object> person = new HashMap<>();
        person.put("externalId", RandomStringUtils.random(20, true, true));
        person.put("firstname", "Ahmed");
        person.put("lastname", "Ahmed");
        person.put("username", "Ahmed");
        person.put("email", "Ahmed");
        person.put("password", "Ahmed");

        final ResultActions result = mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(person))
                .with(httpBasic(ROOT_USERNAME, ROOT_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        result.andExpect(status().isCreated());

        final RequestFieldsSnippet requestDescription = PayloadDocumentation.relaxedRequestFields(
                fieldWithPath("firstname").type(JsonFieldType.STRING).description("Firstname"),
                fieldWithPath("lastname").type(JsonFieldType.STRING).description("Lastname"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("Username"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("email"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("password")
        );

        final ResponseFieldsSnippet responseDescription = PayloadDocumentation.relaxedResponseFields(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("id"),
                fieldWithPath("firstname").type(JsonFieldType.STRING).description("Firstname"),
                fieldWithPath("lastname").type(JsonFieldType.STRING).description("Lastname"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("Username"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("email")
        );

        result.andDo(document("post-person-ok", requestDescription, responseDescription));
    }

}
