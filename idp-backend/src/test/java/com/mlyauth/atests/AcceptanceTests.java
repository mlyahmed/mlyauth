package com.mlyauth.atests;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@CucumberOptions(glue = {"com.mlyauth.atests", "cucumber.api.spring"}, features = {"classpath:features"}, tags = {"~@Ignore"})
public class AcceptanceTests {
}
