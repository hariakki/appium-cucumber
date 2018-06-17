package com.jit.automation.ios.java.stepDefinitions;

import cucumber.api.java.en.Given;

public class InitialSetupDefinitions
{
	@Given("^the user opens app$")
	public void the_user_opens_app() throws Throwable 
	{
		Hook.init();
	}
}
