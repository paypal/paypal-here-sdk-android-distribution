/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.paypal.merchant.sdk.AuthenticationListener;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionController;
import com.paypal.merchant.sdk.domain.ChipAndPinDecisionEvent;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.merchant.sdk.domain.credentials.Credentials;
import com.paypal.merchant.sdk.domain.credentials.OAuthCredentials;
import com.paypal.sampleapp.login.LoginScreenActivity;
import com.paypal.sampleapp.util.CommonUtils;
import com.paypal.sampleapp.util.LocalPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


public class MyActivity extends Activity implements CardReaderListener {

    private static final String LOG = "MyActivity";
    private static Bitmap sBitmap;
    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();
    private static SharedPreferences settings;
    private static volatile boolean sIsPaymentCompleted;
    private static BluetoothDevice sEmvDevice;
    private static String sAccessToken;
    private static String sTokenExpirationTime;
    private static boolean sIsHandledByApp = false;
    private static boolean mSwiperConnected = false;

    /**
     * An authentication listener that is registered with the SDK and would be called when the access token of the
     * merchant expires.
     */
    protected AuthenticationListener authenticationListener = new AuthenticationListener() {
        @Override
        public void onInvalidToken() {

            String refreshUrl = getRefreshUrl();
            if (CommonUtils.isNullOrEmpty(refreshUrl)) {
                sHandler.sendEmptyMessage(0);
                return;
            }

            RefreshTokenTask refreshAccessTokenTask = new RefreshTokenTask();
            refreshAccessTokenTask.execute(refreshUrl);

        }
    };
    /**
     * Handler to display messages to UI while refreshing access tokens.
     */
    private Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0) {
                removedSavedRefreshUrl();
                CommonUtils.createToastMessage(MyActivity.this, "Invalid credentials. Please login again.");
                Intent i = new Intent(MyActivity.this, LoginScreenActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            } else if (msg.what == 1) {
                CommonUtils.createToastMessage(MyActivity.this, "Credentials refreshed.");
            }
        }
    };

    /**
     * Method to check if the merchant has checked in.
     *
     * @return
     */
    public boolean isMerchantCheckedIdIn() {
        if (sMerchantManager != null && sMerchantManager.getActiveMerchant() != null && sMerchantManager
                .getActiveMerchant().getCheckedInMerchant() != null) {

            return true;
        }

        return false;
    }

    protected boolean isPaymentCompleted() {
        return sIsPaymentCompleted;
    }

    protected void paymentCompleted(boolean state) {
        sIsPaymentCompleted = state;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        LocalPreferences.setRefreshURL(refreshUrl);
        Log.d("saveRefreshUrl", "Saving the refresh url: " + refreshUrl);
    }

    /**
     * Method to retrieve the refresh url that is saved as a preference within the app.
     *
     * @return : refresh url string.
     */
    public String getRefreshUrl() {
        return LocalPreferences.getRefreshUrl();
    }

    public void saveAccessToken(String accessToken) {
        sAccessToken = accessToken;
    }

    public String getAccessToken() {
        return sAccessToken;
    }

    public String getTokenExpirationTime() {
        return sTokenExpirationTime;
    }

    public void saveTokenExpirationTime(String expirationTime) {
        sTokenExpirationTime = expirationTime;
    }

    /**
     * Method to remove the saved refresh url from the shared preference.
     */
    private void removedSavedRefreshUrl() {
        LocalPreferences.removeRefreshURL();
    }

    public static Boolean isSwiperConnected(){
        return mSwiperConnected;
    }

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {
        if(ReaderTypes.MagneticCardReader == readerType){
            mSwiperConnected = true;
        }
    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {
        if(ReaderTypes.MagneticCardReader == readerType){
            mSwiperConnected = false;
        }
    }

    @Override
    public void onCardReadSuccess(SecureCreditCard paymentCard) {

    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> reason) {

    }

    @Override
    public void onCardReaderEvent(PPError<CardReaderEvents> e) {

    }

    @Override
    public void onSelectPaymentDecision(List<ChipAndPinDecisionEvent> decisionEventList) {

    }

    @Override
    public void onInvalidListeningPort() {

    }

    /**
     * This async task is meant to invoke the refresh url and fetch a new access token via http.
     */
    private class RefreshTokenTask extends AsyncTask<String, String, String> {

        private String accessToken;
        private String refreshUrl;
        private String expiresIn;

        @Override
        protected String doInBackground(String... params) {

            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0]);
                HttpResponse resp = client.execute(get);
                if (resp.getStatusLine().getStatusCode() != 200) {

                    String responseBody = EntityUtils.toString(resp.getEntity());
                    JSONObject json = new JSONObject(responseBody);
                    // Extract the ticket from the JSON response.
                    this.accessToken = json.getString("access_token");
                    this.refreshUrl = json.getString("refresh_url");
                    this.expiresIn = json.getString("expires_in");
                }
            } catch (Exception e) {
                Log.e(LOG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (CommonUtils.isNullOrEmpty(this.accessToken) || CommonUtils.isNullOrEmpty(this.refreshUrl)) {
                sHandler.sendEmptyMessage(0);
                return;
            }
            // Save the new refresh url.
            saveRefreshUrl(this.refreshUrl);
            saveAccessToken(this.accessToken);
            saveTokenExpirationTime(this.expiresIn);
            // Create a new Credentials object with the newly obtained access token.
            Credentials cred = new OAuthCredentials(this.accessToken, this.refreshUrl, this.expiresIn);
            // Set the credentials object within the SDK.
            PayPalHereSDK.setCredentials(cred, new DefaultResponseHandler<Merchant,
                    PPError<MerchantManager.MerchantErrors>>() {

                @Override
                public void onSuccess(Merchant merchant) {
                    // If a success, just send a toast message via the handler.
                    sHandler.sendEmptyMessage(1);
                }

                @Override
                public void onError(PPError<MerchantManager.MerchantErrors> merchantErrorsPPError) {
                    // In case of a failure, logout the merchant and ask them to login back again.
                    sHandler.sendEmptyMessage(0);
                }
            });


        }
    }
}
