package com.paypal.heresdk.sampleapp.login;

import java.net.URI;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.ui.ReaderConnectionActivity;
import com.paypal.paypalretailsdk.Merchant;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.SdkCredential;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends Activity
{
  private static final String LOG_TAG = LoginActivity.class.getSimpleName();
  public static final String PREFS_NAME = "SDKSampleAppPreferences";
  public static final String PREF_TOKEN_KEY_NAME = "lastToken";
  // private static final String MID_TIER_URL_FOR_LIVE = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/live";
  private static final String MID_TIER_URL_FOR_LIVE = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/live?returnTokenOnQueryString=true";
  // private static final String MID_TIER_URL_FOR_SANDBOX = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/sandbox";
  private static final String MID_TIER_URL_FOR_SANDBOX = "http://pph-retail-sdk-sample.herokuapp.com/toPayPal/sandbox?returnTokenOnQueryString=true";
  private static final String SW_REPOSITORY = "production"; // "production-stage"
  public static final String INTENT_URL_WEBVIEW = "URL_FOR_WEBVIEW";
  public static final String INTENT_ISLIVE_WEBVIEW = "ISLIVE_FOR_WEBVIEW";

  private ProgressDialog mProgressDialog = null;
  private RadioGroup radioGroup1;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.login_activity);

    radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);

    // Checked change Listener for RadioGroup 1
    radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId)
      {
        switch (checkedId)
        {
          case R.id.radioSandbox:
            Toast.makeText(getApplicationContext(), "Sandbox checked", Toast.LENGTH_SHORT).show();
            break;
          case R.id.radioLive:
            Toast.makeText(getApplicationContext(), "Live checked", Toast.LENGTH_SHORT).show();
            break;
          default:
            break;
        }
      }
    });

    final TextView txtInitSDKView = (TextView) findViewById(R.id.txtInitSDK);
    final TextView initSDKCodeView = (TextView) findViewById(R.id.ViewCodeInitSDK);
    final TextView txtInitSDKCodeView = (TextView) findViewById(R.id.txtInitSDKCode);
    final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButton);
    final TextView txtInitMerchantView = (TextView) findViewById(R.id.txtInitMerchant);

    initSDKCodeView.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if (txtInitSDKCodeView.getVisibility() == View.GONE)
        {
          txtInitSDKCodeView.setVisibility(View.VISIBLE);
        }
        else
        {
          txtInitSDKCodeView.setVisibility(View.GONE);
        }
      }
    });

    txtInitSDKView.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        try
        {
          RetailSDK.initialize(getApplicationContext(), new RetailSDK.AppState()
          {
            @Override
            public Activity getCurrentActivity()
            {
              return LoginActivity.this;
            }


            @Override
            public boolean getIsTabletMode()
            {
              return false;
            }
          });
        }
        catch (RetailSDKException e)
        {
          e.printStackTrace();
        }

        imgView.setImageResource(R.drawable.small_greenarrow);
        imgView.setClickable(false);
        txtInitSDKView.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
        txtInitSDKView.setClickable(false);

        txtInitMerchantView.setClickable(true);
        txtInitMerchantView.setTextColor(getResources().getColor(R.color.sdk_blue));

      }
    });

    imgView.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View arg0)
      {
        imgView.setImageResource(R.drawable.small_greenarrow);
        imgView.setClickable(false);
        txtInitSDKView.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
        txtInitSDKView.setClickable(false);
        Toast.makeText(getApplicationContext(), "Clicked Second Image",
                       Toast.LENGTH_SHORT).show();
      }
    });

  }


  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
    Log.d(LOG_TAG, "onConfigurationChanged");
  }


  public void onViewCodeInitMerchantClicked(View view)
  {
    final TextView txtInitMerchantCodeView = (TextView) findViewById(R.id.txtInitMerchantCode);

    if (txtInitMerchantCodeView.getVisibility() == View.GONE)
    {
      txtInitMerchantCodeView.setVisibility(View.VISIBLE);
    }
    else
    {
      txtInitMerchantCodeView.setVisibility(View.GONE);
    }
  }

  public void onInitMerchantClicked(View view)
  {
    RadioButton sandboxButton = (RadioButton) findViewById(R.id.radioSandbox);
    RadioButton liveButton = (RadioButton) findViewById(R.id.radioLive);

    if (sandboxButton.isChecked())
    {
      /*If User selected Sandbox environment then we need to check if we already have sandbox token form mid tier server or not.
       * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
       */
      //String token = LocalPreferences.getSandboxMidtierToken(LoginActivity.this);
      String accessToken = LocalPreferences.getSandboxMidtierAccessToken(LoginActivity.this);
      String refreshUrl = LocalPreferences.getSandboxMidtierRefreshUrl(LoginActivity.this);
      String env = LocalPreferences.getSandboxMidtierEnv(LoginActivity.this);
      if (null == accessToken || null == refreshUrl || null == env)
      {
        startWebView(MID_TIER_URL_FOR_SANDBOX, true, false);
      }
      else
      {
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have sandbox access token: " + accessToken);
        //initializeMerchant(token, SW_REPOSITORY);
        SdkCredential credential = new SdkCredential(env, accessToken);
        credential.setTokenRefreshCredentials(refreshUrl);
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live token. Starting payment options activity");
        initializeMerchant(credential);

      }
    }
    else
    {
      /* If User selected Live environment then we need to check if we already have live token form mid tier server or not.
       * If we don't have the token then we need to start web view, else we can use the token and set it to sdk.
       */
      //String token = LocalPreferences.getLiveMidtierToken(LoginActivity.this);
      String accessToken = LocalPreferences.getLiveMidtierAccessToken(LoginActivity.this);
      String refreshUrl = LocalPreferences.getLiveMidtierRefreshUrl(LoginActivity.this);
      String env = LocalPreferences.getLiveMidtierEnv(LoginActivity.this);

      if (null == accessToken || null == refreshUrl || null == env)
      {
        startWebView(MID_TIER_URL_FOR_LIVE, false, true);
        //hardCodingInitMerchant();
      }
      else
      {
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live access token: " + accessToken);
        SdkCredential credential = new SdkCredential(env, accessToken);
        credential.setTokenRefreshCredentials(refreshUrl);
        Log.d(LOG_TAG, "onLoginButtonClicked looks like we have live token. Starting payment options activity");
        initializeMerchant(credential);
      }
    }
  }

  private void hardCodingInitMerchant()//use this method when using stage rather than live
  {
    Log.d(LOG_TAG, "hard-coding for initializeMerchant()");
    // hard coding the initializeMerchant
    String access_token = "A103.LYafNu2QoAUgEDGshkgLaN9M2xSeTnhTfrFns2HNYdceSa6GO3LhScOMQzdX3TI8.ZeE98NKhHF72Wu-4QNLQ4Rlct8i";
    String env = "stage2d0020";
    Log.d(LOG_TAG, "shouldOverrideURLLoading: access_token: " + access_token);
    Log.d(LOG_TAG, "shouldOverrideURLLoading: env: " + env);
    SdkCredential credential = new SdkCredential(env, access_token);
    // credential.setTokenRefreshCredentials(refresh_url);
    initializeMerchant(credential);
  }

  public void onLogoutClicked(View view)
  {
    RetailSDK.logout();

    // Need to remove tokens from local preferences
    LocalPreferences.storeSandboxMidTierCredentials(LoginActivity.this, null, null, null);
    LocalPreferences.storeLiveMidTierCredentials(LoginActivity.this, null, null, null);

    Toast.makeText(getApplicationContext(), "Logged out! Please initialize Merchant.", Toast.LENGTH_SHORT).show();

    Intent intent = getIntent();
    finish();
    startActivity(intent);
  }


  public void onConnectCardReaderClicked(View view)
  {
    Intent readerConnectionIntent = new Intent(LoginActivity.this, ReaderConnectionActivity.class);
    readerConnectionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(readerConnectionIntent);
  }


  private void startWebView(String url, final boolean isSandBox, final boolean isLive)
  {
    Log.d(LOG_TAG, "startWebView url: " + url + " isSandbox: " + isSandBox + " isLive: " + isLive);

    final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
    mainLayout.setVisibility(View.GONE);

    final WebView webView = (WebView) findViewById(R.id.id_webView);
    webView.setVisibility(View.VISIBLE);

    webView.getSettings().setJavaScriptEnabled(true);
    webView.requestFocus(View.FOCUS_DOWN);
    webView.setWebViewClient(new WebViewClient()
    {
      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        Log.d(LOG_TAG, "shouldOverrideURLLoading: url: " + url);
        //String returnStringCheckParam = "retailsdksampleapp://oauth?sdk_token=";
        String returnStringCheckParam = "retailsdksampleapp://oauth?access_token=";

        // List<NameValuePair> parameters = URLEncodedUtils.parse(new URI(url));
        Uri uri = Uri.parse(url);
        Set<String> paramNames = uri.getQueryParameterNames();
        for (String key: paramNames) {
          String value = uri.getQueryParameter(key);
          Log.d(LOG_TAG, "shouldOverrideURLLoading: name: " + key + " value: " + value);
        }

        if (null != url && url.startsWith(returnStringCheckParam))
        {
          if (paramNames.contains("access_token") && paramNames.contains("refresh_url") && paramNames.contains("env"))
          {
            String access_token = uri.getQueryParameter("access_token");
            String refresh_url = uri.getQueryParameter("refresh_url");
            String env = uri.getQueryParameter("env");
            Log.d(LOG_TAG, "shouldOverrideURLLoading: access_token: " + access_token);
            Log.d(LOG_TAG, "shouldOverrideURLLoading: refresh_url: " + refresh_url);
            Log.d(LOG_TAG, "shouldOverrideURLLoading: env: " + env);
            SdkCredential credential = new SdkCredential(env, access_token);
            credential.setTokenRefreshCredentials(refresh_url);
            //String compositeToken = url.substring(returnStringCheckParam.length());
            //Log.d(LOG_TAG, "shouldOverrideURLLoading compositeToken: " + compositeToken);
            if (isSandBox)
            {
              //LocalPreferences.storeSandboxMidTierToken(LoginActivity.this, compositeToken);
              //startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Sandbox);
              //initializeMerchant(compositeToken, SW_REPOSITORY);
              LocalPreferences.storeSandboxMidTierCredentials(LoginActivity.this, access_token, refresh_url, env);
              initializeMerchant(credential);
            }
            else if (isLive)
            {
              LocalPreferences.storeLiveMidTierCredentials(LoginActivity.this, access_token, refresh_url, env);
              //LocalPreferences.storeLiveMidTierToken(LoginActivity.this, compositeToken);
              // startPaymentOptionsActivity(compositeToken, PayPalHereSDK.Live);
              initializeMerchant(credential);
            }
            webView.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            return true;
          }
        }
        return false;
      }
    });

    webView.loadUrl(url);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == 1) {
      if(resultCode == Activity.RESULT_OK){
        String result = data.getStringExtra("result");
        Log.d(LOG_TAG, "onActivityResult result: " + result);
      }
      if (resultCode == Activity.RESULT_CANCELED) {
        Log.d(LOG_TAG, "onActivityResult RESULT_CANCELED! ");
        //Write your code if there's no result
      }
    }
  }

  private void initializeMerchant(final String token, String repository)
  {
    Log.d(LOG_TAG, "initializeMerchant token: " + token);
    Log.d(LOG_TAG, "initializeMerchant serverName: " + repository);

    try
    {
      showProcessingProgressbar();
      RetailSDK.initializeMerchant(token, repository, new RetailSDK.MerchantInitializedCallback()
      {
        @Override
        public void merchantInitialized(RetailSDKException error, Merchant merchant)
        {
          saveToken(token);
          LoginActivity.this.merchantReady(error, merchant);
        }
      });
    }
    catch (Exception x)
    {
      try
      {
        Log.e(LOG_TAG, "exception: " + x.toString());
        //statusText.setText(x.toString());
      }
      catch (Exception ignore)
      {
        ignore.printStackTrace();
      }
      x.printStackTrace();
    }
  }

  private void initializeMerchant(final SdkCredential credential)
  {
    try
    {
      showProcessingProgressbar();
      RetailSDK.initializeMerchant(credential, new RetailSDK.MerchantInitializedCallback()
      {
        @Override
        public void merchantInitialized(RetailSDKException error, Merchant merchant)
        {
          LoginActivity.this.merchantReady(error, merchant);
        }
      });
    }
    catch (Exception x)
    {
      try
      {
        Log.e(LOG_TAG, "exception: " + x.toString());
        //statusText.setText(x.toString());
      }
      catch (Exception ignore)
      {
        ignore.printStackTrace();
      }
      x.printStackTrace();
    }
  }


  void merchantReady(RetailSDKException error, final Merchant merchant)
  {
    if (error == null)
    {
      LoginActivity.this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          // Add the BN code for Partner tracking. To obtain this value, contact
          // your PayPal account representative. Please do not change this value when
          // using this sample app for testing.
          merchant.setReferrerCode("PPHSDK_SampleApp_Android");

          Log.d(LOG_TAG, "merchantReady without any error");
          cancelProgressbar();

          final TextView txtMerchantEmail = (TextView) findViewById(R.id.merchant_email);
          txtMerchantEmail.setText(merchant.getEmailAddress());

          final TextView txtInitMerchantView = (TextView) findViewById(R.id.txtInitMerchant);
          txtInitMerchantView.setClickable(false);
          txtInitMerchantView.setTextColor(getResources().getColor(R.color.sdk_dark_gray));

          final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButton2);
          imgView.setImageResource(R.drawable.small_greenarrow);
          imgView.setClickable(false);

          final LinearLayout lnLayout = (LinearLayout) findViewById(R.id.logout);
          lnLayout.setVisibility(View.VISIBLE);

          final LinearLayout btmLayout = (LinearLayout) findViewById(R.id.bottomBanner);
          btmLayout.setVisibility(View.VISIBLE);
        }
      });
    }
    else
    {
      Log.d(LOG_TAG, "RetailSDK initialize on Error:" + error.toString());
      cancelProgressbar();
      AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
      builder.setTitle(R.string.error_title);
      builder.setMessage(R.string.error_initialize_msg);
      builder.setCancelable(false);
      builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          Log.d(LOG_TAG, "RetailSDK Initialize error AlertDialog onClick");
          dialog.dismiss();
          finish();
        }
      });
      builder.show();
    }
  }


  private void saveToken(String token)
  {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(PREF_TOKEN_KEY_NAME, token);
    editor.commit();
  }


  private void showProcessingProgressbar()
  {
    mProgressDialog = new ProgressDialog(LoginActivity.this);
    mProgressDialog.setMessage(getString(R.string.initializing_processing_msg));
    mProgressDialog.show();
  }


  private void cancelProgressbar()
  {
    if (null != mProgressDialog && mProgressDialog.isShowing())
    {
      mProgressDialog.dismiss();
      mProgressDialog = null;

    }
  }
}
