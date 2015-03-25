android-here-sdk-dist
=================

The PayPal Here SDK enables Android apps to interact with credit card swipers so that merchants can process in-person credit card transactions using a mobile app. The native libraries of the PayPal Here SDK enable you to:
* **Interact with PayPal Hardware** — Detect, connect to, and listen for events coming from PayPal Here audio jack-based card swipers.
* **Process Card-Present payments** — When you swipe a card through a PayPal Here swiper, card data is immediately encrypted. The encrypted package can be sent to PayPal alongside the transaction data for processing.

Full class and method documentation can be [found here](http://paypal-mobile.github.io/android-here-sdk-dist/).


Prerequisites For Using The SDK
===============================

In order to start using the PayPal Here SDK, you need the following:

1. A developer-enabled PayPal account ([sign up here](https://developer.paypal.com/webapps/developer/applications/myapps)).  This is the account you use to register your app.  You will receive an App ID & Secret to use with the SDK.
2. A PayPal Here business account ([sign up here] (https://www.paypal.com/us/webapps/mobilemerchant/page/mpa/ob/geturl?onbver=2.0&amp;country.x=US&productIntentID=mobile_payment_acceptance&referringpage=ios_sdk_github&hs=login)).  This is the account that the end merchant uses, and will be the destination account of funds received. A single app can be associated with/used by one or many merchant accounts – including the developer-enabled account.  You will receive an Access Token and Refresh URL for each merchant that grants permission to your app.
3. A PayPal Here swiper.  You can get one shipped to you when you create a business account in step (2), or via retailers like [Staples](http://www.staples.com/PayPal-Here-trade-Mobile-Card-Reader/product_1421621).
4. Android development tools (e.g. Android Studio)

The Sample App
==============
To make it easier to see and understand how to best use the capabilities of the SDK, we’ve designed a sample/reference application.  To make the app functional, there is some minimal UI code that can be ignored – the point is to show how to use the SDK API’s.

With the Sample App, you can view code that:
* Initializes the SDK
* Authenticates the merchant
* Updates the merchant location
* Creates & adds items to an invoice
* Takes a payment with the card reader
* Takes a keyed-in card transaction
* Add a signature to finalize a payment
* Send an email/SMS receipt 


Get Started
===========
The first thing you need to do is set up your app to start using the SDK.  
* Initialize the SDK (each time the app starts) 
* Authenticate the merchant and pass the merchant’s credentials (Access Token) to the SDK [(more on PayPal oAuth)](https://github.com/PayPal-Mobile/ios-here-sdk-dist/blob/master/docs/PayPal%20Access%20oAuth.md)
* Set the merchant’s location (any time the merchant’s location changes) 
* Start monitoring the card reader for events (for card present transactions)

You initialize the PayPal Here SDK by calling the class method PayPalHereSDK.init:
```java
Public class LoginScreenActivity extends Activity {
...
 PayPalHereSDK.init(getApplicationContext(), PayPalHereSDK.Sandbox);
 ```
If you want to start with test transactions (generally a good idea), you can specify the environment as PayPalHereSDK.Live or PayPalHereSDK.Sandbox.

With an authenticated merchant, use PayPalHereSDK.setCredentials() to set the merchant for which transactions will be executed.
```java
Credentials credentials = . . .; // The merchant's OAuth credentials.
Final DefaultResponseHandler = // A default response handler.
 new DefaultResponseHandler< Merchant, PPError<MerchantManager.MerchantErrors> >;
PayPalHereSDK.setCredentials(credentials, defaultResponseHandler);
 ```

Now, monitor the card reader for events like reader connections, removals, and swipes with the beginMonitoring method:
```java
PayPalHereSDK.getCardReaderManager().beginMonitoring(
 CardReaderListener.ReaderConnectionTypes.Bluetooth.
 CardReaderListener.ReaderConnectionTypes.AudioJack);
```



Please refer to http://paypal-mobile.github.io/android-here-sdk-dist/ for documentation and more information about android-here-sdk.
