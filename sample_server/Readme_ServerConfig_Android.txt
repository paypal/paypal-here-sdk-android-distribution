The Sample Server
=================

A core aspect of allowing your merchants to access your (and PayPal's) systems is authentication.  We have included a
sample app which works in conjunction with this sample service.   These show the basics of authentication and obtaining
the credentials needed to allow your merchant to make SDK calls.  This document describes the included sample server and
how to use it / configure it.  It is anticipated that your business will already provide a service for authenticating
your merchants ... in that case this sample server should serve as a guide in modifying your existing systems to also
handle a PayPal login.


PayPal Access
=============

In order to authenticate merchants to PayPal and to issue API calls on their behalf for processing payment, you use
PayPal Access, which uses standard OAuth protocols. Basically, you send the merchant to a web page on paypal.com,
they login, and are then redirected back to a URL you control with an "oauth token." That token is then exchanged for
an "access token" which can be used to make API calls on the merchant's behalf. Additionally, a "refresh token" is
returned in that exchange that allows you to get a new access token at some point in the future without merchant
interaction. All of this is based on two pieces of data from your application - an app id and a secret. You can setup
PayPal Access and/or create an application via the [devportal](https://devportal.x.com/). As of this writing your
application will still need to be specifically enabled for the PayPal Here scope, so reach out to your PayPal contact
to enable your app for access. Also, you may refer to the documentation for the server API (link to come) for more
detailed information on setting up and using PayPal Access.

You'll note that it asks you for a Return URL, and that this Return URL must be http or https. This means you can't
redirect directly back to your mobile app after a login. But the good news is this would be a terrible idea anyways.
You never want to store your application secret on a mobile device - you can't be sure it isn't jailbroken or
otherwise compromised and once it's out there you don't have many good options for updating all your users.
So instead, you need a backend server to host this secret and control the applications usage of OAuth on
behalf of your merchants. While you can use PayPal Access as your sole point of authentication, you likely have an
existing account system of some sort, so you would first authenticate your users to your system, then send them to
PayPal and link up the accounts on their return.

The other good news is that we've included a simple sample implementation of a back end server with the SDK, written
in Node.js which most people should be able to read reasonably easily. The sample server implements four REST service
endpoints:

1. /login - a dummy version of your user authentication. It returns a "secret ticket" that can be used in place of a
password to reassure you that the person you're getting future requests from is the same person that typed in their
password to your application.
2. /goPayPal - validates the ticket and returns a URL which your mobile application can open in Safari to start
the PayPal access flow. This method specifies the OAuth scopes you're interested in, which must include the PayPal
Here scope (https://uri.paypal.com/services/paypalhere) if you want to use PayPal Here APIs.
3. /goApp - when PayPal Access completes and the merchant grants you access, PayPal will return them to this
endpoint, and this endpoint will inspect the result and redirect back to your application. First, the code calls PayPal
to exchange the OAuth token for the access token. The request to do the exchange looks like this:
```javascript
    request.post({
      url:config.PAYPAL_ACCESS_BASEURL + "auth/protocol/openidconnect/v1/tokenservice",
        auth:{
          user:config.PAYPAL_APP_ID,
          pass:config.PAYPAL_SECRET,
          sendImmediately:true
        },
        form:{
          grant_type:"authorization_code",
          code:req.query.code
        }
    }, function (error, response, body) {
    });
```
Now comes the important part. The server encrypts the access token received from PayPal using the client ticket so that
even if someone has hijacked your application's URL handler, the data will be meaningless since it wasn't the one that
sent the merchant to the PayPal Access flow anyways (this implies you chose your ticket well - the sample server doesn't
really do this because there's no backend to speak of, it's just a flat file database). Secooen. This URL is to the /refresh handler and
includes the refresh token issued by PayPal encrypted with an "account specific server secret." The refresh token is
never stored on the server, and is not stored in a directly usable form on the client either. This minimizes the value
of centralized data on your server, and allows you to cutoff refresh tokens at will in cases of client compromise.
4. /refresh/username/token - This handler decrypts the refresh token and calls the token service to get a new
access token given that refresh token.

To setup the server you need to setup your ReturnURL in PayPal Access to point to your instance of the sample server.
Assuming you want to test on a device, this URL needs to work on that device and on your simulator typically, meaning
you need a "real" DNS entry somewhere. Hopefully you can do this on your office router, or buy a cheap travel router
and do it there. Alternatively, you could stick the server on heroku or some such. See config.js in the sample-server
directory for the variables you need to set to run the sample server. To run the sample server, after modifying
config.js, install Node.js and run "npm install" in the sample-server directory. Then run "node server.js" and you
should see useful log messages to the console. The server advertises itself using Bonjour/zeroconf, so the sample app
should find it automatically. But again, the return URL in PayPal Access is harder to automate, so you'll need to
configure that once. One instance of the sample server can serve all your developers in theory, so it's easiest to
run it on some shared or external resource.

Heroku
======
The included node service should be easily uploaded to a service like Heroku.  However, Heroku is not required.  It's
a simple node service that should run on wantever platform you desire.  Your local laptop, a server in your building, or
onto a hosting service like heroku. Whatever server you deploy it to you'll need to tell the included sample app
the URL of that server.  Please see the login related code in the sample app.

Configuring the Sample Service
==============================
The file config.js has several key fields in it.  Inside you'll find a fake ApplicationId and a fake Application Secret.
You'll also find something called the BASE URL and a few other flags.  To get off the ground quickly you'll need to replace
the Application Id and Secret with your own values obtained from developer.x.com.  Also, you should use the sandbox
url for the BASE URL until you are ready to test against live.  It is also possible to point the service to a special
PayPal test server called a stage in some instances.

Connecting your Mobile App to Your Service
==========================================
In the Android and iOS Sample apps please see the example code for Oauth login.  It shows an example of how
to perform the calls to the sample merchant service mentioned above.  The example code demonstrates the basics of how
to login to the sample server and obtain an oauth token from PayPal.  It then demonstrates how to init the SDK using
those credentials.  Note that obtaining paypal credentials is outside of the scope of the SDK.  The sample code and sample
service illustrate one way to do it.  If you roll your own oauth login code at some point you will have an oauth access
token you can initialize the SDK with.  That will allow the SDK to make the backend web service calls needed to enable
the card reading, purchasing, refunds, and merchant/client checkin functionality.


Opening Consumer Tabs
=====================
To checkin consumers to merchants, use the checkin.js script in the scripts directory. For example:

```
npm install
node checkin.js --help
node checkin.js -m selleraccount@paypal.com -c buyeraccount@paypal.com -i tombrady.png
```

Use -i to add an image for the buyer - this only needs to be done once. Sometimes it takes a few runs to get through,
and images tend to be very finicky on sandbox / stages.
