package com.mlyauth.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.ProfileCode;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.dao.ProfileDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.Profile;
import com.mlyauth.tools.AccessTokenForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class PersonControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private AccessTokenForTests accessTokenGenerator;

    private String access;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        access = accessTokenGenerator.generateMasterToken();
    }

    @DataProvider
    public static Object[] properties() {
        // @formatter:off
        return new Object[][]{
                {"2154", "Ahmed", "EL IDRISSI", "1984-10-17", "ahmed.elidrissi@gmail.com", "password"},
                {"5121", "Mly", "ATTACH", "1982-11-07", "mlyahmed1@gmail.com", "mlayhmed1"},
                {"5487", "Fatima-Ezzahrae", "EL IDRISSI", "1983-01-27", "fatima.elidrissi@yahoo.fr", "fatima"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("properties")
    public void when_create_a_new_person_then_create_him(String... properties) throws Exception {
        given_the_root_is_a_master();
        PersonBean personBean = given_person(properties);
        final ResultActions resultActions = when_create_new_person(personBean);
        then_is_created(resultActions);
        and_he_is_well_created(properties);
    }

    @Test
    public void when_create_a_new_person_and_already_exists_then_error() throws Exception {
        given_the_root_is_a_master();
        PersonBean personBean = given_person("201254", "Moulay", "ATTACH", "1980-02-15", "moulay.attach@gmail.com", "password");
        final ResultActions resultActions = when_create_new_person(personBean);
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].code", equalTo("PERSON_ALREADY_EXISTS")));
    }

    @Test
    @UseDataProvider("properties")
    public void when_create_a_new_person_without_matser_profile_then_error(String... properties) throws Exception {
        given_the_root_is_not_a_master();
        PersonBean personBean = given_person(properties);
        final ResultActions resultActions = when_create_new_person(personBean);
        resultActions.andExpect(status().isForbidden());
    }

    private void given_the_root_is_a_master() {
        final Person master = personDAO.findByEmail(MASTER_EMAIL);
        final Profile masterProfile = profileDAO.findOne(ProfileCode.MASTER);
        master.setProfiles(new HashSet<>(Arrays.asList(masterProfile)));
        personDAO.save(master);
    }

    private void given_the_root_is_not_a_master() {
        final Person master = personDAO.findByEmail(MASTER_EMAIL);
        master.getProfiles().clear();
        personDAO.save(master);
    }

    private void and_he_is_well_created(String... properties) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final Person person = personDAO.findByEmail(properties[4]);
        assertThat(person, notNullValue());
        assertThat(person.getAuthenticationInfo(), notNullValue());
        assertThat(person.getId(), notNullValue());
        assertThat(person.getExternalId(), equalTo(properties[0]));
        assertThat(person.getFirstname(), equalTo(properties[1]));
        assertThat(person.getLastname(), equalTo(properties[2]));
        assertThat(person.getBirthdate(), notNullValue());
        assertThat(person.getBirthdate(), equalTo(formatter.parse(properties[3])));
        assertThat(person.getAuthenticationInfo().getLogin(), equalTo(properties[4]));
        assertThat(person.getEmail(), equalTo(properties[4]));
        assertThat(person.getAuthenticationInfo().getPassword(), not(equalTo(properties[5])));
        assertTrue(passwordEncoder.matches(properties[5], person.getAuthenticationInfo().getPassword()));
    }

    private void then_is_created(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isCreated());
    }

    private PersonBean given_person(String... properties) {
        PersonBean personBean = new PersonBean();
        personBean.setExternalId(properties[0]);
        personBean.setFirstname(properties[1]);
        personBean.setLastname(properties[2]);
        personBean.setBirthdate(properties[3]);
        personBean.setEmail(properties[4]);
        personBean.setPassword(properties[5].toCharArray());
        return personBean;
    }


    private ResultActions when_create_new_person(PersonBean personBean) throws Exception {
        return mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(personBean))
                .header("Authorization", "Bearer " + access)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

}
