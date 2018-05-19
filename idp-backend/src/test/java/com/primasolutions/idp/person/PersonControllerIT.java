package com.primasolutions.idp.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primasolutions.idp.AbstractIntegrationTest;
import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.ApplicationDAO;
import com.primasolutions.idp.authentication.Profile;
import com.primasolutions.idp.authentication.ProfileDAO;
import com.primasolutions.idp.constants.ProfileCode;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.tools.AccessTokenForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PersonControllerIT extends AbstractIntegrationTest {

    private static final long FATIMA_EZZAHRAE_ID = 9001L;
    private static final int EMAIL_INDEX = 4;
    private static final int EXTERNAL_ID_INDEX = 0;
    private static final int FIRSTNAME_INDEX = 1;
    private static final int LASTNAME_INDEX = 2;
    private static final int BIRTHDATE_INDEX = 3;
    private static final int PASSWORD_INDEX = 5;
    private static final long POLICY_DEV_ID = 9000L;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PersonLookuper personLookuper;

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;
    private ResultActions result;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private AccessTokenForTests accessTokenGenerator;

    private String access;

    private Person fatimaEzzahrae;
    private Application policy;

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
    public void when_create_a_new_person_then_create_him(final String... properties) throws Exception {
        given_the_root_is_a_master();
        PersonBean personBean = given_person(properties);
        when_create_new_person(personBean);
        then_is_created();
        and_he_is_well_created(properties);
    }

    @Test
    public void when_create_a_new_person_and_already_exists_then_error() throws Exception {
        given_the_root_is_a_master();
        final String[] properties = {"201254", "Moulay", "ATTACH", "1980-02-15", "moulay.attach@gmail.com", "password"};
        PersonBean personBean = given_person(properties);
        when_create_new_person(personBean);
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].code", equalTo("EMAIL_ALREADY_EXISTS")));
    }

    @Test
    @UseDataProvider("properties")
    public void when_create_a_new_person_without_matser_profile_then_error(final String... prop) throws Exception {
        given_the_root_is_not_a_master();
        PersonBean personBean = given_person(prop);
        when_create_new_person(personBean);
        result.andExpect(status().isForbidden());
    }

    @Test
    public void when_assign_an_application_to_person_then_must_be_assigned() throws Exception {
        given_Fatima_Ezzahrare_an_existing_person_without_an_application();
        given_PolicyDev_an_existing_application();
        when_assign_PolicyDev_to_Fatima_Ezzahrae();
        then_is_it_accepted();
        then_PolicyDEV_is_assigned_to_Fatima_Ezzahrae();

    }

    private void given_Fatima_Ezzahrare_an_existing_person_without_an_application() {
        fatimaEzzahrae = personDAO.findOne(FATIMA_EZZAHRAE_ID);
        fatimaEzzahrae.getApplications().clear();
        personDAO.saveAndFlush(fatimaEzzahrae);
    }

    private void given_PolicyDev_an_existing_application() {
        policy = applicationDAO.findOne(POLICY_DEV_ID);
    }

    private void when_assign_PolicyDev_to_Fatima_Ezzahrae() throws Exception {
        result = mockMvc.perform(put("/domain/person/_assign/{appname}/to/{personExternalId}",
                policy.getAppname(),
                fatimaEzzahrae.getExternalId())
                .header("Authorization", "Bearer " + access)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"));
    }

    private void then_is_it_accepted() throws Exception {
        result.andExpect(status().isAccepted());
    }

    private void then_PolicyDEV_is_assigned_to_Fatima_Ezzahrae() {
        final Person expected = personDAO.findOne(FATIMA_EZZAHRAE_ID);
        Assert.assertThat(expected.getApplications(), Matchers.is(Matchers.not(Matchers.empty())));
        Assert.assertThat(expected.getApplications(), Matchers.contains(policy));
    }

    private void given_the_root_is_a_master() {
        final Person master = personLookuper.byEmail(MASTER_EMAIL);
        final Profile masterProfile = profileDAO.findOne(ProfileCode.MASTER);
        master.setProfiles(new HashSet<>(Arrays.asList(masterProfile)));
        personDAO.save(master);
    }

    private void given_the_root_is_not_a_master() {
        final Person master = personLookuper.byEmail(MASTER_EMAIL);
        master.getProfiles().clear();
        personDAO.save(master);
    }

    private void and_he_is_well_created(final String... properties) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final Person person = personLookuper.byEmail(properties[EMAIL_INDEX]);
        assertThat(person, notNullValue());
        assertThat(person.getAuthenticationInfo(), notNullValue());
        assertThat(person.getId(), notNullValue());
        assertThat(person.getExternalId(), equalTo(properties[EXTERNAL_ID_INDEX]));
        assertThat(person.getFirstname(), equalTo(properties[FIRSTNAME_INDEX]));
        assertThat(person.getLastname(), equalTo(properties[LASTNAME_INDEX]));
        assertThat(person.getBirthdate(), notNullValue());
        assertThat(person.getBirthdate(), equalTo(formatter.parse(properties[BIRTHDATE_INDEX])));
        assertThat(person.getAuthenticationInfo().getLogin(), equalTo(properties[EMAIL_INDEX]));
        assertThat(person.getEmail(), equalTo(properties[EMAIL_INDEX]));
        assertThat(person.getAuthenticationInfo().getPassword(), not(equalTo(properties[PASSWORD_INDEX])));
        assertTrue(passwordEncoder.matches(properties[PASSWORD_INDEX], person.getAuthenticationInfo().getPassword()));
    }

    private void then_is_created() throws Exception {
        result.andExpect(status().isCreated());
    }

    private PersonBean given_person(final String... properties) {
        PersonBean personBean = new PersonBean();
        personBean.setRole(RoleCode.CLIENT);
        personBean.setExternalId(properties[EXTERNAL_ID_INDEX]);
        personBean.setFirstname(properties[FIRSTNAME_INDEX]);
        personBean.setLastname(properties[LASTNAME_INDEX]);
        personBean.setBirthdate(properties[BIRTHDATE_INDEX]);
        personBean.setEmail(properties[EMAIL_INDEX]);
        personBean.setPassword(properties[PASSWORD_INDEX].toCharArray());
        return personBean;
    }

    private void when_create_new_person(final PersonBean personBean) throws Exception {
        result = mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(personBean))
                .header("Authorization", "Bearer " + access)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));
    }

}
