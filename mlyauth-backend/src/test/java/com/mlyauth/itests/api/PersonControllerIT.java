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
import org.springframework.http.HttpStatus;
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

    final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

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
    public void when_create_a_new_person_then_create_him(String firstname, String lastname, String username, String email, String password) throws Exception {
        PersonBean personBean = given_person(firstname, lastname, username, email, password);
        final ResultActions resultActions = when_create_new_person(personBean);
        then_he_is_well_created(firstname, lastname, username, email, password, resultActions);
    }

    private void then_he_is_well_created(String firstname, String lastname, String username, String email, String password, ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().is(HttpStatus.CREATED.value()));
        final Person person = personDAO.findByUsername(username);
        assertThat(person, notNullValue());
        assertThat(person.getId(), notNullValue());
        assertThat(person.getFirstname(), equalTo(firstname));
        assertThat(person.getLastname(), equalTo(lastname));
        assertThat(person.getUsername(), equalTo(username));
        assertThat(person.getEmail(), equalTo(email));
        assertThat(person.getPassword(), not(equalTo(password)));
        assertTrue(passwordEncoder.matches(password, person.getPassword()));
    }

    private PersonBean given_person(String firstname, String lastname, String username, String email, String password) {
        PersonBean personBean = new PersonBean();
        personBean.setFirstname(firstname);
        personBean.setLastname(lastname);
        personBean.setUsername(username);
        personBean.setEmail(email);
        personBean.setPassword(password.toCharArray());
        return personBean;
    }


    @Test
    public void when_create_a_new_person_and_already_exists_then_error() throws Exception {
        PersonBean personBean = given_person("Moulay", "ATTACH", "mlyattach", "moulay.attach@gmail.com", "password");
        final ResultActions resultActions = when_create_new_person(personBean);
        resultActions.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].code", equalTo("PERSON_ALREADY_EXISTS")));
    }

    private ResultActions when_create_new_person(PersonBean personBean) throws Exception {
        return mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(personBean))
                .with(httpBasic("root", "root"))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

}
