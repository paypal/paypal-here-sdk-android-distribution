/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.Address;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.Merchant.AvailabilityTypeEnum;
import com.paypal.merchant.sdk.domain.Merchant.MobilityTypeEnum;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.credentials.Credentials;
import com.paypal.merchant.sdk.domain.credentials.OauthCredentials;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;
import com.paypal.sampleapp.util.ProgressDialogFragment;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

/**
 * This activity displays an PayPal login web view dialog for OAuth based login.
 * <p/>
 * The main purpose of this screen is to retrieve the access token that would be used by the app to make to the
 * PayPal Here APIs.
 * <p/>
 * In order to obtain, store and retrieve the access token, the app can make use of any convenient method of
 * their choice.
 * <p/>
 * The method used by this sample app is as follows:
 * <p/>
 * 1. App talks to an intermediate Heroku server by passing it the merchant username and password.
 * 2. The server returns back a ticket id and the merchant info in the form a JSON response.
 * 3. App again calls the server with the ticket ID and the username to retrieve back either:
 * a. PayPal access url
 * b. Merchant credentials: Access token, refresh url and expiry date.
 * 4. If the PayPal access url is obtained, a web view is created where the merchant needs to log into paypal,
 * and after a successful login, they get back the merchant credentials.
 * 5. If the merchant credentials are obtained directly, that is good enough to pass it to the SDK.
 * <p/>
 * <p/>
 * In this activity, we also "check-in" the merchant after a successful login.
 */
public class OAuthLoginActivity extends MyActivity {

    private static final String LOG = "PayPalHere.OAuthLoginScreen";
    private static final String MERCHANT_SERVICE_STAGE_URL = "http://morning-tundra-8515.herokuapp.com/";
    private static final String MERCHANT_SERVICE_LIVE_URL = "http://stormy-hollows-1584.herokuapp.com/";
    private String mMerchantServiceUrl;
    private String mTicket;
    private Merchant mMerchant;
    private String mUsername;
    private String mPassword;
    private boolean mUseLive;
    private WebView mLoginWebView;
    private DialogFragment mMerchantInitDialog;
    private ProgressDialog mProgressDialog;

    /**
     * initialize the various layout elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setting the layout for this activity.
        setContentView(R.layout.activity_oauthlogin_screen);
        // Find and set the webview that would display the PayPal access url page.
        mLoginWebView = (WebView) findViewById(R.id.login_webview);
        // Hiding this view initially coz if the heroku server already has the merchant credentials,
        // we dont need to show this paypal access web view to the merchant and ask him to login again. If the server
        // doesnt have the merchant credentials, then, show this web view and ask the merchant to login in via the
        // same.
        mLoginWebView.setVisibility(View.GONE);

        // Find and set the progress dialog.
        mProgressDialog = new ProgressDialog(OAuthLoginActivity.this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        // Get the username and password from the previous login screen.
        mUsername = getIntent().getStringExtra("username");
        mPassword = getIntent().getStringExtra("password");
        mUseLive = getIntent().getBooleanExtra("useLive", false);

        mMerchantServiceUrl = (mUseLive) ? MERCHANT_SERVICE_LIVE_URL : MERCHANT_SERVICE_STAGE_URL;
        performOAuthLogin();

    }

    /**
     * Method to show the progress dialog with a suitable message.
     */
    private void showProgressDialog() {
        mProgressDialog.setMessage("Authorizing... Please wait!");
        mProgressDialog.show();
    }

    /**
     * Method to hide the progress dialog.
     */
    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    /**
     * Method to perform the OAuth login.
     */
    private void performOAuthLogin() {
        // Show the progress dialog
        showProgressDialog();

        // call the 3rd party/intermediate server (heroku etc.) api to log the user in with
        // their credentials and get back the "ticket" info and the "merchant"
        // info in the response.
        LoginTask task = new LoginTask(mUsername, mPassword);
        task.execute();

    }

    /**
     * Move over to the next activity, which is the billing activity.
     */
    private void proceedToBillingActivity() {
        Intent intent = new Intent(OAuthLoginActivity.this, BillingTypeTabActivity.class);
        startActivity(intent);

        // Finish this activity
        finish();

    }

    /**
     * This method is meant to take in the merchant address.
     * <p/>
     * The reason we do this is because the address given by the merchant when registering with PayPal might be a
     * different one, such as their home address etc.
     * We would need/require another address that is more of a business address,
     * which might not be the same the address given while registering with PayPal.
     */
    private void openAddressDialog() {
        AlertDialog.Builder addressDialog = new AlertDialog.Builder(this);
        addressDialog.setTitle("Merchant Address Info");
        final View vi;
        LayoutInflater inflater = this.getLayoutInflater();
        addressDialog.setView(vi = inflater.inflate(R.layout.address, null));

        // Read all the address information that is entered by the merchant.
        addressDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                Address.Builder addrBuilder = new Address.Builder();

                EditText val = (EditText) vi.findViewById(R.id.address_1);
                addrBuilder.setLine1(val.getText().toString());

                val = (EditText) vi.findViewById(R.id.address_2);
                addrBuilder.setLine2(val.getText().toString());

                val = (EditText) vi.findViewById(R.id.city);
                addrBuilder.setCity(val.getText().toString());

                val = (EditText) vi.findViewById(R.id.state);
                addrBuilder.setState(val.getText().toString());

                val = (EditText) vi.findViewById(R.id.country);
                addrBuilder.setCountryCode(val.getText().toString());

                val = (EditText) vi.findViewById(R.id.zip);
                addrBuilder.setPostalCode(val.getText().toString());

                Address address = addrBuilder.build();

                // Once the merchant has logged in, the SDK wouldve created a merchant object,
                // known as the active merchant.
                // Retrieve this merchant object and set the business name, first name,
                // last name and the keyed in address.
                Merchant m = PayPalHereSDK.getMerchantManager().getActiveMerchant();
                m.setBusinessName(getMerchantInfo().getBusinessName());
                m.setFullName(getMerchantInfo().getFirstName(), getMerchantInfo().getLastName());
                m.setAddress(address);

                // Once the address is set, proceed to the billing activity.
                proceedToBillingActivity();
            }
        });

        addressDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Closes the dialog.

                // Since our intermediate Heroku server does return a merchant address back,
                // we use that.
                Merchant m = PayPalHereSDK.getMerchantManager().getActiveMerchant();
                m.setAddress(getMerchantInfo().getAddress());
                // If there is no merchant address returned by the server or if the merchant hasnt keyed in any
                // address, DO NOT ALLOW them to proceed further since the underlying APIs do need a valid address.
                proceedToBillingActivity();
            }
        });

        addressDialog.show();
    }

    private void setupMerchantInfo(Merchant merchant) {
        this.mMerchant = merchant;
    }

    private Merchant getMerchantInfo() {
        return mMerchant;
    }

    private void goBackToLoginScreen() {
        Intent i = new Intent(OAuthLoginActivity.this, LoginScreenActivity.class);
        startActivity(i);
    }

    /**
     * The code below shows an example of how an might want to handle the OAuth
     * login stuff. It can be ignored / removed completely based on the 3rd
     * party app's implementation.
     * <p/>
     * All we are trying to do here is get the access token from the PayPal
     * servers hence different apps might have their own way of implementing
     * this below portion.
     */

    void validateTicketWithMerchantService(String ticket) {
        if (ticket == null) {
            Log.e(LOG, "null ticket!");
        }
        mTicket = ticket;
        // On to the next step.
        GoPayPalTask asyncTask = new GoPayPalTask(ticket, mUsername);
        asyncTask.execute();
    }

    /**
     * If the PayPal login failed, inform the user of the same.
     */
    private void goPayPalFailed() {
        Log.d(LOG, "doneWithGoPayPal. Login Failed");
        CommonUtils.createToastMessage(OAuthLoginActivity.this,
                "Login failed!");
        hideProgressDialog();

    }

    /**
     * If the login is successful, obtain the merchant credentials and initialize the merchant.
     *
     * @param accessToken
     * @param refreshUrl
     * @param expiry
     */
    private void goPayPalSuccessWithAccessToken(String accessToken,
                                                String refreshUrl, String expiry) {
        finishMerchantInit(accessToken, refreshUrl, expiry);
        Log.d(LOG, "goPayPalSuccessWithAccessToken");

    }

    /**
     * This method creates an alert dialog to ask for a confirmation from the
     * user that the app needs to talks to the PP servers
     *
     * @param url
     */
    private void goPayPalSuccessWithURL(final String url) {
        Log.d(LOG,
                "goPayPalSuccessWithURL - Now need to log into PayPal Access");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Log into PayPal Access Needed");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Go to PayPal Access",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getURL(url);
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        hideProgressDialog();

    }

    /**
     * This method creates a web view of the PayPal access login.
     *
     * @param payPalAccessUrl
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void getURL(String payPalAccessUrl) {

        // Show the webview now.
        mLoginWebView.setVisibility(View.VISIBLE);
        // load the web view with paypal url.
        mLoginWebView.loadUrl(payPalAccessUrl);
        // Enable the js functionality.
        mLoginWebView.getSettings().setJavaScriptEnabled(true);
        mLoginWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(
                    android.webkit.WebView view, java.lang.String url) {
                if (url != null && url.contains("sdksampleapp://oauth?")) {
                    String accessTokenPrefix = "access_token=";
                    String refreshUrlPrefix = "refresh_url=";
                    String expirtyTimePrefix = "expires_in=";
                    String userInfoPrefix = mUsername + "/";
                    String delimit = "&";
                    // The access token, refresh url and the exp date are a part of the url.
                    // Extract them by splitting the string.
                    String[] tokens = url.split(delimit);
                    String accessTmp = null;
                    String access = null;
                    String refreshUrl = null;
                    String expiry = null;
                    for (String s : tokens) {
                        //After a successful login, get the access_token value, which would be used to calls teh PPH
                        // servers.
                        if (s.contains("access_token=")) {
                            accessTmp = s.substring(s.indexOf(accessTokenPrefix) + accessTokenPrefix.length());
                            String[] accessToken = accessTmp.split("%3D");
                            access = accessToken[0];

                        } else if (s.contains("refresh_url=")) {
                            refreshUrl = s.substring(s.indexOf(refreshUrlPrefix) + refreshUrlPrefix.length());
                        } else if (s.contains("expires_in=")) {
                            expiry = s.substring(s.indexOf(expirtyTimePrefix)
                                    + expirtyTimePrefix.length());
                        }
                    }
                    try {
                        // The access token that we retrieve would be URL encoded as well as Base64 encoded. Hence,
                        // we need to first URL decode and then Base64 decode the access token.
                        String base64EncryptedRawDataString = URLDecoder.decode(
                                access, "UTF-8");

                        String decryptedAccessToken = base64Decode(base64EncryptedRawDataString, mTicket);

                        //similarly, the refresh url part of the base url is also url encoded - decode it first
                        String decodedRefreshUrl = URLDecoder.decode(refreshUrl, "UTF-8");

                        //NOTE: now the decodedRefreshUrl is the result of decoding both the base url as well as the
                        // refresh token that is
                        //a part of it. From this we will extract the refresh token part of the url
                        String refreshTokenRaw = decodedRefreshUrl.substring(decodedRefreshUrl.indexOf
                                (userInfoPrefix) + userInfoPrefix.length());

                        //Finally, before we submit the refresh url to the sdk we need to make sure that the refresh
                        // token part of it is URL Encoded
                        String refreshTokenEncoded = URLEncoder.encode(refreshTokenRaw, "UTF-8");
                        //the actual refresh url to use = base url + url encoded refresh token
                        String refreshUrlToUse = mMerchantServiceUrl + "refresh/" + mUsername + "/" +
                                refreshTokenEncoded;


                        // Set the merchant credentials within the PayPalHere SDK.
                        setMerchantAndCheckIn(decryptedAccessToken);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(LOG, "decrypt exception = " + ex.getMessage());
                    }
                }
                return false;
            }

        });

    }

    /**
     * This method retrieves the access token and sets the same in the PayPalHere SDK.
     *
     * @param access
     * @param refreshUrl
     * @param expiry
     */
    void finishMerchantInit(String access, String refreshUrl, String expiry) {
        try {
            // The access token obtained in this case would be Base64 encoded hence, we would need to decode that.
            String decryptedAccessToken = base64Decode(access, mTicket);

            String userInfoPrefix = mUsername + "/";
            String refreshTokenRaw = refreshUrl.substring(refreshUrl.indexOf(userInfoPrefix) + userInfoPrefix.length());
            //the refresh token is not URL encoded but before submitting the full refresh url to the SDK we need to
            //url encode the refresh token parts of it
            String encodedToken = URLEncoder.encode(refreshTokenRaw, "UTF-8");
            String refreshUrlToUse = mMerchantServiceUrl + /*"http://morning-tundra-8515.herokuapp.com*/ "refresh/" +
                    mUsername +
                    "/" + encodedToken;
            // Set the merchant credetials within the PayPalHere SDK.
            setMerchantAndCheckIn(decryptedAccessToken);

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("PayPalHere", "decrypt exception = " + ex.getMessage());
        }

    }

    /**
     * Method to show the progress dialog that indicates the merchant setup.
     */
    @SuppressLint("NewApi")
    private void showInitializingMerchantDialog() {
        mMerchantInitDialog = new ProgressDialogFragment();
        mMerchantInitDialog.show(getFragmentManager(), "ProgressDialogFragment");
    }

    /**
     * This decryption method for the access token would perform a base 64 decode.
     * <p/>
     * We would need to perform this type of decode methodology  for all access token that is obtained directly from
     * the heroku server.
     *
     * @param base64EncryptedRawDataString
     * @param password
     * @return
     * @throws Exception
     */
    public String base64Decode(String base64EncryptedRawDataString, String password) throws Exception {
        Cipher dcipher;
        byte[] base64EncryptedRawData = null;

        base64EncryptedRawData = Base64.decode(
                base64EncryptedRawDataString.getBytes(), Base64.DEFAULT);

        if (base64EncryptedRawData.length < 52)
            return null;

        byte[] cipherText = Arrays.copyOfRange(base64EncryptedRawData, 52,
                base64EncryptedRawData.length);
        byte[] salt = Arrays.copyOfRange(base64EncryptedRawData, 0, 16);
        // new String(base64EncryptedRawData).substring(0,16).getBytes();
        int iterationCount = 1000;
        int keyStrength = 256;
        SecretKey key;
        byte[] iv;

        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                iterationCount, keyStrength);
        SecretKey tmp = factory.generateSecret(spec);
        key = new SecretKeySpec(tmp.getEncoded(), "AES");
        dcipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        AlgorithmParameters params = dcipher.getParameters();
        iv = Arrays.copyOfRange(base64EncryptedRawData, 16, 32);

        dcipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] utf8 = dcipher.doFinal(cipherText);
        return new String(utf8, "UTF8");
    }

    /**
     * This decryption method for the access token would first url decode the value and then perform a base 64 decode.
     * <p/>
     * We would need to perform this type of decode methodology for all the access tokens obtained after logging in
     * via the PayPal access web view.
     *
     * @param base64EncryptedRawDataString2
     * @param password
     * @return
     * @throws Exception
     */
    public String urlDecodeAndBase64Decode(String base64EncryptedRawDataString2, String password) throws Exception {

        String base64EncryptedRawDataString = URLDecoder.decode(
                base64EncryptedRawDataString2, "UTF-8");

        return base64Decode(base64EncryptedRawDataString, password);
    }

    /**
     * This method is meant to init the merchant with the PPHSDK and perform a merchant check-in at the same time
     * for checkin based transactions.
     *
     * @param accessToken
     */
    private void setMerchantAndCheckIn(String accessToken) {
        // Create a credentials obj based off of the decrypted access token.
        // Should also implement a callback listener in case the access token is expired.
        Credentials credentials = new OauthCredentials(accessToken);
        // Display a message that indicates the merchant is being setup.
        showInitializingMerchantDialog();
        Log.d("Access Token", accessToken);
        // Init the SDK with the current merchant credentials.
        PayPalHereSDK.setCredentials(credentials, new DefaultResponseHandler<Merchant,
                PPError<MerchantManager.MerchantErrors>>() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(Merchant merchant) {
                mMerchantInitDialog.dismiss();
                // check to see if the merchant is already checked in.
                // In this sample app, we achieve this by storing the checked in merchant location id as a shared
                // preference.
                if (!isMerchantLocationSaved()) {
                    checkinMerchant();
                }
                // Hide the progress dialog.
                hideProgressDialog();
                //Ask the merchant to fill in the address information, which would be set in the SDK and would be
                // used for merchant payments.
                openAddressDialog();
            }

            @Override
            public void onError(PPError<MerchantManager.MerchantErrors> arg0) {
                Log.e(LOG,
                        "Merchant init failed : "
                                + arg0.getDetailedMessage());
            }
        });
    }

    /**
     * Method to check in the merchant.
     */
    private void checkinMerchant() {
        // Checkin the merchant based on geo location provided by the device.
        PayPalHereSDK.getMerchantManager().checkinMerchant("Joe's Pizza",
                new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {

                    @Override
                    public void onSuccess(Merchant merchant) {
                        CommonUtils.createToastMessage(OAuthLoginActivity.this,
                                "Merchant checkin successful!");
                        Log.d(LOG, "Merchant check in successful.");
                        Log.d(LOG, merchant.getCheckedInMerchant().getLocationId());
                        // Save the location id
                        checkAndSaveLocationId();
                    }

                    @Override
                    public void onError(PPError<MerchantManager.MerchantErrors> error) {
                        CommonUtils.createToastMessage(OAuthLoginActivity.this,
                                "checkin unsuccessful. " + error.getDetailedMessage());

                        Log.e(LOG, "checkin unsuccessful. " + error.getDetailedMessage());
                    }
                });

    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    /**
     * This async task class in meant to call/invoke the 3rd party merchant's
     * login url to authenticate the logged in user via their credentials and
     * for obtaining the Token/Ticket of the merchant along with the merchant
     * info.
     */
    private class LoginTask extends AsyncTask<String, Void, ArrayList<String>> {

        String mUsername;
        String mPassword;
        String mTicket;
        boolean mFailed = false;
        Merchant mMerchantInfo;

        public LoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        private synchronized String getUsername() {
            return mUsername;
        }

        private synchronized String getPassword() {
            return mPassword;
        }

        public synchronized String getTicket() {
            return mTicket;
        }

        public synchronized void setTicket(String ticket) {
            mTicket = ticket;
        }

        public synchronized Merchant getMerchantInfo() {
            return mMerchantInfo;
        }

        public synchronized void setFailed() {
            mFailed = true;
        }

        public synchronized boolean isFailed() {
            return mFailed;
        }

        /**
         * This method is used to create a Merchant object based on the json payload response from the heroku server.
         *
         * @param merchantInfoJson
         */
        private synchronized void parseMerchantInfo(JSONObject merchantInfoJson) {
            if (null == merchantInfoJson) {
                setFailed();
                Log.e(LOG, "null merchantInfoJson");
                return;
            }
            try {
                String businessName = merchantInfoJson
                        .getString("businessName");
                String countryCode = merchantInfoJson.getString("country");
                String addressLine1 = merchantInfoJson.getString("line1");
                String city = merchantInfoJson.getString("city");
                String stateCode = merchantInfoJson.getString("state");
                String currencyCode = merchantInfoJson.getString("currency");
                String postalCode = merchantInfoJson.getString("postalCode");
                String phoneNumber = null; // merchantInfoJson.getString("phoneNumber");
                Address.Builder addrBuilder = new Address.Builder();
                addrBuilder.setLine1(addressLine1).setCity(city)
                        .setState(stateCode).setPostalCode(postalCode)
                        .setCountryCode(countryCode);
                Address address = addrBuilder.build();
                String merchantEmail = null;

                Currency currency = Currency.getInstance(currencyCode);

                // Create the Merchant object that is required by the
                // PayPalHereSDK to authenticate the initialize the Merchant.
                mMerchantInfo = new Merchant(businessName, null, address,
                        merchantEmail, currency, "0016193003729");
                mMerchantInfo.setAvailabilityType(AvailabilityTypeEnum.OPEN);
                mMerchantInfo.setMobilityType(MobilityTypeEnum.FIXED);

            } catch (JSONException ex) {
                setFailed();
                Log.e(LOG, ex.getMessage());
            }

        }

        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            // Create the HTTP request to invoke the 3rd party merchant's
            // service url.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(mMerchantServiceUrl + "login");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                        2);

                // Provide the user's login credentials.
                nameValuePairs.add(new BasicNameValuePair("username",
                        getUsername()));
                nameValuePairs.add(new BasicNameValuePair("password",
                        getPassword()));
                nameValuePairs.add(new BasicNameValuePair("isLive", String.valueOf(mUseLive)));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);

                if (response.getStatusLine().getStatusCode() != 200) {
                    setFailed();
                    Log.e(LOG, "Response status code: "
                            + response.getStatusLine().getStatusCode());
                } else {
                    String responseBody = EntityUtils.toString(response
                            .getEntity());
                    Log.d(LOG, "Login Response: " + responseBody);
                    try {
                        // Since the response in a JSON format, create a JSON
                        // object to access the same.
                        JSONObject json = new JSONObject(responseBody);
                        // Extract the ticket from the JSON response.
                        String theTicket = json.getString("ticket");
                        setTicket(theTicket);
                        // Extract the merchant info from the JSON response.
                        JSONObject merchantInfoJson = json
                                .getJSONObject("merchant");
                        parseMerchantInfo(merchantInfoJson);
                    } catch (JSONException e) {
                        setFailed();
                        Log.e(LOG, e.getMessage());
                    }
                }

            } catch (Exception e) {
                setFailed();
                Log.e(LOG, e.getMessage());
            }
            return null;
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(final ArrayList<String> arrayList) {
            if (isFailed()) {
                // Message the user that their login failed
                CommonUtils.createToastMessage(OAuthLoginActivity.this,
                        "Login failed!");

                hideProgressDialog();

                goBackToLoginScreen();

            } else {
                // Let's now attempt to login to Paypal Access
                validateTicketWithMerchantService(getTicket());
                setupMerchantInfo(getMerchantInfo());
            }
        }

    }

    /**
     * This async task class is meant to call/invoke the 3rd party Merchant's
     * goToPayPal url, which in turn talks to the PayPal servers to provide
     * OAuth access.
     */
    private class GoPayPalTask extends
            AsyncTask<String, Void, ArrayList<String>> {
        String mTicket = null;
        String mUsername = null;
        String mURL = null;
        String mAccessToken = null;
        String mRefreshUrl = null;
        String mExpiry = null;
        boolean mFailed = false;

        public GoPayPalTask(String ticket, String username) {
            mTicket = ticket;
            mUsername = username;
        }

        public synchronized String getExpiry() {
            return mExpiry;
        }

        public synchronized void setExpiry(String expiry) {
            mExpiry = expiry;
        }

        public synchronized String getRefreshUrl() {
            return mRefreshUrl;
        }

        public synchronized void setRefreshUrl(String refreshUrl) {
            mRefreshUrl = refreshUrl;
        }

        public synchronized String getAccessToken() {
            return mAccessToken;
        }

        public synchronized void setAccessToken(String token) {
            mAccessToken = token;
        }

        public synchronized String getTicket() {
            return mTicket;
        }

        public synchronized void setTicket(String ticket) {
            mTicket = ticket;
        }

        public synchronized String getURL() {
            return mURL;
        }

        public synchronized void setURL(String url) {
            mURL = url;
        }

        public synchronized void setFailed() {
            mFailed = true;
        }

        public synchronized boolean isFailed() {
            return mFailed;
        }

        private synchronized String getUsername() {
            return mUsername;
        }

        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            // Create the HTTP request to call the Merchant's goPayPal url.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(mMerchantServiceUrl + "goPayPal");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                        2);
                nameValuePairs
                        .add(new BasicNameValuePair("ticket", getTicket()));
                nameValuePairs.add(new BasicNameValuePair("username",
                        getUsername()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);

                if (response.getStatusLine().getStatusCode() != 200) {
                    setFailed();
                    Log.e(LOG, "Response status code: "
                            + response.getStatusLine().getStatusCode());
                } else {
                    String responseBody = EntityUtils.toString(response
                            .getEntity());
                    Log.d(LOG, "Login Response: " + responseBody);
                    try {
                        // Since the response in a JSON format, create a JSON
                        // object to access the same.
                        JSONObject json = new JSONObject(responseBody);

                        // Get the refresh URL from the reponse.
                        if (json.has("url")) {
                            setURL(json.getString("url"));
                        }
                        // Get the access token from the reponse.
                        else if (json.has("access_token")) {
                            setAccessToken(json.getString("access_token"));
                            setRefreshUrl(json.getString("refresh_url"));
                            setExpiry(json.getString("expires_in"));
                        }
                        // If neither are obtained, flag as failed.
                        else {
                            setFailed();
                            Log.e(LOG, "Response has no url nor access_token.");
                        }
                    } catch (JSONException e) {
                        setFailed();
                        Log.e(LOG, e.getMessage());
                    }
                }

            } catch (Exception e) {
                setFailed();
                Log.e(LOG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> arrayList) {
            if (isFailed()) {
                // Message the user that their login failed
                goPayPalFailed();
            } else {
                // If the heroku server returns back a paypal access url, display the same in a webview for the
                // merchant to login and retrieve the access token.
                if (getURL() != null)
                    goPayPalSuccessWithURL(getURL());
                    // If the heroku server returns the merchant credentials such as the access token etc of the already
                    // logged in merchant, then, use the same to set within the SDK.
                else if (getAccessToken() != null)
                    goPayPalSuccessWithAccessToken(getAccessToken(),
                            getRefreshUrl(), getExpiry());

            }
        }
    }
}
