package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.login.LocalPreferences;
import com.paypal.heresdk.sampleapp.login.LoginActivity;

/**
 * Created by muozdemir on 1/17/18.
 */

public class WebViewActivity extends Activity
{
  private static final String LOG_TAG = WebViewActivity.class.getSimpleName();
  public static final String INTENT_URL_WEBVIEW = "URL_FOR_WEBVIEW";
  public static final String INTENT_ISLIVE_WEBVIEW = "ISLIVE_FOR_WEBVIEW";

  String mUrl;
  boolean mIsLive;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.webview_activity);

    Intent intent = getIntent();

    if (intent.hasExtra(INTENT_URL_WEBVIEW))
    {
      mUrl = (String) intent.getSerializableExtra(INTENT_URL_WEBVIEW);
      mIsLive = (boolean) intent.getSerializableExtra(INTENT_ISLIVE_WEBVIEW);
      Log.d(LOG_TAG, "onCreate url:" + mUrl + " isLive: " + mIsLive);
      startWebView();
    }
  }

  private void startWebView()
  {
    Log.d(LOG_TAG, "startWebView url: " + mUrl + " isLive: " + mIsLive);

    WebView webView = (WebView) findViewById(R.id.id_webView);
    webView.setVisibility(View.VISIBLE);

    webView.getSettings().setJavaScriptEnabled(true);
    webView.requestFocus(View.FOCUS_DOWN);
    webView.setWebViewClient(new WebViewClient()
    {
      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        Log.d(LOG_TAG, "shouldOverrideURLLoading: url: " + url);
        String returnStringCheckParam = "retailsdksampleapp://oauth?sdk_token=";
        Intent returnIntent = new Intent();
        if (null != url && url.startsWith(returnStringCheckParam))
        {
          String compositeToken = url.substring(returnStringCheckParam.length());
          Log.d(LOG_TAG, "shouldOverrideURLLoading compositeToken: " + compositeToken);
          if (mIsLive)
          {
            LocalPreferences.storeLiveMidTierToken(WebViewActivity.this, compositeToken);
            // startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Live);
            //initializeMerchant(compositeToken, SW_REPOSITORY);
          }
          else
          {
            LocalPreferences.storeSandboxMidTierToken(WebViewActivity.this, compositeToken);
            //startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Sandbox);
            //initializeMerchant(compositeToken, SW_REPOSITORY);
          }
          returnIntent.putExtra("result",compositeToken);
          setResult(Activity.RESULT_OK,returnIntent);
          finish();
          return true;
        }
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
        return false;
      }
    });

    webView.loadUrl(mUrl);
  }

}
