Introduction
=================
The PayPal Here SDK enables Android apps to process in-person credit card transactions using an assortment of [card readers](https://www.paypal.com/webapps/mpp/credit-card-reader#A39) that are capable of accepting contactless, EMV, and swipe payment methods.

Developers should use the PayPal Here SDK to get world-class payment processing with one simple integration.  Some of the main benefits include
* **Low, transparent pricing:** US Merchants pay just 2.7% per transaction (or 3.5% + $0.15 for keyed in transactions), including cards like American Express, with no additional hidden/monthly costs.
* **Safety & Security:** PayPal's solution uses encrypted swipers, such that card data is never made available to merchants or anyone else.
* **Live customer support:** Whenever you need support, we’re available to help with our customer support team.
[Visit our website](https://www.paypal.com/webapps/mpp/credit-card-reader) for more information about PayPal Here.


Supporting Materials
========================
 *  PPH SDK documentation can be found [here](https://developer.paypal.com/docs/integration/paypal-here/).
 *  Sample App (Coming Soon): Please see and modify the sample app thats available in this repo to experiment and learn more about the SDK and it's capabilities.


Installation
==============
Our recommended installation method is to reference from Maven:
```
dependencies {
    repositories {
        mavenCentral()
    }

//    api('com.paypal.retail:here-sdk-release:2.0.0@aar'){transitive=true}
    api('com.paypal.retail:here-sdk-debug:2.0.0@aar'){transitive=true}
}
```
There's both a Debug and Release build available to be referenced. Please make sure that you switch to the Release build prior to pushing your app to the Play Store otherwise your submission will likely be rejected.

Also, if you see a runtime exception `java.lang.IllegalStateException: J2V8 native library not loaded`, please add the following code. Native 64 bit support will come with an upcoming release, but PayPal Here is using this workaround currently. More info on this can be found at [eclipsesource/J2V8#174](https://github.com/eclipsesource/J2V8/issues/174).
```
android {
    ....
    defaultConfig {
        ....
        ndk {
            abiFilters "armeabi", "armeabi-v7a", "x86", "mips"
        }
    }
}
```


Housekeeping Items
=====================
There are a few noteworthy items that should be called out. These include:
* **Auth/Capture:** Please note that you will see code relating to auth/capture within the sample app, but this feature is NOT yet available. Our auth/capture functionality will be coming soon and then we'll update the sample app accordingly. For the time being, the code is available in the sample app but it's commented out.
* **Key-in:** Even though there's not an example in the sample app, please know that the SDK will support this payment method should you need to implement it.
* **Server:** There will be some server-side work that needs to be done to handle the token management part of the integration. Standard Oauth2 is used for Merchant Onboarding and more information on this piece can be found [here](https://developer.paypal.com/docs/integration/paypal-here/merchant-onboarding/)
* **Marketing Toolkit:** Within this repo, you'll find downloadable marketing assets – from emails to banner ads – to help you quickly, and effectively, promote your app’s new payments functionality. 
