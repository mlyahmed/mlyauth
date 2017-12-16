package com.mlyauth.atests.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlyauth.atests.domain.RestHelper;
import com.mlyauth.atests.world.ApplicationsHolder;
import com.mlyauth.atests.world.CurrentPersonHolder;
import com.mlyauth.beans.ApplicationBean;
import com.mlyauth.beans.AttributeMap;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.dao.AuthAspectDAO;
import com.mlyauth.domain.AuthAspect;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.mlyauth.beans.AttributeBean.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MlyAuthSteps extends AbstractStep {

    final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationsHolder applicationHolder;

    @Autowired
    private CurrentPersonHolder currentPersonHolder;

    @Autowired
    private RestHelper restHelper;

    @Autowired
    private AuthAspectDAO authAspectDAO;


    @Given("^(.+), (.+) is a new person withe (.+) as username and (.+) as email$")
    public void an_existed_person_withe_username_and_email(String firstname, String lastname, String username, String email) throws Exception {
        PersonBean person = new PersonBean();
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setUsername(username);
        person.setEmail(email);
        person.setPassword("password".toCharArray());
        final ResultActions resultActions = restHelper.performPost("/domain/person", person).andExpect(status().is(CREATED.value()));
        currentPersonHolder.setCurrentPerson(mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), PersonBean.class));
    }

    @Given("^(.+) is a registered Application$")
    public void is_a_registered_application(String appname) throws Exception {
        ApplicationBean application = new ApplicationBean();
        application.setAppname(appname);
        application.setTitle(appname);
        final ResultActions resultActions = restHelper.performPost("/domain/application", application).andExpect(status().is(CREATED.value()));
        applicationHolder.addApplication(mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApplicationBean.class));
    }

    @Given("^(.+) has the basic authentication aspect$")
    public void application_has_the_basic_authentication_aspect(String appname) throws Exception {
        final AuthAspect basic = authAspectDAO.findOne(AuthAspectType.AUTH_BASIC);

        final ApplicationBean application = applicationHolder.getApplication(appname);
        application.setAuthAspect(AuthAspectType.AUTH_BASIC);
        application.getAuthSettings().add(new AttributeMap(createAuthAttr("authurl"), BASIC_AUTH_ENDPOINT, "https://uat-sgi-policy01.prima-solutions.com/primainsure/j_spring_security_check"));
        application.getAuthSettings().add(new AttributeMap(createAuthAttr("j_username"), BASIC_AUTH_USERNAME, "gestF"));
        application.getAuthSettings().add(new AttributeMap(createAuthAttr("j_password"), BASIC_AUTH_PASSWORD, "gestF"));


        restHelper.performPut("/domain/application", applicationHolder.getApplication(appname)).andExpect(status().is(ACCEPTED.value()));
    }


    @When("^(.+) navigates to (.+)$")
    public void user_navigate_to_app(String username, String appname) throws Exception {
        restHelper.performGet("/route/navigate/to/"+appname)
                .andExpect(status().is(OK.value()))
                //.andExpect(forwardedUrl("https://uat-sgi-policy01.prima-solutions.com/primainsure/j_spring_security_check"))
        ;
    }

    @Then("^(.+) is connected to (.+)")
    public void user_is_connected_to_app(String username, String appname) throws Exception {
        throw new PendingException();
    }

    @Given("^(.+) is not asigned to (.+)")
    public void app_is_not_asigned_to_user(String appname, String username) throws Exception {
        throw new PendingException();
    }

    @Then("^(.+) error$")
    public void app_not_assigned_error(String errorCode) throws Exception {
        throw new PendingException();
    }


}
