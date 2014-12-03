/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.emv.sampleapp.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.activity.MainActivity;
import com.paypal.emv.sampleapp.utils.AddressUtil;
import com.paypal.merchant.sdk.AuthenticationListener;
import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.Address;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.DomainFactory;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.Merchant.AvailabilityTypeEnum;
import com.paypal.merchant.sdk.domain.Merchant.MobilityTypeEnum;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.credentials.Credentials;
import com.paypal.merchant.sdk.domain.credentials.OauthCredentials;
import com.paypal.merchant.sdk.internal.domain.Country;

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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
public class OAuthLoginActivity extends Activity {

    public static final String PREFS_NAME = "MerchantPrefs";
    public static final String REFRESH_URL = "refreshUrl";
    public static final String PREFS_LAST_GOOD_USERNAME = "PREFS_LAST_GOOD_USERNAME";
    public static final String PREFS_LAST_GOOD_SERVER = "PREFS_LAST_GOOD_SERVER";
    public static final String PREFS_LAST_GOOD_EMV_CONFIG_REPO = "PREFS_LAST_GOOD_EMV_CONFIG_REPO";
    private static final String LOG = OAuthLoginActivity.class.getSimpleName();
    //private static final String MERCHANT_SERVICE_STAGE_URL = "http://192.168.0.141:8000/server/";
    private static final String MERCHANT_SERVICE_URL = "http://hidden-spire-8232.herokuapp.com/server/";
    private static SharedPreferences mSharedPrefs;
    private static int HANDLER_MESSAGE_INVALID_CREDENTIALS = 3001;
    /**
     * An authentication listener that is registered with the SDK and would be called when the access token of the
     * merchant expires.
     */
    protected AuthenticationListener authenticationListener = new AuthenticationListener() {
        @Override
        public void onInvalidToken() {

            String refreshUrl = getRefreshUrl();
            if (null != refreshUrl && refreshUrl.length() > 0) {
                mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALID_CREDENTIALS);
                return;
            }

            //RefreshTokenTask refreshAccessTokenTask = new RefreshTokenTask();
            //refreshAccessTokenTask.execute(refreshUrl);

        }
    };
    private static int HANDELR_MESSAGE_TOKEN_REFRESH_SUCCESS = 3002;
    /**
     * Handler to display messages to UI while refreshing access tokens.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (HANDLER_MESSAGE_INVALID_CREDENTIALS == msg.what) {
                removedSavedRefreshUrl();
                Toast.makeText(OAuthLoginActivity.this, OAuthLoginActivity.this.getString(R.string.invalid_login), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(OAuthLoginActivity.this, LoginScreenActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            } else if (HANDELR_MESSAGE_TOKEN_REFRESH_SUCCESS == msg.what) {
                Toast.makeText(OAuthLoginActivity.this, OAuthLoginActivity.this.getString(R.string.credentials_refreshed), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private String mMerchantServiceUrl;
    private String mTicket;
    private Merchant mMerchant;
    private String mUsername;
    private String mPassword;
    private String mServerName;
    private boolean mUseLive = false;
    private WebView mLoginWebView;
    private DialogFragment mMerchantInitDialog;
    private ProgressDialog mProgressDialog;
    private String mAccessToken;

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
        /* Hiding this view initially coz if the heroku server already has the merchant credentials,
           we dont need to show this paypal access web view to the merchant and ask him to login again. If the server
           doesnt have the merchant credentials, then, show this web view and ask the merchant to login in via the
           same. */
        mLoginWebView.setVisibility(View.GONE);

        // Find and set the progress dialog.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        // Get the username and password from the previous login screen.
        mUsername = getIntent().getStringExtra("username");
        mPassword = getIntent().getStringExtra("password");
        mServerName = getIntent().getStringExtra("servername");
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        setMerchantServiceUrl();
        // Checking to see if the server urls are set above.
        // NOTE: This check is looking for a url that has the domain "herokuapp.com". If the 3rd apps are using any
        // other server url, PLEASE REMOVE THIS CHECK.
        if (isHerokuUrlsSet()) {
            performOAuthLogin();
        } else {
            herokuUrlsNotSetErrorMessage();
        }
    }

    private void setMerchantServiceUrl() {
        String currentServer = PayPalHereSDK.getCurrentServer();
        if (currentServer.equalsIgnoreCase(PayPalHereSDK.Live)) {
            mUseLive = true;
        }
        mMerchantServiceUrl = MERCHANT_SERVICE_URL;
    }

    /**
     * Method to check if the server urls are provided.
     * If they arent, dont allow the application to proceed.
     *
     * @return
     */
    private boolean isHerokuUrlsSet() {
        if (null == mMerchantServiceUrl || mMerchantServiceUrl.length() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Method to show the progress dialog with a suitable message.
     */
    private void showProgressDialog() {
        mProgressDialog.setMessage("Logging in...");
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

    private void proceedToMainActivity() {
        Intent intent = new Intent(OAuthLoginActivity.this, MainActivity.class);
        startActivity(intent);
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

        loadDefaultMerchantAddressInfo(vi);

        // Read all the address information that is entered by the merchant.
        addressDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                Address.Builder addrBuilder = DomainFactory.newAddressBuilder();

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

                Merchant m = PayPalHereSDK.getMerchantManager().getActiveMerchant();
                m.setBusinessName(getMerchantInfo().getBusinessName());
                m.setFullName(getMerchantInfo().getFirstName(), getMerchantInfo().getLastName());
                m.setAddress(address);

                proceedToMainActivity();
            }
        });

        addressDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Since our intermediate Heroku server does return a merchant address back,
                // we use that.
                Merchant m = PayPalHereSDK.getMerchantManager().getActiveMerchant();
                m.setAddress(getMerchantInfo().getCurrentAddress());
                // If there is no merchant address returned by the server or if the merchant hasnt keyed in any
                // address, DO NOT ALLOW them to proceed further since the underlying APIs do need a valid address.
                proceedToMainActivity();
            }
        });

        addressDialog.show();
    }

    private void loadDefaultMerchantAddressInfo(View vi) {
        Address address = null;
        Merchant m = PayPalHereSDK.getMerchantManager().getActiveMerchant();

        if (m.getEmail().contains("uk") || m.getEmail().contains("UK") || m.getEmail().contains("gb") || m.getEmail().contains("GB")) {

            address = AddressUtil.getDefaultUKMerchantAddress();
            m.setMerchantCurrency(Currency.getInstance("GBP"));
            m.setUserCountry(new Country("GB"));
            //PayPalHereSDK.getCardReaderManager().beginMonitoring(CardReaderListener.ReaderConnectionTypes.Bluetooth);
        } else {
            address = AddressUtil.getDefaultUSMerchantAddress();
            m.setMerchantCurrency(Currency.getInstance("USD"));
            m.setUserCountry(new Country("US"));
            //  PayPalHereSDK.getCardReaderManager().beginMonitoring(CardReaderListener.ReaderConnectionTypes.AudioJack);
        }

        ((EditText) vi.findViewById(R.id.address_1)).setText(address.getLine1());
        ((EditText) vi.findViewById(R.id.address_2)).setText(address.getLine2());
        ((EditText) vi.findViewById(R.id.city)).setText(address.getCity());
        ((EditText) vi.findViewById(R.id.state)).setText(address.getState());
        ((EditText) vi.findViewById(R.id.country)).setText(address.getCountryCode());
        ((EditText) vi.findViewById(R.id.zip)).setText(address.getPostalCode());
    }

    private void setupMerchantInfo(Merchant merchant) {
        this.mMerchant = merchant;
    }

    private Merchant getMerchantInfo() {
        return mMerchant;
    }

    private void goBackToLoginScreen() {
        Intent i = new Intent(OAuthLoginActivity.this, LoginScreenActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    private void loginPayPalFailed(String msg) {
        Log.d(LOG, "doneWithLoginPayPal. Login Failed");
        msg = ((null == msg || msg.length() <= 0) ? "Login Failed!" : "Login Failed : " + msg);
        Toast.makeText(OAuthLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
        hideProgressDialog();
        goBackToLoginScreen();
    }

    private void herokuUrlsNotSetErrorMessage() {
        Log.d(LOG, "Heroku urls are not set.");
        Toast.makeText(OAuthLoginActivity.this, "Heroku server urls are not set! Please provide the urls to continue.", Toast.LENGTH_SHORT).show();
        hideProgressDialog();
        goBackToLoginScreen();
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
                }
        );
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
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
        mLoginWebView.requestFocus(View.FOCUS_DOWN);
        mLoginWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(
                    WebView view, String url) {
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
                        setMerchantAndCheckIn(decryptedAccessToken, refreshUrlToUse, expiry);

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
            String refreshUrlToUse = mMerchantServiceUrl + "refresh/" +
                    mUsername +
                    "/" + encodedToken;
            // Set the merchant credetials within the PayPalHere SDK.
            setMerchantAndCheckIn(decryptedAccessToken, refreshUrlToUse, expiry);

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("PayPalHere", "decrypt exception = " + ex.getMessage());
        }

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
     * This method is meant to init the merchant with the PPHSDK and perform a merchant check-in at the same time
     * for checkin based transactions.
     *
     * @param accessToken
     */
    private void setMerchantAndCheckIn(String accessToken, String refreshUrl, String expiry) {
        saveRefreshUrl(refreshUrl);
        saveLastGoodServer();
        saveLastGoodEMVConfigStage();
        saveLastGoodUsername();

        saveAccessToken(accessToken);
        /* Create a credentials obj based off of the decrypted access token.
           Should also implement a callback listener in case the access token is expired. */
        Credentials credentials = new OauthCredentials(accessToken, refreshUrl, expiry);
        Log.d("Access Token", accessToken);
        showProgressDialog();
        // Init the SDK with the current merchant credentials.
        PayPalHereSDK.setCredentials(credentials, new DefaultResponseHandler<Merchant,
                PPError<MerchantManager.MerchantErrors>>() {
            @Override
            public void onSuccess(Merchant merchant) {
                /* Register the Authentication listener (implemented in the parent class MyActivity) to listen for any
                   invalid credentials callbacks sent by the SDK. */
                PayPalHereSDK.registerAuthenticationListener(authenticationListener);
                hideProgressDialog();
                proceedToMainActivity();
            }

            @Override
            public void onError(PPError<MerchantManager.MerchantErrors> arg0) {
                Log.e(LOG, "Merchant init failed : " + arg0.getDetailedMessage());
                loginPayPalFailed(arg0.getDetailedMessage());
            }
        });
    }

    /**
     * Upon successful login with PayPal via PayPalAccess, an access token and a refresh url would be returned back.
     * It is currently the application's responsibility to save this refresh url and invoke the same if needed when
     * the token expires.
     * <p/>
     * The expiration of the token would be indicated by the SDK by invoking the AuthenticationListener callback
     * (implemented above).
     *
     * @param refreshUrl : refresh url to be saved.
     */
    public void saveRefreshUrl(String refreshUrl) {

        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(REFRESH_URL, refreshUrl);
        editor.commit();
        Log.d("saveRefreshUrl", "Saving the refresh url: " + refreshUrl);
    }

    private void saveLastGoodServer() {
        String successfulServer = PayPalHereSDK.getCurrentServer();

        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_LAST_GOOD_SERVER, successfulServer);
        editor.commit();
        Log.d("saveRefreshUrl", "Saving the last good server: " + successfulServer);
    }

    private void saveLastGoodEMVConfigStage() {
        String successfulEMVConfigRepo = PayPalHereSDK.getEMVConfigRepo();
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_LAST_GOOD_EMV_CONFIG_REPO,successfulEMVConfigRepo);
        editor.commit();
        Log.d("saveLastGoodEMVConfigStage", "Saving the last good EMV Config Repo: " + successfulEMVConfigRepo);
    }

    private void saveLastGoodUsername() {
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_LAST_GOOD_USERNAME, mUsername);
        editor.commit();
        Log.d("saveRefreshUrl", "Saving the last good username: " + mUsername);
    }

    public void saveAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    /**
     * Method to retrieve the refresh url that is saved as a preference within the app.
     *
     * @return : refresh url string.
     */
    public String getRefreshUrl() {
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(REFRESH_URL, null);
    }

    private String getLastGoodServer() {
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(PREFS_LAST_GOOD_SERVER, null);
    }

    private String getLastGoodUsername() {
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(PREFS_LAST_GOOD_USERNAME, null);
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
     * Method to remove the saved refresh url from the shared preference.
     */
    private void removedSavedRefreshUrl() {
        mSharedPrefs = getSharedPreferences(PREFS_NAME, 0);
        if (mSharedPrefs != null) {
            mSharedPrefs.edit().remove(REFRESH_URL).commit();
        }
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

        private synchronized String getServerName(){
            return mServerName;
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
                Address.Builder addrBuilder = DomainFactory.newAddressBuilder();
                addrBuilder.setLine1(addressLine1).setCity(city)
                        .setState(stateCode).setPostalCode(postalCode)
                        .setCountryCode(countryCode);
                Address address = addrBuilder.build();
                String merchantEmail = null;

                Currency currency = Currency.getInstance(currencyCode);

                // Create the Merchant object that is required by the
                // PayPalHereSDK to authenticate the initialize the Merchant.
                mMerchantInfo = DomainFactory.newMerchant(businessName, null, address,
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
                nameValuePairs.add(new BasicNameValuePair("username",getUsername()));
                nameValuePairs.add(new BasicNameValuePair("password",getPassword()));
                nameValuePairs.add(new BasicNameValuePair("servername",getServerName()));
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
                loginPayPalFailed("");

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

        private synchronized String getServerName(){
            return mServerName;
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
                nameValuePairs.add(new BasicNameValuePair("servername",getServerName()));
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
                loginPayPalFailed("");
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
