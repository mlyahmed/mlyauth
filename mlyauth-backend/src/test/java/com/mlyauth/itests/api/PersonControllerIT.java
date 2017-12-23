package com.mlyauth.itests.api;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.itests.AbstractIntegrationTest;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class PersonControllerIT extends AbstractIntegrationTest {

    public static final String ROOT_USERNAME = "root";
    public static final String ROOT_PASSWORD = "root";

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DataProvider
    public static Object[] properties() {
        // @formatter:off
        return new Object[][]{
                {"Ahmed", "EL IDRISSI", "mlyahmed", "ahmed.elidrissi@gmail.com", "password"},
                {"Mly", "ATTACH", "mlayhmed1", "mlyahmed1@gmail.com", "mlayhmed1"},
                {"Fatima-Ezzahrae", "EL IDRISSI", "fatiid", "fatima.elidrissi@yahoo.fr", "fatina"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("properties")
    public void when_create_a_new_person_then_create_him(String... properties) throws Exception {
        PersonBean personBean = given_person(properties);
        final ResultActions resultActions = when_create_new_person(personBean);
        then_he_is_well_created(resultActions, properties);

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
                fieldWithPath("email").type(JsonFieldType.STRING).description("email"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("password")
        );

        resultActions.andDo(document("person-creation-ok", requestDescription, responseDescription));

    }

    private void then_he_is_well_created(ResultActions resultActions, String... properties) throws Exception {
        resultActions.andExpect(status().isCreated());
        final Person person = personDAO.findByUsername(properties[2]);
        assertThat(person, notNullValue());
        assertThat(person.getId(), notNullValue());
        assertThat(person.getFirstname(), equalTo(properties[0]));
        assertThat(person.getLastname(), equalTo(properties[1]));
        assertThat(person.getUsername(), equalTo(properties[2]));
        assertThat(person.getEmail(), equalTo(properties[3]));
        assertThat(person.getPassword(), not(equalTo(properties[4])));
        assertTrue(passwordEncoder.matches(properties[4], person.getPassword()));
    }

    private PersonBean given_person(String... properties) {
        PersonBean personBean = new PersonBean();
        personBean.setFirstname(properties[0]);
        personBean.setLastname(properties[1]);
        personBean.setUsername(properties[2]);
        personBean.setEmail(properties[3]);
        personBean.setPassword(properties[4].toCharArray());
        return personBean;
    }


    @Test
    public void when_create_a_new_person_and_already_exists_then_error() throws Exception {
        PersonBean personBean = given_person("Moulay", "ATTACH", "mlyattach", "moulay.attach@gmail.com", "password");
        final ResultActions resultActions = when_create_new_person(personBean);
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].code", equalTo("PERSON_ALREADY_EXISTS")));
    }

    private ResultActions when_create_new_person(PersonBean personBean) throws Exception {
        return mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(personBean))
                .with(httpBasic(ROOT_USERNAME, ROOT_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

}
