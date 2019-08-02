package uk.gov.ons.census.fwmt.tests.acceptance.runners;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "json:build/cucumber-report.json"},
        features = {"src/test/resources/acceptancetests/CCSCSVServiceTests.feature"},
        glue = {"uk.gov.ons.census.fwmt.tests.acceptance.steps.ccscsvservice"})
public class CCSCSVServiceTestRunner {

}
