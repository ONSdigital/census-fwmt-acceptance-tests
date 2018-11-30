package uk.gov.ons.fwmt.acceptancetests.runners;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "json:build/cucumber-report.json"},
    features = {"src/test/resources/acceptancetests/CensusTests.feature"},
    glue = {"uk.gov.ons.fwmt.acceptancetests.steps.census"})
public class CensusTestRunner {

}
