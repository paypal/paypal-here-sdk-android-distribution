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
import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionController;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.credentials.Credentials;
import com.paypal.merchant.sdk.domain.credentials.OauthCredentials;
import com.paypal.sampleapp.util.CommonUtils;

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


public class MyActivity extends Activity {

    public static final String PREFS_NAME = "MerchantPrefs";
    public static final String REFRESH_URL = "refreshUrl";
    private static final String LOG = "MyActivity";
    private static Bitmap sBitmap;
    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();
    private static SharedPreferences settings;
    private static volatile boolean sIsPaymentCompleted;
    private static BluetoothDevice sEmvDevice;
    private static String sAccessToken;
    private static boolean sIsHandledByApp = false;
    /**
     * Implementing the transaction controller that acts as an interceptor for pre and post authorize events.
     */
    protected TransactionController mTransactionController = new TransactionController() {
        @Override
        public TransactionControlAction onPreAuthorize(Invoice invoice,
                                                       String payload) {

            CommonUtils.createToastMessage(MyActivity.this, "OnPreAuthorize!!!");
            if (sIsHandledByApp) {
                TransController controller = new TransController(payload);
                controller.execute();
                return TransactionControlAction.HANDLED;
            }
            return TransactionControlAction.CONTINUE;

        }

        @Override
        public void onPostAuthorize(boolean didFail) {
            CommonUtils.createToastMessage(MyActivity.this, "OnPostAuthorize!!!");
        }
    };
    private static String sBNCode;
    private static String sCashierId;
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

    public static Bitmap getBitmap() {
        return sBitmap;
    }

    public static void setBitmap(Bitmap bm) {
        sBitmap = bm;

    }

    public BluetoothDevice getBTDevice() {
        return sEmvDevice;
    }

    public void setBTDevice(BluetoothDevice btDevice) {
        sEmvDevice = btDevice;
    }

    public void handledByApp(boolean b) {
        sIsHandledByApp = b;
    }

    public boolean isHandledByApp() {
        return sIsHandledByApp;
    }

    public String getBNCode() {
        return sBNCode;
    }

    public void setBNCode(String bnCode) {
        sBNCode = bnCode;
    }

    public String getCashierId() {
        return sCashierId;
    }

    public void setCashierId(String cashierId) {
        sCashierId = cashierId;
    }

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

        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(REFRESH_URL, refreshUrl);
        // Commit the edits!
        editor.commit();

        Log.d("saveRefreshUrl", "Saving the refresh url: " + refreshUrl);
    }

    /**
     * Method to retrieve the refresh url that is saved as a preference within the app.
     *
     * @return : refresh url string.
     */
    public String getRefreshUrl() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(REFRESH_URL, null);
    }

    public void saveAccessToken(String accessToken) {
        sAccessToken = accessToken;
    }

    public String getAccessToken() {
        return sAccessToken;
    }

    /**
     * Method to remove the saved refresh url from the shared preference.
     */
    private void removedSavedRefreshUrl() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings != null)
            settings.edit().remove(REFRESH_URL).commit();

    }

    /**
     * This async task is meant to invoke the refresh url and fetch a new access token via http.
     */
    private class RefreshTokenTask extends AsyncTask<String, String, String> {

        private String accessToken;
        private String refreshUrl;

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
            // Create a new Credentials object with the newly obtained access token.
            Credentials cred = new OauthCredentials(this.accessToken);
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

    private class TransController extends AsyncTask<String, String, String> {

        private String mPayload;

        public TransController(String jsonPayload) {
            mPayload = jsonPayload;

        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append((line + "\n"));
                }
            } catch (IOException e) {
                Log.e("AppSDK", "Exception in response");
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("AppSDK", "Exception in response");
                }
            }
            return sb.toString();
        }

        protected String doInBackground(String... strings) {
            String jsonResultAsString = null;
            HttpClient hc = new DefaultHttpClient();
            String message;

            HttpPost p = new HttpPost("https://www.stage2pph32.stage.paypal.com/webapps/hereapi/merchant/v1/pay");
            try {
                message = mPayload;
                p.setEntity(new StringEntity(message, "UTF8"));
                p.setHeader("Content-type", "application/json");
                p.setHeader("Authorization", "Bearer " + getAccessToken());
                HttpResponse resp = hc.execute(p);
                if (resp != null) {
                    if (resp.getStatusLine().getStatusCode() == 200) {
                        jsonResultAsString = convertStreamToString(resp.getEntity().getContent());
                    }

                }
                Log.d("Status line", "" + resp.getStatusLine().getStatusCode());
            } catch (Exception e) {
                e.printStackTrace();

            }
            return jsonResultAsString;
        }

        protected void onPostExecute(String result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
            builder.setTitle("Payment Response");
            builder.setMessage(result);
            AlertDialog d = builder.create();
            d.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener
                            () {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
            d.show();
        }
    }
}
