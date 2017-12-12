package com.mlyauth.atests.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class MlyAuthSteps extends AbstractStep {

    @Autowired
    private MockMvc mockMvc;

    @Given("^(.+) is a connected user$")
    public void mlyahmed_a_connected_user(String username) throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(content().string(containsString("Get your greeting")));
    }

    @Given("^(.+) is a Co-Application$")
    public void is_a_Co_Application(String appname) throws Exception {
    }

    @When("^(.+) navigates to (.+)$")
    public void user_navigate_to_app(String username, String appname) throws Exception {
    }

    @Then("^(.+) is connected to (.+)")
    public void user_is_connected_to_app(String username, String appname) throws Exception {
    }

    @Given("^(.+) is not asigned to (.+)")
    public void app_is_not_asigned_to_user(String appname, String username) throws Exception {
    }

    @Then("^(.+) error$")
    public void app_not_assigned_error(String errorCode) throws Exception {
    }


}
