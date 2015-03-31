android-here-sdk-dist
=================

The PayPal Here SDK enables Android apps to interact with credit card swipers so that merchants can process in-person credit card transactions using a mobile app. The native libraries of the PayPal Here SDK enable you to:
* **Interact with PayPal Hardware** — Detect, connect to, and listen for events coming from PayPal Here audio jack-based card swipers.
* **Process Card-Present payments** — When you swipe a card through a PayPal Here swiper, card data is immediately encrypted. The encrypted package can be sent to PayPal alongside the transaction data for processing.

Developers should use the PayPal Here SDK to get world-class payment process with extremely simple integration.  Some of the main benefits include
* **Low, transparent pricing:** US Merchants pay just 2.7% per transaction (or 3.5% + $0.15 for keyed in transactions), including cards like American Express, with no additional hidden/monthly costs.
* **Safety & Security:** PayPal's solution uses encrypted swipers, such that card data is never made available to merchants or anyone else.
* **Live customer support:** Whenever you need support, we’re available to help with our customer support team.
[Visit our website](https://www.paypal.com/webapps/mpp/credit-card-reader) for more information about PayPal Here.


Full class and method documentation can be [found here](http://paypal-mobile.github.io/android-here-sdk-dist/).

As an alternative to the SDK, a developer can also use a URI framework that lets one app (or mobile webpage) link directly to the PayPal Here app to complete a payment.  Using this method, the merchant will tap a button or link in one app, which will open the pre-installed PayPal Here app on their device, with the PayPal Here app pre-populating the original order information, collect a payment (card swipe) in the PayPal Here app, and return the merchant to the original app/webpage. This is available for US, UK, Austalia, and Japan for iOS & Android.  See the [Sideloader API](https://github.com/paypal/here-sideloader-api-samples) on Github.


Prerequisites For Using The SDK
===============================

In order to start using the PayPal Here SDK, you need the following:

1. A developer-enabled PayPal account ([sign up here](https://developer.paypal.com/webapps/developer/applications/myapps)).  This is the account you use to register your app.  You will receive an App ID & Secret to use with the SDK.
2. A PayPal Here business account ([sign up here] (https://www.paypal.com/us/webapps/mobilemerchant/page/mpa/ob/geturl?onbver=2.0&amp;country.x=US&productIntentID=mobile_payment_acceptance&referringpage=ios_sdk_github&hs=login)).  This is the account that the end merchant uses, and will be the destination account of funds received. A single app can be associated with/used by one or many merchant accounts – including the developer-enabled account.  You will receive an Access Token and Refresh URL for each merchant that grants permission to your app. (*See our [Onboarding guide](/docs/Merchant%20Onboarding.pdf) for suggestions on how to help your merchants sign up for PayPal business accounts*)
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



Interacting With The Card Reader
================================
Card reader interaction is established by calling:
```java
PayPalHereSDK.getCardReaderManager().beginMonitoring(
 CardReaderListener.ReaderConnectionTypes.Bluetooth.
 CardReaderListener.ReaderConnectionTypes.AudioJack);
```
which will monitor for all card reader types.

Once you've begun monitoring, the SDK will start firing notification center events for relevant card events.
However, we recommend you do not monitor the notification center directly, but instead use our class that
will translate untyped notification center calls to typed delegate calls. You do this by simply storing an
instance of PPHCardReaderWatcher in your class and implementing the PPHCardReaderDelegate protocol:
```java
self.readerWatcher =
 [[alloc] initWithDelegate: self];
```

<!--- Should mirror iOS with description of basic events
The events are very simple:

BadSwipe: When the card reader/SDK could not read the swiped or the inserted card.
CardBlocked: When the card used by the user has been blocked.


```objectivec
-(void)didStartReaderDetection: (PPHReaderType) readerType; //Indicates a reader (or something else) was inserted into the headphone jack
-(void)didDetectReaderDevice: (PPHCardReaderBasicInformation*) reader; //Indicates that a PayPal reader was detected
-(void)didReceiveCardReaderMetadata: (PPHCardReaderMetadata*) metadata; //Includes additional data about the PayPal reader, like reader type and serial number
-(void)didRemoveReader: (PPHReaderType) readerType; //Indicates the reader was removed

-(void)didDetectCardSwipeAttempt; //Indicates that something (e.g. a card, a piece of paper) was swiped through the reader
-(void)didCompleteCardSwipe:(PPHCardSwipeData*)card; //Indicates a successful read of the card, with data
-(void)didFailToReadCard; //Indicates a failed read (e.g. this wasn't a credit card)
```

The first four relate to the insertion, removal and detection of the card reader, the other three are in the context of a transaction, which you must "begin" by telling the card reader manager you're ready to receive a swipe. Because some readers (namely audio jack readers) have batteries in them, you MUST be careful about when you activate the reader. In the PayPal Here app, for example, we activate the reader when there is a non-zero value in the "cart" or active order. If you have a view or step which expresses clear intent to take a charge, that's a good time to activate the reader. 
--->


Build & Complete a Transaction
===================
In order to process a payment, there needs to be an amount to charge.  PayPal creates Invoices to represent each transaction to be paid.  Invoices can be extremely simple (a simple amount), or complex with details on item names, taxes, tips, and/or discounts.  The basic order of operations:
* Start a new invoice
* Add item data to the invoice (optional)
* Begin a purchase event and collect card data
* Collect a signature for the transaction

**Start a new invoice**

The invoice is a TransactionManager object, and doesn't need to have been saved to the PayPal backend to begin watching for card swipes. It's automatically created within the SDK, but you can retrieve it to add one or more items, and set tax or other information:

```java
TransactionManager transactionMgr = PayPalHereSDK.getTransactionManager();
.
.
.
Invoice mInvoice = transactionMgr.beginPayment();
```

**Add item data**

You should add details about each item on the receipt if possible. To save an invoice, just call save and provide a completion handler. Typically you would show some progress UI while doing this, unless it's being done in the background:

```java
String mItem = "Self-deploying umbrella";
BigDecimal mPrice = BigDecimal("149.95");
InvoiceItem mInvoice = DomainFactory.newInvoiceItem(mItem, mPrice);

long quantityToAdd = . . .;
mInvoice.addItem(mItem, quantityToAdd);
```

And then, get the invoice ready for payment:
```java
transactionMgr.setInvoice(mInvoice);
```


**Begin a purchase event**

Next, call the TransactionManager object’s processPayment method. The form of this call depends on the type of card being used:

```java
transactionMgr.processPayment(PaymentType.CARD_READER, NULL, mResponseHandler);
```
 
The call to processPayment is asynchronous, and so uses a response handler. The response handler receives a TransactionManager.PaymentResponse object if the operation succeeds (indicating that payment has been made), or a PPError object if the operation fails. Do not go on to the next step until the operation has succeeded.

Finally, call the TransactionManager object’s finalizePayment method:

```java
transactionMgr.finalizePayment(PaymentType.CARD_READER, mResponseHandler);`
```

The call to finalizePayment() is also asynchronous. The response handler receives the same type of object as the processPayment response handler when the operation succeeds or fails.


**Add a signature**

After your app completes an invoice and receives a card-read notification, but before it pays the invoice, it can capture a signature image. (For most merchants and transaction amounts, the PayPal requires a signature with payment).

To pay an invoice with the result of a card swipe, you must first gather the signature image.  The PPHSignatureView can be placed in a view controller of your own design and it will provide an image which can be sent to the API.

Use the TransactionManager class’s *finalizePaymentForTransaction* method, which finalizes the payment. 


More Stuff to Look At
=====================
There is a lot more available in the PayPal Here SDK.  More detail is available in our [developer documentation](/docs/DeveloperGuide_Android.pdf) and [javadocs](/javadoc/index.html) to show other capabilities.  These include:
* **Auth/Capture:** Rather than a one-time sale, authorize a payment with a card swipe, and complete the transaction at a later time.  This is common when adding tips after the transaction is complete (e.g. at a restaurant).
* **Refunds:** Use the SDK to refund a transaction
* **Send Receipts:** You can use services through the SDK to send email or SMS receipts to customers
* **Key-in:** Most applications need to let users key in card numbers directly, in case the card's magstripe data can no longer be read.
* **CashierID:** Include your own unique user identifier to track a merchant's employee usage
* **Error Handling:** See more detail about the different types of errors that can be returned
