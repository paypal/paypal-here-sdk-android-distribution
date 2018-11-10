package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.RetailSDK;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.ViewById;

public class PaymentOptionsActivity extends ToolbarActivity
{
  final private String logComponent = "PaymentOptionsActivity";
  @ViewById
  Switch authCaptureSwitch;
  @ViewById
  Switch vaultSwitch;
  @ViewById
  Switch promptAppSwitch;
  @ViewById
  Switch promptReaderSwitch;
  @ViewById
  Switch amountTippingSwitch;
  @ViewById
  Switch enableQuickChipSwitch;
  @ViewById
  Switch readerTipSwitch;

  @ViewById
  EditText tagTxt;

  @ViewById
  CheckBox chipBox;
  @ViewById
  CheckBox contactlessBox;
  @ViewById
  CheckBox magneticSwipeBox;
  @ViewById
  CheckBox manualCardBox;
  @ViewById
  CheckBox secureManualBox;

  @ViewById
  StepView btLogin;
  @ViewById
  WebView btWebView;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_options_dialog);

    authCaptureSwitch = (Switch) findViewById(R.id.auth_capture_switch);
    promptReaderSwitch = (Switch) findViewById(R.id.show_prompt_card_reader_switch);
    promptAppSwitch = (Switch) findViewById(R.id.show_prompt_app_switch);
    readerTipSwitch = (Switch) findViewById(R.id.tipping_reader_switch);
    amountTippingSwitch = (Switch) findViewById(R.id.amount_tipping_switch);
    enableQuickChipSwitch = (Switch) findViewById(R.id.enable_quick_chip_switch);
    vaultSwitch = (Switch) findViewById(R.id.vault_switch);

    tagTxt = (EditText) findViewById(R.id.tag);

    magneticSwipeBox = (CheckBox) findViewById(R.id.magnetic_swipe);
    chipBox = (CheckBox) findViewById(R.id.chip);
    contactlessBox = (CheckBox) findViewById(R.id.contactless);
    secureManualBox = (CheckBox) findViewById(R.id.secure_manual);
    manualCardBox = (CheckBox) findViewById(R.id.manual_card);

    btLogin = (StepView) findViewById(R.id.bt_login);
    btWebView = (WebView) findViewById(R.id.btWebView);

    Bundle options = getIntent().getExtras();
    if (options!=null)
    {
      authCaptureSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_AUTH_CAPTURE));
      vaultSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_VAULT_ONLY));
      promptReaderSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_CARD_READER_PROMPT));
      promptAppSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_APP_PROMPT));
      readerTipSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_TIP_ON_READER));
      amountTippingSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_AMOUNT_TIP));
      enableQuickChipSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_QUICK_CHIP_ENABLED));
      chipBox.setChecked(options.getBoolean(ChargeActivity.OPTION_CHIP));
      magneticSwipeBox.setChecked(options.getBoolean(ChargeActivity.OPTION_MAGNETIC_SWIPE));
      contactlessBox.setChecked(options.getBoolean(ChargeActivity.OPTION_CONTACTLESS));
      secureManualBox.setChecked(options.getBoolean(ChargeActivity.OPTION_SECURE_MANUAL));
      manualCardBox.setChecked(options.getBoolean(ChargeActivity.OPTION_MANUAL_CARD));
      tagTxt.setText(options.getString(ChargeActivity.OPTION_TAG));
    }

    if (vaultSwitch.isChecked()) {
      btLogin.setVisibility(View.VISIBLE);
    } else {
      btLogin.setVisibility(View.GONE);
    }

    vaultSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
      {
        if (isChecked) {
          btLogin.setVisibility(View.VISIBLE);
        } else {
          btLogin.setVisibility(View.GONE);
        }
      }
    });

    btLogin.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        PaymentOptionsActivity.this.btLoginClicked();
      }
    });
  }


  @Click(R.id.bt_login)
  void btLoginClicked()
  {
    String btLoginURL = RetailSDK.getBtLoginUrl(null);

    Log.d(logComponent, "starting BT web view with URL: " + btLoginURL);
    btWebView.setVisibility(View.VISIBLE);
    btWebView.getSettings().setJavaScriptEnabled(true);
    btWebView.requestFocus(View.FOCUS_DOWN);
    btWebView.setWebViewClient(new WebViewClient()
    {
      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        Log.d(logComponent, "this is the overloaded url " + url);
        Log.d(logComponent, "does it contain auth code: " + RetailSDK.isBtReturnUrlValid(url));
        if (RetailSDK.isBtReturnUrlValid(url))
        {
          Log.d(logComponent, "GOOD it contains auth code! ");
          btWebView.setVisibility(View.GONE);
          return true;
        }
        return false;
      }
    });
    btWebView.loadUrl(btLoginURL);
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    hideSoftKeyboard();
  }

  public void onDoneClicked(View view){
    Intent data = new Intent();
    data.putExtras(getOptionsBundle());
    setResult(RESULT_OK,data);
    onBackPressed();
  }

  public void hideSoftKeyboard() {

    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
  }

  @Override
  public int getLayoutResId()
  {
    return R.layout.activity_payment_options;
  }
  private Bundle getOptionsBundle()
  {
    Bundle bundle = new Bundle();
    bundle.putBoolean(ChargeActivity.OPTION_AUTH_CAPTURE,authCaptureSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_VAULT_ONLY,vaultSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_CARD_READER_PROMPT,promptReaderSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_APP_PROMPT,promptAppSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_TIP_ON_READER,readerTipSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_AMOUNT_TIP,amountTippingSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_QUICK_CHIP_ENABLED, enableQuickChipSwitch.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_MAGNETIC_SWIPE,magneticSwipeBox.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_CHIP,chipBox.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_CONTACTLESS,contactlessBox.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_MANUAL_CARD,manualCardBox.isChecked());
    bundle.putBoolean(ChargeActivity.OPTION_SECURE_MANUAL,secureManualBox.isChecked());
    bundle.putString(ChargeActivity.OPTION_TAG,tagTxt.getText().toString());
    return bundle;
  }
}

