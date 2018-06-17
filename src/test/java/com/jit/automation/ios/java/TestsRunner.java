package com.jit.automation.ios.java;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;


@RunWith(Cucumber.class)

@CucumberOptions(
		strict = false, 
		features={"classpath:features/"},
		glue={"classpath:com/jit/automation/ios/java/stepDefinitions/"},
		format = { "junit:target/reports/junit.xml", "pretty","html:target/reports/cucumber-reports","json:target/cucumber.json" } 
	//	,tags = { "~@ignore"}
		)


public class TestsRunner 
{	

	  
}