package com.mlyauth.itests.controllers;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
                {"Moulay", "ATTACH", "attach", "mlyahmed1@gmail.com", "mlyattach"},
                {"Fatima-Ezzahrae", "EL IDRISSI", "fatiid", "fatima.elidrissi@yahoo.fr", "fatina"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("properties")
    public void when_create_a_new_person_then_create_it(String firstname, String lastname, String username, String email, String password) throws Exception {
        PersonBean personBean = new PersonBean();
        personBean.setFirstname(firstname);
        personBean.setLastname(lastname);
        personBean.setUsername(username);
        personBean.setEmail(email);
        personBean.setPassword(password.toCharArray());

        final ResultActions resultActions = mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(personBean))
                .with(httpBasic("root", "root"))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
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


}