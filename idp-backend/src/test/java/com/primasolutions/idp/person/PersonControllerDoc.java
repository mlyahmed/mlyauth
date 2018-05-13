package com.primasolutions.idp.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primasolutions.idp.AbstractIntegrationTest;
import com.primasolutions.idp.tools.AccessTokenForTests;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PersonControllerDoc extends AbstractIntegrationTest {

    private static final String RANDOM_EXTERNAL_ID = RandomStringUtils.random(20, true, true);

    @Rule
    public final JUnitRestDocumentation doc = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private AccessTokenForTests accessTokenGenerator;

    private String access;
    private Map<String, Object> person;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(documentationConfiguration(doc)).build();
        access = accessTokenGenerator.generateMasterToken();
    }


    @Test
    public void when_person_creation_is_ok_then_document_it() throws Exception {

        given_a_person();

        final ResultActions result = mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(person))
                .header("Authorization", "Bearer " + access)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        result.andExpect(status().isCreated());

        final RequestFieldsSnippet requestDescription = PayloadDocumentation.relaxedRequestFields(
                fieldWithPath("firstname").type(JsonFieldType.STRING).description("Firstname"),
                fieldWithPath("lastname").type(JsonFieldType.STRING).description("Lastname"),
                fieldWithPath("birthdate").type(JsonFieldType.STRING).description("birthdate"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("email"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("password")
        );

        final ResponseFieldsSnippet responseDescription = PayloadDocumentation.relaxedResponseFields(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("id"),
                fieldWithPath("firstname").type(JsonFieldType.STRING).description("Firstname"),
                fieldWithPath("lastname").type(JsonFieldType.STRING).description("Lastname"),
                fieldWithPath("birthdate").type(JsonFieldType.STRING).description("birthdate"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("email")
        );

        result.andDo(document("post-person-ok", requestDescription, responseDescription));
    }

    private void given_a_person() {
        person = new HashMap<>();
        person.put("role", "CLIENT");
        person.put("externalId", RANDOM_EXTERNAL_ID);
        person.put("firstname", "Ahmed");
        person.put("lastname", "Ahmed");
        person.put("birthdate", "1987-01-15");
        person.put("email", "ahmed@elidrissi.ma");
        person.put("password", "Ahmed");
    }

}
