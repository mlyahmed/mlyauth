package com.primasolutions.idp.atests;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@CucumberOptions(glue = {"com.primasolutions.idp.atests", "cucumber.api.spring"},
        features = {"classpath:features"}, tags = {"not @Ignore"})
public class AcceptanceTests {

}
