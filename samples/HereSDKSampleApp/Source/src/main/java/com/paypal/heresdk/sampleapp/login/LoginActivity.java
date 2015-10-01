package com.paypal.heresdk.sampleapp.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapper;
import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapperCallbacks;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.ui.PaymentOptionsActivity;
import com.paypal.merchant.sdk.PayPalHereSDK;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends Activity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private static final String MID_TIER_URL_FOR_LIVE = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/live";
    private static final String MID_TIER_URL_FOR_SANDBOX = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/sandbox";

    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_login);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }

    public void onLoginButtonClicked(View view) {
        RadioButton sandboxButton = (RadioButton) findViewById(R.id.id_sandbox);
        RadioButton liveButton = (RadioButton) findViewById(R.id.id_live);

        if (sandboxButton.isChecked()) {
            /* If User selected Sandbox environment then we need to check if we already have sandbox token form mid tier server or not.
             * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
             */

            String token = LocalPreferences.getSandboxMidtierToken(LoginActivity.this);
            if (null == token) {
                startWebView(MID_TIER_URL_FOR_SANDBOX, true, false);
            } else {
                Log.d(LOG_TAG, "onLoginButtonClicked looks like we have sandbox token. Starting payment options activity");
                startPaymentOptionsActivity(token, PayPalHereSDK.Sandbox);
            }
        } else {
            /* If User selected Live environment then we need to check if we already have live token form mid tier server or not.
             * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
             */

            String token = LocalPreferences.getLiveMidtierToken(LoginActivity.this);
            if (null == token) {
                startWebView(MID_TIER_URL_FOR_LIVE, false, true);
            } else {
                Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live token. Starting payment options activity");
                startPaymentOptionsActivity(token, PayPalHereSDK.Live);
            }
        }
    }

    private void startWebView(String url, final boolean isSandBox, final boolean isLive) {
        Log.d(LOG_TAG, "startWebView url: " + url + " isSandbox: " + isSandBox + " isLive: " + isLive);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.id_main_layout);
        mainLayout.setVisibility(View.GONE);

        WebView webView = (WebView) findViewById(R.id.id_webView);
        webView.setVisibility(View.VISIBLE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(LOG_TAG, "shouldOverrideURLLoading: url: " + url);
                String returnStringCheckParam = "retailsdksampleapp://oauth?";
                if (null != url && url.startsWith(returnStringCheckParam)) {
                    String compositeToken = url.substring(returnStringCheckParam.length());
                    Log.d(LOG_TAG, "shouldOverrideURLLoading compositeToken: " + compositeToken);
                    if (isSandBox) {
                        LocalPreferences.storeSandboxMidTierToken(LoginActivity.this, compositeToken);
                        startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Sandbox);
                    } else if (isLive) {
                        LocalPreferences.storeLiveMidTierToken(LoginActivity.this, compositeToken);
                        startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Live);
                    }
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(url);
    }

    private void startPaymentOptionsActivity(String token, String serverName) {
        Log.d(LOG_TAG, "startPaymentOptionsActivity serverName: " + serverName);

        if (null != token && null != serverName) {
            showProcessingProgressbar();
            PayPalHereSDKWrapper.getInstance().initializeSDK(getApplicationContext(), serverName, token, new PayPalHereSDKWrapperCallbacks() {
                @Override
                public void onErrorWhileSettingAccessTokenToSDK() {
                    Log.d(LOG_TAG, "PayPalHere SDK initialize onErrorWhileSettingAccessTokenToSDK");
                    cancelProgressbar();
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(R.string.error_title);
                    builder.setMessage(R.string.error_initialize_msg);
                    builder.setCancelable(false);
                    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(LOG_TAG, "PayPalHere SDK Initialize error AlertDialog onClick");
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.show();
                }

                @Override
                public void onSuccessfulCompletionOfSettingAccessTokenToSDK() {
                    Log.d(LOG_TAG, "PayPalHere SDK initialize onSuccessfulCompletionOfSettingAccessTokenToSDK");
                    cancelProgressbar();

                    Intent paymentOptionsIntent = new Intent(LoginActivity.this, PaymentOptionsActivity.class);
                    paymentOptionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(paymentOptionsIntent);
                }
            });
        }
    }

    private void showProcessingProgressbar() {
        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mProgressDialog.setMessage(getString(R.string.initializing_processing_msg));
        mProgressDialog.show();
    }

    private void cancelProgressbar() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;

        }
    }
}
