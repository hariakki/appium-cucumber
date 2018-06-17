package com.jit.automation.ios.driver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import org.apache.log4j.Logger;
import com.jit.automation.ios.utils.PropertyUtil;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

public class AppiumServerManager 
{
	final static Logger LOGGER = Logger.getLogger(AppiumServerManager.class);
	private static AppiumDriverLocalService service;
	private static AppiumServiceBuilder builder;
	public static String SERVER_ADDRESS;
	public static int PORT_NUMBER;
	
	public static void startServer() 
	{
		//Build the Appium service
		SERVER_ADDRESS=PropertyUtil.getAppiumServerAddress().replace("http://", "");
		PORT_NUMBER=getPort(); //random port
		builder = new AppiumServiceBuilder();
		builder.withAppiumJS(new File("/usr/local/lib/node_modules/appium/build/lib/main.js"));
		builder.withIPAddress(SERVER_ADDRESS);
		builder.usingPort(PORT_NUMBER);
		builder.withArgument(GeneralServerFlag.SESSION_OVERRIDE);
		//builder.withArgument(GeneralServerFlag.LOG_LEVEL,"error");
		builder.withLogFile(new File("target/appium_server.log"));
		try
		{
			//Start the server with the builder
			LOGGER.info("Starting Appium server on address:"+SERVER_ADDRESS);
			service = AppiumDriverLocalService.buildService(builder);
			service.start();
		}
		catch(Exception e)
		{
			LOGGER.info("Unable to start appium server:"+e.fillInStackTrace());
			throw new RuntimeException("Unable to start appium server:"+e.fillInStackTrace());
		}
		
	}
	
	public static void stopServer() 
	{
		service.stop();
		LOGGER.info("Appium Server stopped successfully!");
	}
	
	public static int getPort() 
	{
        ServerSocket socket;
		try 
		{
			socket = new ServerSocket(0);
	        socket.setReuseAddress(true);
	        int port = socket.getLocalPort();
	        socket.close();
	        LOGGER.info("Random port number:"+port);
	        return port;
		}
	    catch (IOException e) 
		{
	    	LOGGER.error("Unable to get port number:"+e.fillInStackTrace());
	    	throw new RuntimeException("Unable to get port number.");
		}
    }
		
}
