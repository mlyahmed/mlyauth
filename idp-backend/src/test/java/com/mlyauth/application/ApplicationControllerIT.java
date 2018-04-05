package com.mlyauth.application;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.tools.AccessTokenForTests;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private WebApplicationContext context;


    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private AccessTokenForTests accessTokenGenerator;

    private String access;
    private Person fatimaEzzahrae;
    private Application policyDev;

    protected MockMvc mockMvc;
    private ResultActions result;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        access = accessTokenGenerator.generateMasterToken();
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
        fatimaEzzahrae = personDAO.findOne(9001l);
        fatimaEzzahrae.getApplications().clear();
        personDAO.saveAndFlush(fatimaEzzahrae);
    }

    private void given_PolicyDev_an_existing_application() {
        policyDev = applicationDAO.findOne(9000l);
    }

    private void when_assign_PolicyDev_to_Fatima_Ezzahrae() throws Exception {
        result = mockMvc.perform(put("/domain/application/_assign/{app}/to/{personId}",
                policyDev.getAppname(),
                fatimaEzzahrae.getExternalId())
                .header("Authorization", "Bearer " + access)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8"));
    }

    private void then_is_it_accepted() throws Exception {
        result.andExpect(status().isAccepted());
    }

    private void then_PolicyDEV_is_assigned_to_Fatima_Ezzahrae() {
        final Person expected = personDAO.findOne(9001l);
        Assert.assertThat(expected.getApplications(), Matchers.is(Matchers.not(Matchers.empty())));
        Assert.assertThat(expected.getApplications(), Matchers.contains(policyDev));
    }
}
