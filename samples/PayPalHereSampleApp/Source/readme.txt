Instructions for compiling the sample app

Prerequisites
============================================================================================================================================================================================================
1. Install Android Studio (0.4.3 +)
2. Install Maven(3.1.1)
3. Install Android SDK and set ANDROID_HOME environmental variable


IMPORTANT STEP TO BE FOLLOWED BEFORE COMPILING THE SAMPLE APP
============================================================================================================================================================================================================
Android Gradle build system doesn't allow referencing the .aar files locally by keeping them in libs folder. Instead they have to be referenced as remote maven artifacts. 
For that first we will install the PayPalHereSDK-X.X.aar file in local Maven repositories and reference it as local maven artifact.
Command for installing PayPalHereSDK-X.X.aar as local maven artifact
mvn install:install-file -Dfile=[location where the PayPalHereSDK.aar files exists ex. sdk/PayPalHereSDK-X.X.aar] -DgroupId=com.paypal.merchant.sdk -DartifactId=paypal-sdk -Dversion=[X.X] -Dpackaging=aar


STEPS TO IMPORT AND COMPILE THE PayPalHereSampleApp
============================================================================================================================================================================================================
1. Open the Android Studio and import the project (File -> Import Project)
2. Point that to build.gradle file present under PayPalHereSampleApp/Source/
3. This will import the project to Android Studio
4. Now you can hit the run button from the Android Studio toolbar to build and run the application.