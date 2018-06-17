package com.jit.automation.ios.java.stepDefinitions;

import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import com.jit.automation.ios.driver.AppiumHelper;
import com.jit.automation.ios.utils.PropertyUtil;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class Hook
{
	final static Logger LOGGER = Logger.getLogger(Hook.class);
	private static boolean INITIALIZED = false;
	
    @Before
	public void beforeScenario(Scenario scenario) throws AssumptionViolatedException
	{
		LOGGER.info("********* Automation execution started for scenario:"+scenario.getName());	
		AppiumHelper.CURRENT_SCENARIO=scenario;
	    try
	    {
	    	Assume.assumeTrue(AppiumHelper.EXECUTE_TEST);
	    }
	    catch(Exception e)
	    {
	    	AppiumHelper.closeDriver();
	    	throw new RuntimeException("Rest of the test cases will not be executed due to assumption violated:"+e.fillInStackTrace());
	    }
	}
	@After
	public void afterScenario(Scenario scenario) throws Exception
	{
		LOGGER.info("********* Automation execution Completed for scenario:"+scenario.getName());
		//if(scenario.isFailed())
		{
			AppiumHelper.saveScreenShot(scenario.getName().replace(' ', '_'), scenario);
		}
	}
	public static void init()
   	{
       	LOGGER.info("Initializing...");
       	if(!INITIALIZED)
   		{
   			try 
   			{
   				String config=System.getProperty("Counfiguration");
   				if(null==config)
   				{
   						LOGGER.info("Setting configuration:"+config);
   					    PropertyUtil.loadPropertyFile(config);
   						INITIALIZED=true;
   						if(AppiumHelper.startAppiumServer())
   						{
   							AppiumHelper.loadDriver();
   	   						LOGGER.info("Driver initialized successfully.");
   						}
   						else
   						{
   							AppiumHelper.EXECUTE_TEST=false;	
   							//stop execution for remaining test cases, as there is error in initialization...
   						}		
   				}
   				else
   				{
   					LOGGER.info("No configuration provided!");
   				}
   			} 
   			catch (Exception e) 
   			{
   				LOGGER.error("Error while initializing driver:"+e.getStackTrace());
   				LOGGER.error(e.fillInStackTrace());
   				AppiumHelper.EXECUTE_TEST=false;
   			}
   		}
   	}
	
}
