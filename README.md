Introduction
=================

The Android PayPal Here SDK enables Android apps to process in-person credit card transactions using Contactless/EMV chip card readers or mag stripe swipers. PayPal Here SDK library enables you to:
* **Interact with PayPal Hardware** — Detect, connect to, and listen for card events coming from both PayPal provided audio jack based card swipers and Contactless/EMV Chip card readers
* **Process Card-Present payments** — To process the payment using the data coming from card readers (chip card reader or mag stripe card reader) which will be in the encrypted form. 

Developers should use the PayPal Here SDK to get world-class payment process with extremely simple integration.  Some of the main benefits include
* **Low, transparent pricing:** US Merchants pay just 2.7% per transaction (or 3.5% + $0.15 for keyed in transactions), including cards like American Express, with no additional hidden/monthly costs.
* **Safety & Security:** PayPal's solution uses encrypted swipers, such that card data is never made available to merchants or anyone else.
* **Live customer support:** Whenever you need support, we’re available to help with our customer support team.
[Visit our website](https://www.paypal.com/webapps/mpp/credit-card-reader) for more information about PayPal Here.

**Note:** At the moment we only support Android Studio/Mac.

Supporting Materials
===================


* Full class and method documentation can be [found here](http://paypal-mobile.github.io/android-here-sdk-dist/javadoc/index.html).
* The sample app demonstrates how to use PayPal Here SDK to perform the following functionality
  * How to take payment using mag stripe audio jack card reader
  * How to take payment using EMV chip card reader
  * How to take payment when both mag stripe and EMV chip card readers are connected at the same time
  * How to perform refund once the payment goes through
  * How to check & update the software on EMV chip card rader

Please feel free to modify and play with the sample app to learn more about the SDK and it's capabilities.

Project Configuration
==============

Please follow the steps in the [described here](http://paypal-mobile.github.io/android-here-sdk-dist/sample_apps.html) to properly set up your application for use with the PayPalHereSDK.

Authentication
===============================
First you need to complete the on-boarding process and get the access token to use PayPal Here SDK. Without the proper access token, PayPal Here SDK will not get initialized properly and hence first thing is to get the proper access token.

1. Set up a PayPal developer account ([sign up here](https://developer.paypal.com/developer/applications/)) and configure an application to be used with the PayPal Here SDK.  Refer to the [PayPal Here SDK integration Document](https://developer.paypal.com/docs/integration/mobile/pph-sdk-overview/) for information on how to properly configure your app.

2. Deploy and configure the [Retail SDK Authentication Server](https://github.com/djMax/paypal-retail-node) OR manually negotiate the [PayPal oAuth2 flow](https://developer.paypal.com/docs/integration/direct/paypal-oauth2/) to obtain the tokens required for login.

See our [Merchant Onboarding Guide](docs/Merchant%20Onboarding%20Guide.pdf) for suggestions on how to help your merchants sign up for PayPal business accounts and link them in your back-office software.

SDK Initialization
==================

* By default SDK is configured to use Live environment. In case if you wish to use sandbox environment (for using with _Swipe_ card readers only), please configure it while initializing the SDK and consult [sandbox overview](https://developer.paypal.com/docs/classic/lifecycle/sb_overview/) for more information about the PayPal sandbox environment.

```java
//For setting Live environment
PayPalHereSDK.init(appContext, PayPalHereSDK.Live);

//For setting Sandbox environment
PayPalHereSDK.init(appContext, PayPalHereSDK.Sandbox);

//Alternatively you can set Live or Sandbox environment after completing initialization as below

//For setting Live environment
PayPalHereSDK.setServerName(PayPalHereSDK.Live);

//For setting Sandbox environment
PayPalHereSDK.setServerName(PayPalHereSDK.Sandbox);
```

* Setup the SDK merchant with your credentials.

```java
// with credentials object
PayPalHereSDK.setCredentials(credentialsObj, new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {
            @Override
            public void onSuccess(Merchant merchant) {
                //PayPal Here SDK Succssfuly accepted and set with credentials
            }

            @Override
            public void onError(PPError<MerchantManager.MerchantErrors> merchantErrorsPPError) {
                //PayPal Here SDK failed to set with the credentials provided.
            }
        });

// Or by digesting the response from paypal-retail-node...
PayPalHereSDK.setCredentialsFromCompositeStrFromMidTierServer(compositeAccessTokenStr, new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {
            @Override
            public void onSuccess(Merchant merchant) {
                //PayPal Here SDK Succssfuly accepted and set with credentials
            }

            @Override
            public void onError(PPError<MerchantManager.MerchantErrors> merchantErrorsPPError) {
                //PayPal Here SDK failed to set with the credentials provided.
            }
        });
```

Creating Invoice and Beginning Payment
================================

Inorder to take a payment, first, we must create an invoice which can be as simple or complex as your use case demands.

* Creating the invoice with fixed price (1 dollar) and beginning Payment
```java
//create the invoice..
Invoice myOneDollarFixedPriceInvoice = DomainFactory.newInvoiceWithFixedAmountItem(new BigDecimal(1));

//begin the payment using above created invoice..
PayPalHereSDK.getTransactionManager().beginPayment(myOneDollarFixedPriceInvoice, transactionController);
```

* Creating the empty invoice, adding the items to it and beginning payment
```java
//Create empty invoice
Invoice myInvoice = DomainFactory.newEmptyInvoice();

//create new invoice item
InvoiceItem myInvoiceItem = DomainFactory.newInvoiceItem("Name Of The Item", "Inventory ID", new BigDecimal(10));

//add the invoice item to invoice
myInvoice.addItem(myInvoiceItem, new BigDecimal(1));

//begin the payment
PayPalHereSDK.getTransactionManager().beginPayment(myInvoice, transactionController);
```

* Beginning the payment which will in turn returns you with the invoice.
```java
//To begin payment with no amount and later adding items to invoice
Invoice invoice = PayPalHereSDK.getTransactionManager().beginPayment(transactionController);

//create new invoice item
InvoiceItem myInvoiceItem = DomainFactory.newInvoiceItem("Name Of The Item", "Inventory ID", new BigDecimal(10));

//add the invoice item to invoice
invoice.addItem(myInvoiceItem, new BigDecimal(1));
```

ProcessPayment
================================

PayPalHere SDK provides simple APIs to process payments which will take care of showing the UI which is needed to complete the transaction. Process payment API takes care of:

* Reader connection and activation
* Listening for card events
* Complicated EMV flows
* Signature entry UI and transmission
* Receipt destination UI and transmission

Before calling process payment API, please make sure to:

* Implemeint `TransactionController` Interface
* Call `PayPalHereSDK.getTransactionManager().beginPayment()` as described in the above step "Creating Invoice and Beginning Payment"

Once the above steps are completed then call process payment of the transaction manager
```java
PayPalHereSDK.getTransactionManager().processPaymentWithSDKUI(TransactionManager.PaymentType.CardReader, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
            @Override
            public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                //Successfully completed the payment
            }

            @Override
            public void onError(PPError<TransactionManager.PaymentErrors> error) {
                //Failed to take payment. error object will indicate what was the error.
            }
        });
```

The approach for taking a refund is very similar.

For more information about the apis please visit [full API documentation](http://paypal-mobile.github.io/android-here-sdk-dist/javadoc/index.html).

Card Readers
================================

Although `TransactionManager` is capable of managing card readers by itself there may be times when you require more information about the card reader or more granular control over card readers. This functionality is provided by `CardReaderManager`.

**Available Card Readers**

To get all the list of available card readers
```java
List<CardReader> availableCardReaders = PayPalHereSDK.getCardReaderManager().getAvailableReaders();
```

To get the currently active reader type (while multiple readers are connected)
```java
ReaderTypes activeReaderType = PayPalHereSDK.getCardReaderManager().getActiveReaderType();
```

**Card Reader Listener Events**

If you wish to monitor the events of a card reader such as connection, metadata updates, and magstripe interactions doing so is as simple as implementing the interface `CardReaderListener` and registering it with `CardRederManager`
```java
PayPalHereSDK.getCardReaderManager().registerCardReaderListener(cardReaderListener);
```

More Stuff to Look At
=====================
There is a lot more available in the PayPal Here SDK.  More detail is available in our [developer documentation](https://developer.paypal.com/docs/integration/paypal-here/android-dev/getting-started/) to show other capabilities.  These include:
* **Sideloader:** As an alternative to the SDK, a developer can also use a URI framework that lets one app (or mobile webpage) link directly to the PayPal Here app to complete a payment.  Using this method, the merchant will tap a button or link in one app, which will open the pre-installed PayPal Here app on their device, with the PayPal Here app pre-populating the original order information, collect a payment in the PayPal Here app, and return the merchant to the original app/webpage. This is available for US, UK, Australia, and Japan for iOS & Android.  See the [Sideloader API](https://github.com/paypal/here-sideloader-api-samples) on Github.
* **Auth/Capture:** Rather than a one-time sale, authorize a payment with a card swipe, and complete the transaction at a later time.  This is common when adding tips after the transaction is complete (e.g. at a restaurant).
* **Refunds:** Use the SDK to refund a transaction
* **Send Receipts:** You can use services through the SDK to send email or SMS receipts to customers
* **Key-in:** Most applications need to let users key in card numbers directly, in case the card's magstripe data can no longer be read.
* **CashierID:** Include your own unique user identifier to track a merchant's employee usage
* **Error Handling:** See more detail about the different types of errors that can be returned
* **Marketing Toolkit:** Downloadable marketing assets – from emails to banner ads – help you quickly, and effectively, promote your app’s new payments functionality. 
