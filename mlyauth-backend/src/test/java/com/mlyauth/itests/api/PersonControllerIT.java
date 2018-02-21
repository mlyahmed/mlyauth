package com.mlyauth.itests.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.itests.AbstractIntegrationTest;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class PersonControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;


    @Autowired
    private MockMvc mockMvc;

    @DataProvider
    public static Object[] properties() {
        // @formatter:off
        return new Object[][]{
                {"2154", "Ahmed", "EL IDRISSI", "ahmed.elidrissi@gmail.com", "password"},
                {"5121", "Mly", "ATTACH", "mlyahmed1@gmail.com", "mlayhmed1"},
                {"5487", "Fatima-Ezzahrae", "EL IDRISSI", "fatima.elidrissi@yahoo.fr", "fatina"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("properties")
    public void when_create_a_new_person_then_create_him(String... properties) throws Exception {
        PersonBean personBean = given_person(properties);
        final ResultActions resultActions = when_create_new_person(personBean);
        then_is_created(resultActions);
        and_he_is_well_created(resultActions, properties);
    }

    @Test
    public void when_create_a_new_person_and_already_exists_then_error() throws Exception {
        PersonBean personBean = given_person("201254", "Moulay", "ATTACH", "moulay.attach@gmail.com", "password");
        final ResultActions resultActions = when_create_new_person(personBean);
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].code", equalTo("PERSON_ALREADY_EXISTS")));
    }


    private void and_he_is_well_created(ResultActions resultActions, String... properties) throws Exception {
        final Person person = personDAO.findByEmail(properties[3]);
        assertThat(person, notNullValue());
        assertThat(person.getAuthenticationInfo(), notNullValue());
        assertThat(person.getId(), notNullValue());
        assertThat(person.getExternalId(), equalTo(properties[0]));
        assertThat(person.getFirstname(), equalTo(properties[1]));
        assertThat(person.getLastname(), equalTo(properties[2]));
        assertThat(person.getAuthenticationInfo().getLogin(), equalTo(properties[3]));
        assertThat(person.getEmail(), equalTo(properties[3]));
        assertThat(person.getAuthenticationInfo().getPassword(), not(equalTo(properties[4])));
        assertTrue(passwordEncoder.matches(properties[4], person.getAuthenticationInfo().getPassword()));
    }

    private void then_is_created(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isCreated());
    }

    private PersonBean given_person(String... properties) {
        PersonBean personBean = new PersonBean();
        personBean.setExternalId(properties[0]);
        personBean.setFirstname(properties[1]);
        personBean.setLastname(properties[2]);
        personBean.setEmail(properties[3]);
        personBean.setPassword(properties[4].toCharArray());
        return personBean;
    }


    private ResultActions when_create_new_person(PersonBean personBean) throws Exception {
        return mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(personBean))
                .with(httpBasic(ROOT_USERNAME, ROOT_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

}
