package com.jit.automation.ios.driver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import com.jit.automation.ios.utils.PropertyUtil;
import cucumber.api.Scenario;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import io.appium.java_client.remote.HideKeyboardStrategy;

public class AppiumHelper extends PropertyUtil 
{
	final static Logger LOGGER = Logger.getLogger(AppiumHelper.class);
	public static Scenario CURRENT_SCENARIO;
	public static boolean EXECUTE_TEST = true;
	private static IOSDriver<IOSElement> driver;
	private static File app;
	private static DesiredCapabilities CAPABILITIES;
	public static IOSDriver<IOSElement> getDriver()
	{
		if(driver!=null)
		{
			return driver;
		}
		else
		{
			try 
			{
				loadDriver();
			} 
			catch (IOException e)
			{
				LOGGER.info("Unable to load driver:"+e.fillInStackTrace());
			}
			return driver;
		}
	}
	public static void loadDriver() throws IOException
	{	
		setCapabilities();
		driver = new IOSDriver<IOSElement>(new URL(getAppiumServerUrl()), CAPABILITIES);
	}
	private static void setCapabilities() 
	{
		try
		{
			CAPABILITIES = new DesiredCapabilities();
			CAPABILITIES.setCapability("automationName", "XCUITest");
			CAPABILITIES.setCapability("platformName", "iOS");
			if(System.getProperty("udid").equalsIgnoreCase("simulator"))
			{
				app= new File(System.getProperty("user.dir") + "/src/test/resources/app"+getPropertyValue("APP_PATH_SIMULATOR"));
				CAPABILITIES.setCapability("app", app.getAbsolutePath());	
				CAPABILITIES.setCapability("wdaLocalPort", AppiumServerManager.getPort());
			}
			else if(getRunEnvironment().equalsIgnoreCase("real"))
			{
				app= new File(System.getProperty("user.dir") + "/src/test/resources/app"+getPropertyValue("APP_PATH_REAL_DEVICE"));
				CAPABILITIES.setCapability("app", app.getAbsolutePath());
				CAPABILITIES.setCapability("xcodeOrgId",PropertyUtil.getPropertyValue("xcodeOrgId"));
			}
			
			Properties capProp=readCapabilitiesProperties();
			LOGGER.info("Loading capabilities from property file....");
			System.setProperty("testRun",new java.util.Date().toString().replace(" ", "_"));
			String value=null;
			   for(Entry<Object, Object> e : capProp.entrySet()) 
			   {
				   value=(String) e.getValue();
				   if(value.contains("{"))
				   {
					   //replace value with system property value passed from maven cmd line
					   value=value.replace('{',' ');
					   value=value.replace('}',' ');
					   value=System.getProperty(value.trim());
				   }
				   CAPABILITIES.setCapability((String) e.getKey(), value);
			   }
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while setting capabilities:"+e.fillInStackTrace());
		}	
	}
	
	public static void saveScreenShot(String fileNameSufix, Scenario scenario)
	{	
	    try
	    {
			if (getDriver() instanceof TakesScreenshot) 
		        {
		            TakesScreenshot camera = (TakesScreenshot) getDriver();
		            byte[] screenshot = camera.getScreenshotAs(OutputType.BYTES);
		            scenario.embed(screenshot, "image/png");
		        }
	    }
	    catch(Exception e)
	    {
	    	LOGGER.error("Unable to capture screenshot:"+e.fillInStackTrace());
	    }
	}
	public static void closeDriver()
	{
		try
		{
			if(driver==null)
			{
				LOGGER.info("Driver is already closed");
			}
			else
			{
				try 
				{ 
					saveScreenShot("", CURRENT_SCENARIO);
					/*LogEntries syslog = driver.manage().logs().get("syslog");
					LOGGER.info("\n\nAppium syslog:\n\n");
					 for (LogEntry logEntry : syslog.getAll()) {
						 LOGGER.info(logEntry.toString() + "\n");
					 }
					 */
					LogEntries crashlog = driver.manage().logs().get("crashlog");
					LOGGER.info("\n\nAppium crashlog:\n\n");
					for (LogEntry logEntry : crashlog.getAll()) {
						 LOGGER.info(logEntry.toString() + "\n");
					 }
				} 
				catch(Exception e1)
				{
					LOGGER.error("Error occured while capturing logs  :"+e1.fillInStackTrace());
				}
				finally
				{
					driver.closeApp();
					driver.quit();
					AppiumServerManager.stopServer();
					LOGGER.info("Driver closed successfully.");
					driver=null;
				}	
			}
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while closing driver :"+e.fillInStackTrace());
		}
	}
	public static boolean checkIfExists(By elementBy)
	{
		if(getDriver()!=null)
		{
			try
			{
				//List<IOSElement> elementsFound = getDriver().findElements(elementBy);
				IOSElement elementsFound = getDriver().findElement(elementBy);
				if (elementsFound != null)
				{
					LOGGER.info("Expected element found on screen:"+elementBy.toString());
					return true;
					//Commenting below part as some elements doest not have displayed or enabled attribute
					/*for(IOSElement el:elementsFound)
					{
						if(el.isDisplayed() || el.isEnabled())
						{
							if(el.getText()!=null && ! (el.getText().isEmpty()))
							{
								LOGGER.info("IOS element found on screen having text:"+el.getText());
							}	
						}
					}
					if(elementsFound.size()>0)
					{
						LOGGER.info("Expected element found on screen:"+elementsFound.toString());
						LOGGER.info("Number of elements exists:"+elementsFound.size());
						return true;
					}
					else
					{
						LOGGER.warn("Element exists but its attributes not available! : "+elementBy.toString());
					}
					*/
				}
				else
				{
					LOGGER.error("Element not found on screen:" + elementBy.toString());
					return false;
				}
			}
			catch (Exception e) 
			{
				LOGGER.error("Element not found on screen:" + elementBy.toString());
				e.printStackTrace();
				return false;
			}	
		}
		else
		{
			LOGGER.error("Driver is not initialized or its closed now.");
			throw new WebDriverException("Driver is not initialized or its closed now.");
		}
	}

	public static void tapHere(By by)
	{
		IOSElement element=null;
		element = waitUntilObjectIsThere(by); 
		try
		{
			String elementText=(element.getText())== null?element.toString():element.getText();
			//element.tap(1, 1);
			LOGGER.info("Tap action performed on:"+elementText);
			/*
			Method 1:
			int centerx = element.getLocation().getX() + (element.getSize().getWidth() / 2)
			int centery = element.getLocation().getY() + (element.getSize().getHeight() / 2);
			TouchAction touchAction=new TouchAction(getDriver());
			touchAction.tap(centerx, centery).perform(); 
		   	Method 2:
		   	*/
		    TouchAction simpleClick = new TouchAction(driver).tap(element);
		    simpleClick.perform();
			
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while performing tap operation on:"+by.toString());
			LOGGER.error("Exception details:"+e.fillInStackTrace());
			throw new NoSuchElementException("Unable to perform tap action as element not present on screen:"+by.toString());
		}
	}
	public static void longPressHere(IOSElement element)
	{
		try
		{
			String elementText=(element.getText())== null?element.toString():element.getText();
		   // Duration d=Duration.ofSeconds(2);
			TouchAction simpleClick = new TouchAction(driver).longPress(element);
		    simpleClick.perform();
		    LOGGER.info("Performed long press action:"+elementText);
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while performing long press operation on:"+element.toString());
			LOGGER.error("Exception details:"+e.fillInStackTrace());
			throw new NoSuchElementException("Unable to perform long press action as element not present on screen:"+element.toString());
		}
	}
	public static void longPressHere(IOSElement element, int x, int y)
	{
		try
		{
			String elementText=(element.getText())== null?element.toString():element.getText();
			//Duration d=Duration.ofSeconds(2);
			TouchAction simpleClick = new TouchAction(driver).longPress(element, x, y);
		    simpleClick.perform();
		    LOGGER.info("Peformed long press action:"+elementText);
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while performing long press operation on:"+element.toString());
			LOGGER.error("Exception details:"+e.fillInStackTrace());
			throw new NoSuchElementException("Unable to perform long press action as element not present on screen:"+element.toString());
		}
	}
	public static IOSElement waitUntilObjectIsThere(By elementBy)
	 {
		IOSElement element = null;  
	    if(getDriver()!=null)
	     { 
	   		for(int i=0;i<=3;i++)
	   		{
	   			 try
	    	     {
	    			  element=getDriver().findElement(elementBy);
	    			  if(element!=null)
	    			  {
	    				  return element;
	    			  }
	    			  else
	    			  {
	    				  sleep(5000);
	    			  }
	    		  }
	    		  catch(Exception e)
	    		  {
	    			  LOGGER.warn("Element not found in attempt:"+(i+1)+", Retrying for:"+elementBy.toString());
	    			  sleep(5000);
	    		  }
	        }
	     }
	    else
	    {
	      	LOGGER.error("Driver is not initialized or its closed now.");
	        throw new WebDriverException("Driver is not initialized or its closed now.");
	    }
	    		 
	 return element;
	}
	public static void enterValue(By by, String value)
	{
		IOSElement element = waitUntilObjectIsThere(by);
		try
		{			
			if(value.equalsIgnoreCase("enter") || value.equalsIgnoreCase("return"))
			{
				element.sendKeys(Keys.ENTER);
				LOGGER.info("Enter/Return key pressed on element:"+(element.getText()== null?element.toString():element.getText()));
			}
			else
			{
				try
				{	
					element.clear(); 
				}
				catch(Exception e)
				{
					LOGGER.warn("Unable to clear text:"+e.getMessage());
				}
				element.setValue(value);
				LOGGER.info("Value entered successfully for:"+(element.getText()== null?element.toString():element.getText() + ", as :"+value));
			}
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while performing SetValue operation on:"+by.toString());
			LOGGER.error("Exception details:"+e.fillInStackTrace());
			throw new NoSuchElementException("Unable to perform SetValue action as element not present on screen:"+by.toString());
		}
	}
	public static void clearText(By by) 
	{
		try
		{
			IOSElement myelement=getIosElementFromBy(by);
			longPressHere(myelement);
			sleep(2000);
			getDriver().findElementByAccessibilityId("Select All").click();
			sleep(1000);
			getDriver().findElementByAccessibilityId("Cut").click();
			LOGGER.info("Text Cleared successfully using Select All and Cut!");
		}
		catch(Exception e)
		{
			LOGGER.warn("Unable to clear text using Select All and Clear");
		}
		
	}
	public static String acceptAlert() 
	{
		try
		{
			String alertText=getDriver().switchTo().alert().getText();
			getDriver().switchTo().alert().accept();
			LOGGER.info("Alert accepted:"+alertText);
			return alertText;
		}
		catch(Exception e)
		{
			LOGGER.warn("Expected alert but not found!");
			return null;
		}
	}
	public static String dismissAlert() 
	{
		String alertText = null;
		try
		{
			alertText=getDriver().switchTo().alert().getText();
			getDriver().switchTo().alert().dismiss();
			LOGGER.info("Alert dissmissed:"+alertText);
		}
		catch(Exception e)
		{
			LOGGER.warn("Expected alert but not found!");
		}
		return alertText;
	}
	public static boolean startAppiumServer() 
	{		
		if(!getRunEnvironment().equalsIgnoreCase("cloud"))
		{
			try
			{
				AppiumServerManager.startServer();
			}
			catch(Exception e)
			{
				return false;
			}
			try 
			{
				URL status = new URL(getAppiumServerUrl() + "/sessions");
	         	new UrlChecker().waitUntilAvailable(60, TimeUnit.SECONDS, status);
	         	LOGGER.info("Appium server started successfully at :"+getAppiumServerUrl());
	         	return true;
	        } 
			catch (Exception e) 
			{
	        	 LOGGER.error("Appium server not started within expected time!");
	        	 return false;
	        }
		}
		else
		{
			return true;
		}
	}
	
	public static IOSElement[] getChildElementsFromParent(By ParentElement,By ChildElement)
	{
		IOSElement myElement= getDriver().findElement(ParentElement);
		List<MobileElement> myElements = myElement.findElements(ChildElement);
		LOGGER.info("Number of child elements found:"+myElements.size());
		IOSElement myArrayElements[]= new IOSElement[myElements.size()];
		myArrayElements=myElements.toArray(myArrayElements);
		return myArrayElements;
	}
	public static boolean compareElementValue(By elementBy, String expectedValue, String operator)
	{
		boolean result=false;
		double expectedNumericValue=0;
		double actualNumericValue=0;	
		String actualStringValue=null;
		if(!(operator.equalsIgnoreCase("contains")||operator.equalsIgnoreCase("equals")))
		{
			//not a string comparison, so convert expectedValue string to double 
			try
			{
				expectedNumericValue=Double.parseDouble(expectedValue);	
			}
			catch(Exception e)
			{
				expectedNumericValue=0;
				LOGGER.error("Exception occured while converting string to double, for:"+expectedValue);
				LOGGER.error("Exception details:"+e.getMessage());
			}
		}
		if(getDriver()!=null)
		{
			try
			{
				List<IOSElement> elementsFound = getDriver().findElements(elementBy);
				if (elementsFound != null)
				{
					LOGGER.info("IOS element found on screen:"+elementBy.toString());
					LOGGER.info("Number of elements found on screen:"+elementsFound.size());
					switch(operator)
					{
						case "contains" :
											for(IOSElement el:elementsFound)
											{
												if(el.isDisplayed())
												{
													actualStringValue=el.getText();
													if(actualStringValue.toLowerCase().contains(expectedValue.toLowerCase()) || expectedValue.toLowerCase().contains(actualStringValue.toLowerCase()))
													{
														LOGGER.info("Expected text:"+ expectedValue +" is found in the element having text:"+actualStringValue);
														result=true;
													}
													else
													{
														LOGGER.error("Expected text:"+ expectedValue +" not found in the actual element containing text:"+actualStringValue);
														result=false;
														break;
													}
												}
												else
												{
													break;
												}
												
											}
											break;
											
						case ">="      :
											for(IOSElement el:elementsFound)
											{
												if(el.isDisplayed())
												{
													actualStringValue=el.getText();
													actualNumericValue=getNumberFromString(actualStringValue);
													if(actualNumericValue >=expectedNumericValue )
													{
														LOGGER.info("Expected value:"+ expectedNumericValue +" is less than or equals to the actual value found on screen:"+actualStringValue);
														result=true;
													}
													else
													{
														LOGGER.info("Expected value:"+ expectedNumericValue +" is not less than or equals to the actual value found on screen:"+actualStringValue);				result=false;
														break;
													}
												}
												else
												{
													break;
												}
												
											}
											break;
						
						case "<="      :
											
											for(IOSElement el:elementsFound)
											{
												if(el.isDisplayed())
												{
													actualStringValue=el.getText();
													actualNumericValue=getNumberFromString(actualStringValue);	
													if(actualNumericValue <=expectedNumericValue )
													{
														LOGGER.info("Expected value:"+ expectedNumericValue +" is greater than or equals to the actual value found on screen:"+actualStringValue);
														result=true;
													}
													else
													{
														LOGGER.info("Expected value:"+ expectedNumericValue +" is not greater than or equals to the actual value found on screen::"+actualStringValue);				
														result=false;
														break;
													}
												}
												else
												{
													result=true;
													break;
												}
												
											}
											break;
						case "equals"  : 
											for(IOSElement el:elementsFound)
											{
												if(el.isDisplayed())
												{
													actualStringValue=el.getText();	
													if(actualStringValue.equalsIgnoreCase(expectedValue) )
													{
														LOGGER.info("Expected value:"+ expectedValue +" is same as actual value found on screen:"+actualStringValue);
														result=true;
													}
													else
													{
														LOGGER.info("Expected value:"+ expectedValue +" is not same as the actual value found on screen:"+actualStringValue);				
														result=false;
														break;
													}
												}
												else
												{
													break;
												}
												
											}
											break;
						case "=" 	  :
											for(IOSElement el:elementsFound)
											{
												if(el.isDisplayed())
												{
													actualStringValue=el.getText();	
													actualNumericValue=getNumberFromString(actualStringValue);
													if(actualNumericValue==expectedNumericValue)
													{
														LOGGER.info("Expected value:"+ expectedNumericValue +" is same as actual value found on screen:"+actualStringValue);
														result=true;
													}
													else
													{
														LOGGER.info("Expected value:"+ expectedNumericValue +" is not same as the actual value found on screen:"+actualStringValue);				
														result=false;
														break;
													}
												}
												else
												{
													break;
												}
												
											}
											break;
											
											
					}
					if(result)
					{
						return true;
					}
				}
			}
			catch (Exception e) 
			{
				LOGGER.error("Element not found on screen:" + elementBy.toString());
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			LOGGER.error("Driver is not initialized or its closed now.");
			throw new WebDriverException("Driver is not initialized or its closed now.");
			
		}
		return false;
	
	}

	private static double getNumberFromString(String actualStringValue) 
	{
		String number = actualStringValue.replaceAll( "[^\\d]", "" );
	    double numerticValue=0.0;
		try
		{
			numerticValue=Double.parseDouble(number);	
		}
		catch(Exception e)
		{
			numerticValue=0;
			LOGGER.error("Exception occured while converting string to double, for:"+number);
			LOGGER.error("Exception details:"+e.getMessage());
		}
		return numerticValue;
	}
	public static void hideKeyboard()
	{
		try
		{
			getDriver().hideKeyboard(HideKeyboardStrategy.PRESS_KEY, "Done");
			LOGGER.info("Keyboard is now hidden...");
		}
		catch(Exception e)
		{
			try
			{
				getDriver().hideKeyboard(HideKeyboardStrategy.TAP_OUTSIDE);
				LOGGER.info("Keyboard is now hidden...");
			}
			catch(Exception e1)
			{
				LOGGER.error("Unable to hide keyboard..."+e1.fillInStackTrace());
			}
		}
	}
	public static void scrollToElement(By by, String direction)
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
		HashMap<String, String> scrollObject = new HashMap<String, String>();
		scrollObject.put("direction", direction);
		RemoteWebElement element=getDriver().findElement(by);
		scrollObject.put("element", ((RemoteWebElement) element).getId());
		js.executeScript("mobile: scroll", scrollObject);
		LOGGER.info("Scroll operation performed successfully for element:"+element.toString());
	}
	public static void scrollToElement(IOSElement element, String direction)
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
		HashMap<String, String> scrollObject = new HashMap<String, String>();
		scrollObject.put("direction", direction);
		scrollObject.put("element", ((RemoteWebElement) element).getId());
		js.executeScript("mobile: scroll", scrollObject);
		LOGGER.info("Scroll operation performed successfully for element:"+element.toString());
	}
	
	public static void scrollDown()
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
        Map scrollObject = new HashMap();
        scrollObject.put("direction", "down");
       // for(int i =0; i<2; i++)
        {	          
	       js.executeScript("mobile: scroll", scrollObject);
        }
        LOGGER.info("Scroll has been performed till end!");
	}
	public static void scrollTillUp()
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
        Map scrollObject = new HashMap();
        scrollObject.put("direction", "up");
       // for(int i =0; i<2; i++)
		{	          
	       js.executeScript("mobile: scroll", scrollObject);
        }	
        LOGGER.info("Scroll has been performed from bottom to top!");
	}
	public static void swipeDown()
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
	    Map scrollObject = new HashMap();
	    scrollObject.put("direction", "down");
	    js.executeScript("mobile: swipe", scrollObject);
	    LOGGER.info("Swipe has been performed to up!");
    }
	public static void swipeUp()
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
	    Map scrollObject = new HashMap();
	    scrollObject.put("direction", "up");
	    js.executeScript("mobile: swipe", scrollObject);
	    LOGGER.info("Swipe has been performed to down!"); 
    }
	public static void swipeToElement(IOSElement el)
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
	    HashMap<String, String> swipeObject = new HashMap<String, String>();
	    swipeObject.put("direction", "up");
	    swipeObject.put("element", el.getId());
	    js.executeScript("mobile: swipe", swipeObject);
	}
	 
	public static IOSElement getIosElementFromBy(By by)
	{
		if(by!=null)
		{
			IOSElement element = null;  
		    if(getDriver()!=null)
		     { 
		   		for(int i=0;i<=3;i++)
		   		{
		   			 try
		    	     {
		    			  element=getDriver().findElement(by);
		    			  if(element!=null)
		    			  {
		    				  return element;
		    			  }
		    			  else
		    			  {
		    				  sleep(5000);
		    			  }
		    		  }
		    		  catch(Exception e)
		    		  {
		    			  LOGGER.warn("Element not found in attempt:"+(i+1)+", Retrying for:"+by.toString());
		    			  sleep(5000);
		    		  }
		        }
		     }
		}
		return null;
		
	}
	public static Point getElementLocation(IOSElement element)
	{
		return element.getLocation();
	}
	public static Dimension getElementSize(IOSElement element)
	{
		return element.getSize();
	}
	public static void selectFromWheel(String value)
	{
		if(driver!=null)
		{
			try
			{
				IOSElement wheel=driver.findElementByClassName("XCUIElementTypePickerWheel");
				wheel.sendKeys(value);
				driver.findElementByAccessibilityId("Done").click();
				LOGGER.info("Value selected successfully from rotating wheel:"+value);
			}
			catch(Exception e)
			{
				LOGGER.warn("Unable to Select value from rotating wheel:"+e.fillInStackTrace());
			}
			
		}
		else
		{
			throw new AssertionError("Driver is null!");
		}
	}
	public static void setValueOnSlider(By sliderBy, String value)
	{
		try
		{
			IOSElement slider=getIosElementFromBy(sliderBy);
			slider.sendKeys(value);
			LOGGER.info("Slider set successfully!");
		}
		catch(Exception e)			
		{
			LOGGER.error("Unable to set value on slider");
			throw new NoSuchElementException("Unable to set value on slider");
		}
	}
	public static void setRangeSliderValues(By startElementBy,By endElementBy,int minValue,int maxValue)
	{
		if(driver!=null)
		{
			try
			{
				int minX,minY;
				int maxX,maxY;
				double interval;
				double length;
				long startValue;
				long endValue;
				int newMinX,newMaxX;
				
				IOSElement startElement=getIosElementFromBy(startElementBy);
				
				minX=startElement.getLocation().getX();
				minY=startElement.getLocation().getY();
			
				LOGGER.info("Range Slider co-ordinate: min-x="+minX);
				startValue=Long.parseLong(startElement.getAttribute("value").replaceAll( "[^\\d]", ""));
				LOGGER.info("Range Slider default min value:"+startValue);
				
				IOSElement endElement=getIosElementFromBy(endElementBy);
				
				maxX=endElement.getLocation().getX();
				maxY=endElement.getLocation().getY();
				LOGGER.info("Range Slider co-ordinate: max-x="+maxX);
				endValue=Long.parseLong(endElement.getAttribute("value").replaceAll( "[^\\d]", ""));
				LOGGER.info("Range Slider default max value:"+endValue);
				
				length=(maxX-1)-(minX+1);
				interval=(length)/(double)((endValue-1)-(startValue+1));
				LOGGER.info("Range Slider length and interval is:"+length+" and "+interval);
				
				newMinX=(int) Math.ceil(interval*(minValue-startValue));
				newMinX=(int) (newMinX+(startElement.getSize().getWidth())*1.025);
				
				newMaxX=(int) Math.ceil(interval*(endValue-maxValue));
				newMaxX=(int) (newMaxX-(endElement.getSize().getWidth())*1.025);
				
				LOGGER.info("Range Slider co-ordinate: new-min-x="+newMinX);
				LOGGER.info("Range Slider co-ordinate: new-max-x="+newMaxX);
				
				TouchAction action=new TouchAction(driver);
				boolean flag = true;
				try
				{
					action.press(minX,minY).moveTo(newMinX,0).release().perform();
					LOGGER.info("Slider Min value set successfully!");
				}
				catch(Exception e2)	
				{
					LOGGER.error("Issue with Slider for setting min value!"+e2.fillInStackTrace());
					flag = false;
				}
				TouchAction action2=new TouchAction(driver);
				try
				{
					action2.press(maxX,maxY).moveTo(-newMaxX,0).release().perform();
					LOGGER.info("Slider Max value set successfully!");
				}
				catch(Exception e3)	
				{
					LOGGER.error("Issue with Slider for setting max value!"+e3.fillInStackTrace());
					flag = false;
				}
				if(!flag)
				{
					throw new NoSuchElementException("Unable to perform set value action as element not present on screen!");
				}
			}
			catch(Exception e)
			{
				LOGGER.warn("Unable to set values on range selector:"+e.fillInStackTrace());
				throw new NoSuchElementException("Unable to perform set value action as element not present on screen!");
			}
		}
		else	
		{
			throw new AssertionError("Driver is null!");
		}
	}
	public static String getElementText(By by)
	{
		IOSElement element=getIosElementFromBy(by);
		String text=null;
		if(element!=null)
		{
			text=element.getText();
			LOGGER.info("Element text is:"+text);
		}
		else
		{
			LOGGER.info("Element is found null for capturing text:"+by.toString());
		}
		return text;
	}
	public static String getClipboardData()
	{
		Object content;
		sleep(5000);
		LOGGER.info("Trying to get simulator content...");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		try
		{
			Map<String, Object> args = new HashMap<>();
			args.put("encoding","UTF_8");
			content = js.executeScript("mobile: getPasteboard");
			//content= execCmd("xcrun simctl pbpaste booted");
		    LOGGER.info("Simulator clipboard content:"+content);
		    return content.toString();
		}
		catch(Exception e)
		{
			LOGGER.warn("Unable to get clipboard content:"+e.fillInStackTrace());
		}
		return "";
		
	}
	public static String waitForAlertAndAccept() 
	{
		
		String alertText=null;
		
		for(int i=0;i<=4;i++)
		{
			alertText = AppiumHelper.acceptAlert();
			if(alertText==null)
			{
				sleep(5000);
			}
			else
			{
				return alertText;
			}
		}
		return alertText;
	}
	public static void sleep(long time) 
	{
		try 
		{
			Thread.sleep(time);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	public static String waitForAlertAndDissmiss() 
	{
		String alertText=null;
		for(int i=0;i<=4;i++)
		{
			alertText = AppiumHelper.dismissAlert();
			if(alertText==null)
			{
				sleep(5000);
			}
			else
			{
				return alertText;
			}
		}
		return alertText;
	}
	public static IOSElement getVisiblElementFromElements(By elementBy)
	{
		if(driver!=null)
		{
			List<IOSElement> elements = driver.findElements(elementBy);
			for(IOSElement e:elements)
			{
				if(e.isDisplayed())
				{
					return e;
				}
			}
		}
		return null;
	}
	
	public static void clickHere(By by)
	{
		IOSElement element = waitUntilObjectIsThere(by); 
		try
		{
			String elementText=(element.getText())== null?element.toString():element.getText();
			element.click();
			LOGGER.info("Click action performed on:"+elementText);
			
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while performing Click operation on:"+by.toString());
			LOGGER.error("Exception details:"+e.fillInStackTrace());
			throw new NoSuchElementException("Unable to perform Click action as element not present on screen:"+by.toString());
		}
	}
	public static void clickHere(IOSElement element)
	{
		try
		{
			String elementText=(element.getText())== null?element.toString():element.getText();
			element.click();
			LOGGER.info("Click action performed on:"+elementText);
			
		}
		catch(Exception e)
		{
			LOGGER.error("Error occured while performing Click operation on:"+element.toString());
			LOGGER.error("Exception details:"+e.fillInStackTrace());
			throw new NoSuchElementException("Unable to perform Click action as element not present on screen:"+element.toString());
		}
	}
	public static boolean checkIfVisible(By by)
	{
		try
		{
			IOSElement element=getDriver().findElement(by);
			if(element!=null)
			{
				if(element.isDisplayed())
				{
					LOGGER.info("Element is visible:"+by.toString());
					return true;
				}
			}
			return false;
		}
		catch(Exception e)
		{
			LOGGER.info("Element is not visible:"+by.toString());
			return false;
		}
	}
	public static String getIosDeviceSize(String device)
	{
		final String THREE_POINT_FIVE_INCH="3.5";
		final String FOUR_INCH="4";
		final String FOUR_POINT_SEVEN_INCH="4.7";
		final String FIVE_POINT_FIVE_INCH="5.5";
		
		HashMap<String,String> deviceSizes=new HashMap<String,String>();
		
		deviceSizes.put("iphone 4",THREE_POINT_FIVE_INCH);
		deviceSizes.put("iphone 4s",THREE_POINT_FIVE_INCH);
		
		deviceSizes.put("iphone 5",FOUR_INCH);
		deviceSizes.put("iphone 5s",FOUR_INCH);
		deviceSizes.put("iphone 5c",FOUR_INCH);
		deviceSizes.put("iphone SE",FOUR_INCH);
		
		deviceSizes.put("iphone 6",FOUR_POINT_SEVEN_INCH);
		deviceSizes.put("iphone 6s",FOUR_POINT_SEVEN_INCH);
		deviceSizes.put("iphone 7",FOUR_POINT_SEVEN_INCH);
		deviceSizes.put("iphone 8",FOUR_POINT_SEVEN_INCH);
		
		deviceSizes.put("iphone 6 Plus",FIVE_POINT_FIVE_INCH);
		deviceSizes.put("iphone 6s Plus",FIVE_POINT_FIVE_INCH);
		deviceSizes.put("iphone 7 Plus",FIVE_POINT_FIVE_INCH);
		deviceSizes.put("iphone 8 Plus",FIVE_POINT_FIVE_INCH);
		
		for (Entry<String, String> entry : deviceSizes.entrySet())
		{
		   if(entry.getKey().toLowerCase().contains(device.toLowerCase()))
		   {
			   return entry.getValue();
		   }
		}
		return FOUR_POINT_SEVEN_INCH;  //default or most common
	}
	
	public static String getValueByReplacingSubstitutes(String value) 
	{
		String temp=value;
		if(temp.contains("PICK_FROM_FILE_"))	
		{
			temp=temp.replaceAll("PICK_FROM_FILE_\\S+",(temp.indexOf(' ')>0 ? PropertyUtil.getPropertyValue(temp.substring(0,temp.indexOf(' '))) : PropertyUtil.getPropertyValue(value)));
		}
		if(temp.contains("UNIQUE"))	
		{
			temp=temp.replace("UNIQUE", UUID.randomUUID().toString().subSequence(1, 6));
		}
		return temp;
	}
	public static boolean checkIfDynamicElementExists(String DynamicElementValue)
	{
		setDynamicElementValue(DynamicElementValue);
		return checkIfDynamicElementExists();
	}
	public static boolean checkIfDynamicElementVisible(String DynamicElementValue)
	{
		setDynamicElementValue(DynamicElementValue);
		if(AppiumHelper.checkIfVisible(getDynamicLocator()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public static By getDynamicLocator()
	{
		return PropertyUtil.getLocator("DYNAMIC_ELEMENT");
	}
	public static By getDynamicLocator(String DynamicElementValue)
	{
		setDynamicElementValue(DynamicElementValue);
		return PropertyUtil.getLocator("DYNAMIC_ELEMENT");
	}
	public static void setDynamicElementValue(String DynamicElementValue)
	{
		PropertyUtil.DYNAMIC_ELEMENT_VALUE=DynamicElementValue;
	}
	public static boolean checkIfDynamicElementExists() 
	{
		if(AppiumHelper.checkIfExists(getDynamicLocator()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public static boolean checkIfElementExistsAfterScrollDown(By locator, int NUMBER_OF_TIMES_SCROLL_NEEDED)
	{
		for(int i=0;i<=NUMBER_OF_TIMES_SCROLL_NEEDED;i++)
		{
			if(checkIfExists(locator))
			{
				return true;
			}
			else
			{
				//item not visible, try to scroll
				swipeUp();
				//scrollTillEnd();
			}
		}
		return false;
	}
	public static boolean checkIfElementVisibleAfterScrollDown(By locator)
	{
		for(int i=0;i<=10;i++)
		{
			if(checkIfVisible(locator))
			{
				return true;
			}
			else
			{
				//item not visible, try to scroll
				swipeUp();
			}
		}
		return false;
	}
}

