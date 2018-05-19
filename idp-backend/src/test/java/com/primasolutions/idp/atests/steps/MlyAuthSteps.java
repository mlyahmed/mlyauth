package com.primasolutions.idp.atests.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primasolutions.idp.application.ApplicationBean;
import com.primasolutions.idp.application.AttributeBean;
import com.primasolutions.idp.atests.domain.RestTestHelper;
import com.primasolutions.idp.atests.world.ApplicationsHolder;
import com.primasolutions.idp.atests.world.CurrentPersonHolder;
import com.primasolutions.idp.atests.world.ResultActionHolder;
import com.primasolutions.idp.constants.ApplicationTypeCode;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.person.PersonBean;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.primasolutions.idp.application.AttributeBean.newAttribute;
import static com.primasolutions.idp.constants.AspectAttribute.SP_BASIC_PASSWORD;
import static com.primasolutions.idp.constants.AspectAttribute.SP_BASIC_SSO_URL;
import static com.primasolutions.idp.constants.AspectAttribute.SP_BASIC_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MlyAuthSteps extends AbstractStepsDef {

    private static final String RANDOM_EXTERNAL_ID = RandomStringUtils.random(20, true, true);

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ApplicationsHolder applicationHolder;

    @Autowired
    private CurrentPersonHolder currentPersonHolder;

    @Autowired
    private RestTestHelper restTestHelper;


    @Autowired
    private ResultActionHolder resultActionHolder;

    @Given("^(.+), (.+) is a new person with (.+) as username and (.+) as email$")
    public void an_existed_person_withe_username_and_email(final String firstname, final String lastname,
                                                           final String username, final String email) throws Exception {
        PersonBean person = PersonBean.newInstance()
                .setRole(RoleCode.CLIENT.getValue())
                .setFirstname(firstname)
                .setExternalId(RANDOM_EXTERNAL_ID)
                .setLastname(lastname)
                .setBirthdate("1984-10-17")
                .setEmail(email)
                .setPassword("password".toCharArray());
        final ResultActions result = restTestHelper.performBearerPost("/domain/person", person)
                .andExpect(status().is(CREATED.value()));
        person = mapper.readValue(result.andReturn().getResponse().getContentAsString(), PersonBean.class);
        person.setPassword("password".toCharArray());
        currentPersonHolder.setCurrentPerson(person);
    }

    @Given("^(.+) is a registered Application$")
    public void is_a_registered_application(final String appname) throws Exception {
        ApplicationBean application = new ApplicationBean();
        application.setType(ApplicationTypeCode.STORE);
        application.setAppname(appname);
        application.setTitle(appname);
        final ResultActions result = restTestHelper.performBearerPost("/domain/application", application)
                .andExpect(status().is(CREATED.value()));
        applicationHolder.addApplication(mapper.readValue(result.andReturn().getResponse().getContentAsString(),
                ApplicationBean.class));
    }

    @Given("^(.+) has the basic authentication aspect$")
    public void application_has_the_basic_authentication_aspect(final String appname) throws Exception {
        final ApplicationBean application = applicationHolder.getApplication(appname);
        Map<String, AttributeBean> authSettings = new LinkedHashMap<>();
        authSettings.put(SP_BASIC_SSO_URL.getValue(), newAttribute(SP_BASIC_SSO_URL.getValue())
                .setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        authSettings.put(SP_BASIC_USERNAME.getValue(), newAttribute(SP_BASIC_USERNAME.getValue())
                .setAlias("j_username").setValue("gestF"));
        authSettings.put(SP_BASIC_PASSWORD.getValue(), newAttribute(SP_BASIC_PASSWORD.getValue())
                .setAlias("j_password").setValue("gestF"));
        application.setAuthSettings(authSettings);
        application.setAuthAspect(AspectType.SP_BASIC);
        restTestHelper.performBearerPut("/domain/application", applicationHolder.getApplication(appname))
                .andExpect(status().is(ACCEPTED.value()));
    }


    @When("^(.+) navigates to (.+)$")
    public void user_navigate_to_app(final String username, final String appname) throws Exception {
         resultActionHolder.setResultActions(restTestHelper.performBasicGet("/navigate/forward/basic/to/" + appname));
    }

    @Then("^(.+) is posted to (.+)")
    public void user_is_connected_to_app(final String username, final String appname) throws Exception {
        resultActionHolder.getResultActions()
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(request().attribute("navigation", notNullValue()));
    }

    @Given("^(.+) is asigned to (.+)")
    public void app_is_asigned_to_user(final String appname, final String username) throws Exception {
        final String endpoint = String.format("/domain/person/_assign/%s/to/%s", appname,
                currentPersonHolder.getCurrentPerson().getExternalId());
        restTestHelper.performBearerPut(endpoint, null).andExpect(status().is(ACCEPTED.value()));
    }

    @Given("^(.+) is not asigned to (.+)")
    public void app_is_not_asigned_to_user(final String appname, final String username) throws Exception {
        currentPersonHolder.getCurrentPerson().getApplications().remove(appname);
        restTestHelper.performBearerPut("/domain/person", currentPersonHolder.getCurrentPerson())
                .andExpect(status().is(ACCEPTED.value()));
    }

    @Then("^(.+) error$")
    public void app_not_assigned_error(final String errorCode) throws Exception {
        resultActionHolder.getResultActions()
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(request().attribute("MLY_AUTH_ERROR_CODE", equalTo(errorCode)));
    }


}
