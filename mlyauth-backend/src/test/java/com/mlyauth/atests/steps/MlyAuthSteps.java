package com.mlyauth.atests.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlyauth.atests.domain.RestTestHelper;
import com.mlyauth.atests.world.ApplicationsHolder;
import com.mlyauth.atests.world.CurrentPersonHolder;
import com.mlyauth.atests.world.ResultActionHolder;
import com.mlyauth.beans.ApplicationBean;
import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.AuthAspectType;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.mlyauth.beans.AttributeBean.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MlyAuthSteps extends AbstractStepsDef{

    final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ApplicationsHolder applicationHolder;

    @Autowired
    private CurrentPersonHolder currentPersonHolder;

    @Autowired
    private RestTestHelper restTestHelper;


    @Autowired
    private ResultActionHolder resultActionHolder;

    @Given("^(.+), (.+) is a new person with (.+) as username and (.+) as email$")
    public void an_existed_person_withe_username_and_email(String firstname, String lastname, String username, String email) throws Exception {
        PersonBean person = PersonBean.newInstance()
                .setFirstname(firstname)
                .setLastname(lastname)
                .setUsername(username)
                .setEmail(email)
                .setPassword("password".toCharArray());
        final ResultActions resultActions = restTestHelper.performPost("/domain/person", person).andExpect(status().is(CREATED.value()));
        person = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), PersonBean.class);
        person.setPassword("password".toCharArray());
        currentPersonHolder.setCurrentPerson(person);
    }

    @Given("^(.+) is a registered Application$")
    public void is_a_registered_application(String appname) throws Exception {
        ApplicationBean application = new ApplicationBean();
        application.setAppname(appname);
        application.setTitle(appname);
        final ResultActions resultActions = restTestHelper.performPost("/domain/application", application).andExpect(status().is(CREATED.value()));
        applicationHolder.addApplication(mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApplicationBean.class));
    }

    @Given("^(.+) has the basic authentication aspect$")
    public void application_has_the_basic_authentication_aspect(String appname) throws Exception {
        final ApplicationBean application = applicationHolder.getApplication(appname);
        Map<String, AttributeBean> authSettings = new LinkedHashMap<>();
        authSettings.put(BASIC_AUTH_ENDPOINT.getCode(), BASIC_AUTH_ENDPOINT.clone().setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        authSettings.put(BASIC_AUTH_USERNAME.getCode(), BASIC_AUTH_USERNAME.clone().setAlias("j_username").setValue("gestF"));
        authSettings.put(BASIC_AUTH_PASSWORD.getCode(), BASIC_AUTH_PASSWORD.clone().setAlias("j_password").setValue("gestF"));
        application.setAuthSettings(authSettings);
        application.setAuthAspect(AuthAspectType.AUTH_BASIC);
        restTestHelper.performPut("/domain/application", applicationHolder.getApplication(appname)).andExpect(status().is(ACCEPTED.value()));
    }


    @When("^(.+) navigates to (.+)$")
    public void user_navigate_to_app(String username, String appname) throws Exception {
        resultActionHolder.setResultActions(restTestHelper.performGet("/navigate/to/" + appname));
    }

    @Then("^(.+) is posted to (.+)")
    public void user_is_connected_to_app(String username, String appname) throws Exception {
        resultActionHolder.getResultActions()
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(request().attribute(BASIC_AUTH_ENDPOINT.getCode(), hasProperty("alias", Matchers.equalTo("authurl"))))
                .andExpect(request().attribute(BASIC_AUTH_ENDPOINT.getCode(), hasProperty("value", Matchers.equalTo("https://localhost/j_spring_security_check"))))
                .andExpect(request().attribute(BASIC_AUTH_USERNAME.getCode(), hasProperty("alias", Matchers.equalTo("j_username"))))
                .andExpect(request().attribute(BASIC_AUTH_USERNAME.getCode(), hasProperty("value", Matchers.equalTo("gestF"))))
                .andExpect(request().attribute(BASIC_AUTH_PASSWORD.getCode(), hasProperty("alias", Matchers.equalTo("j_password"))))
                .andExpect(request().attribute(BASIC_AUTH_PASSWORD.getCode(), hasProperty("value", Matchers.equalTo("gestF"))))
        ;
    }

    @Given("^(.+) is asigned to (.+)")
    public void app_is_asigned_to_user(String appname, String username) throws Exception {
        currentPersonHolder.getCurrentPerson().getApplications().add(appname);
        restTestHelper.performPut("/domain/person", currentPersonHolder.getCurrentPerson()).andExpect(status().is(ACCEPTED.value()));
    }

    @Given("^(.+) is not asigned to (.+)")
    public void app_is_not_asigned_to_user(String appname, String username) throws Exception {
        currentPersonHolder.getCurrentPerson().getApplications().remove(appname);
        restTestHelper.performPut("/domain/person", currentPersonHolder.getCurrentPerson()).andExpect(status().is(ACCEPTED.value()));
    }

    @Then("^(.+) error$")
    public void app_not_assigned_error(String errorCode) throws Exception {
        resultActionHolder.getResultActions()
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(request().attribute("MLY_AUTH_ERROR_CODE", equalTo(errorCode)));
    }


}
