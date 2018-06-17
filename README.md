# appium-cucumber
This repository has framework for mobile automation (iOs and can be extended for android)  using appium, java and Cucumber

How to run?
Examples: 
1. Run below command for simulator and running on local machine with smoke tests:
clean install "-DConfiguration=locator-ios-ui.properties" "-DdeviceName=iPhone 6s" "-DplatformVersion=11.1" "-Dudid=simulator" -Pdev,smoke

2. Run below command for real device connected to local with sanity tests:
clean install "-DConfiguration=locator-ios-ui.properties" "-DdeviceName=iPhone 6s" "-DplatformVersion=11.1" "-Dudid=dfsdnfksdfksdknfksndfsdfsdf" -Preal,sanity

3. Run below command for cloud devices with sanity tests:
clean install "-DConfiguration=locator-ios-ui.properties" "-DdeviceName=iPhone 6s" "-DplatformVersion=11.1" "-Dudid=NoNeed" -Pcloud,sanity


Command line options:
-DConfiguration = This parameter value is used to load property file that has all locator information stored
-DdeviceName = This parameter value is used to specify device name (simulator or real/cloud device name)
-DplatformVersion = This parameter value is used to specify platform version of iOS
-Dudid= This parameter value is used for real device

Maven Profiles: 
-Pcloud /-Pdev / -Preal = This indicate where to test (on simualtor or real devices) so that respective capabilities can be loaded at run time.

-Psmoke/ -Psanity= This profile will help to reduce or to specify which cucumber scenarios to run or not to run.


