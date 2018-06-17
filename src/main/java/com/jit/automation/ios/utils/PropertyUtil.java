package com.jit.automation.ios.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.io.InputStream;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByClassName;
import org.openqa.selenium.By.ByXPath;

import com.jit.automation.ios.driver.AppiumServerManager;

import io.appium.java_client.MobileBy.ByAccessibilityId;
import io.appium.java_client.MobileBy.ByIosClassChain;
import io.appium.java_client.MobileBy.ByIosNsPredicate;

public class PropertyUtil {
	public static Properties PROPERTIES=null;
	public static Properties CONFIG_PROPERTIES=null;
	public static String DYNAMIC_ELEMENT_VALUE="";
	private static final String APPIUM_URL = "appium.url";
	private static final String RUN_ENVIRONMENT="run.enviornment";
	
	public static String getAppiumServerAddress()
	{
		if(CONFIG_PROPERTIES==null)
		{
			CONFIG_PROPERTIES = readConfigProperties();
		}
		String url = (String) CONFIG_PROPERTIES.get(APPIUM_URL);
		return url;
	}
	public static String getRunEnvironment()
	{
		if(CONFIG_PROPERTIES==null)
		{
			CONFIG_PROPERTIES = readConfigProperties();
		}
		String profileName = (String) CONFIG_PROPERTIES.get(RUN_ENVIRONMENT);
		return profileName;
	}
	public static String getAppiumPort()
	{
		String port = Integer.toString(AppiumServerManager.PORT_NUMBER);
		return port;
	}
	public static String getAppiumServerUrl() throws IOException 
	{
		if(CONFIG_PROPERTIES==null)
		{
			CONFIG_PROPERTIES = readConfigProperties();
		}
		if(getRunEnvironment().equalsIgnoreCase("cloud"))
		{
			System.out.println("Appium Cloud Server URL:"+String.format("%s/wd/hub",getAppiumServerAddress()));
			return String.format("%s/wd/hub",getAppiumServerAddress());
		}
		System.out.println("Appium Server URL: " + String.format("%s:%s/wd/hub",getAppiumServerAddress(), getAppiumPort()));
		return String.format("%s:%s/wd/hub",getAppiumServerAddress(), getAppiumPort());
	}

	private static Properties readConfigProperties()
	{
		String propertiesFileName = "config.properties";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		try(InputStream resourceStream = loader.getResourceAsStream(propertiesFileName)) 
		{
			properties.load(resourceStream);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return properties;
	}

	protected static Properties readCapabilitiesProperties() throws IOException 
	{
		String propertiesFileName=null;
		switch(getRunEnvironment())
		{
			case "cloud" : 
							propertiesFileName="cloud-devices-capabilities.properties";
							break;
			case "real" : 
							propertiesFileName="real-devices-capabilities.properties";
							break;
			default:		
							propertiesFileName="simulated-devices-capabilities.properties";
							
		}
		Properties properties = new Properties();
		try 
	      {
	    	  FileInputStream Master = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/capabilities/"+propertiesFileName);
	    	  properties.load(Master);
	    	  Master.close();
	      }
	      catch (IOException e) 
	      {
	            System.out.println(e.getMessage());
	      }
		return properties;
	}
	public static void loadPropertyFile(String mapFile)
	  {
		  if(PROPERTIES==null)
		  {
			  PROPERTIES = new Properties();
		      try 
		      {
		    	  FileInputStream Master = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/UIMap/"+mapFile);
		    	  PROPERTIES.load(Master);
		    	  Master.close();
		      }
		      catch (IOException e) 
		      {
		            System.out.println(e.getMessage());
		      }
		  }
	  }

	   public static By getLocator(String ElementName)
	   {
		   String ElementNameValue;
		   String locatorType = null; 
		   String locatorValue;
		   try
	         {
			   	 ElementNameValue = PROPERTIES.getProperty(ElementName);
		         String[] values = ElementNameValue.split("_");
		         locatorType = values[0];
		         locatorValue = values[1];
		         if(locatorValue.contains("%s"))
		         {
		        	 locatorValue=String.format(locatorValue, DYNAMIC_ELEMENT_VALUE);
		         }
		         if (values.length > 2)
		         {
		        	 locatorValue = "";
		        	 locatorType = values[0];
			         for(int i = 1; i < values.length; i++)
			         {
			        	 locatorValue = locatorValue+values[i]+"_";
			         }
			         locatorValue=locatorValue.substring(0, locatorValue.length()-1);
		         }
	         }
	         catch(Exception e)
	         {
	        	 throw new NoSuchElementException("Locator type '" + locatorType + "' not defined in the prop file!!");
 
	         }
	      
			if(locatorType.toLowerCase().equals("id"))
	                 return By.id(locatorValue);
	           else if(locatorType.toLowerCase().equals("accessibilityid"))
	                 return ByAccessibilityId.AccessibilityId(locatorValue);
	           else if((locatorType.toLowerCase().equals("classchain")))
	                 return ByIosClassChain.iOSClassChain((locatorValue));
	           else if((locatorType.toLowerCase().equals("predicate")))
	                 return ByIosNsPredicate.iOSNsPredicateString(locatorValue);
	           else if(locatorType.toLowerCase().equals("name"))
	                 return By.name(locatorValue);
	           else if((locatorType.toLowerCase().equals("classname")) || (locatorType.toLowerCase().equals("class")))
	                 return ByClassName.className(locatorValue);
	           else if((locatorType.toLowerCase().equals("tagname")) || (locatorType.toLowerCase().equals("tag")))
	                 return By.className(locatorValue);
	           else if((locatorType.toLowerCase().equals("linktext")) || (locatorType.toLowerCase().equals("link")))
	                 return By.linkText(locatorValue);
	           else if(locatorType.toLowerCase().equals("partiallinktext"))
	                 return By.partialLinkText(locatorValue);
	           else if((locatorType.toLowerCase().equals("cssselector")) || (locatorType.toLowerCase().equals("css")))
	                 return By.cssSelector(locatorValue);
	           else if(locatorType.toLowerCase().equals("xpath"))
	                 return ByXPath.xpath(locatorValue);
	           else
	                   throw new NoSuchElementException("Locator type '" + locatorType + "' not defined in the prop file!!");
	         }
	   public static String getPropertyValue(String key)
	   {   
		   if(PROPERTIES.getProperty(key)==null)
		   {
			   throw new NoSuchFieldError("Property key not found in properties file:"+key);
		   }
		   return PROPERTIES.getProperty(key);
		   
	   }
}
