package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultType;

public class PaymentOptionsActivity extends ToolbarActivity
{
  final private String logComponent = "PaymentOptionsActivity";
  Switch authCaptureSwitch;
  Switch promptAppSwitch;
  Switch promptReaderSwitch;
  Switch amountTippingSwitch;
  Switch enableQuickChipSwitch;
  Switch readerTipSwitch;
  EditText tagTxt;
  CheckBox chipBox;
  CheckBox contactlessBox;
  CheckBox magneticSwipeBox;
  CheckBox manualCardBox;
  CheckBox secureManualBox;

  LinearLayout ll_customerId;
  EditText customerId;

  RadioGroup radioGroupVault;

  TransactionBeginOptionsVaultType vaultType;

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

    radioGroupVault = (RadioGroup) findViewById(R.id.vaultRadioGroup);
    vaultType = TransactionBeginOptionsVaultType.PayOnly;
    radioGroupVault.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId)
      {
        // find which radio button is selected
        if(checkedId == R.id.payOnlyButton) {
          vaultType = TransactionBeginOptionsVaultType.PayOnly;
        } else if(checkedId == R.id.vaultOnlyButton) {
          vaultType = TransactionBeginOptionsVaultType.VaultOnly;
        } else {
          vaultType = TransactionBeginOptionsVaultType.PayAndVault;
        }
      }
    });


    tagTxt = (EditText) findViewById(R.id.tag);

    magneticSwipeBox = (CheckBox) findViewById(R.id.magnetic_swipe);
    chipBox = (CheckBox) findViewById(R.id.chip);
    contactlessBox = (CheckBox) findViewById(R.id.contactless);
    secureManualBox = (CheckBox) findViewById(R.id.secure_manual);
    manualCardBox = (CheckBox) findViewById(R.id.manual_card);

    ll_customerId = (LinearLayout) findViewById(R.id.ll_customer_id);
    customerId = (EditText) findViewById(R.id.customer_id);

    Bundle options = getIntent().getExtras();
    if (options!=null)
    {
      authCaptureSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_AUTH_CAPTURE));
      // vaultSwitch.setChecked(options.getBoolean(ChargeActivity.OPTION_VAULT_ONLY));
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
      customerId.setText(options.getString(ChargeActivity.OPTION_CUSTOMER_ID));
      tagTxt.setText(options.getString(ChargeActivity.OPTION_TAG));
    }
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
    bundle.putInt(ChargeActivity.OPTION_VAULT_TYPE, vaultType.getValue());
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
    bundle.putString(ChargeActivity.OPTION_CUSTOMER_ID,customerId.getText().toString());
    bundle.putString(ChargeActivity.OPTION_TAG,tagTxt.getText().toString());
    return bundle;
  }
}

