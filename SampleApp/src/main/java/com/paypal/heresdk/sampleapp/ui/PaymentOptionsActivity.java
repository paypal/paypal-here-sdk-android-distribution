package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.paypal.heresdk.sampleapp.R;

public class PaymentOptionsActivity extends ToolbarActivity
{

  PaymentOptionsFragment paymentOptionsFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    paymentOptionsFragment = (PaymentOptionsFragment) getSupportFragmentManager().findFragmentById(R.id.options_fragment);
    Bundle options = getIntent().getExtras();
    if (options!=null)
    {
      paymentOptionsFragment.setAuthCapturePreference(options.getBoolean(ChargeActivity.OPTION_AUTH_CAPTURE));
      paymentOptionsFragment.setPromptReaderPreference(options.getBoolean(ChargeActivity.OPTION_CARD_READER_PROMPT));
      paymentOptionsFragment.setPromptAppPreference(options.getBoolean(ChargeActivity.OPTION_APP_PROMPT));
      paymentOptionsFragment.setReaderTipPreference(options.getBoolean(ChargeActivity.OPTION_TIP_ON_READER));
      paymentOptionsFragment.setAmountTippingPreference(options.getBoolean(ChargeActivity.OPTION_AMOUNT_TIP));
      paymentOptionsFragment.setChipPreference(options.getBoolean(ChargeActivity.OPTION_CHIP));
      paymentOptionsFragment.setContactlessPreference(options.getBoolean(ChargeActivity.OPTION_CONTACTLESS));
      paymentOptionsFragment.setMagneticSwipePreference(options.getBoolean(ChargeActivity.OPTION_MAGNETIC_SWIPE));
      paymentOptionsFragment.setManualCardPreference(options.getBoolean(ChargeActivity.OPTION_MANUAL_CARD));
      paymentOptionsFragment.setSecureManualPreference(options.getBoolean(ChargeActivity.OPTION_SECURE_MANUAL));
      paymentOptionsFragment.setTag(options.getString(ChargeActivity.OPTION_TAG));
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
    bundle.putBoolean(ChargeActivity.OPTION_AUTH_CAPTURE,paymentOptionsFragment.getAuthCapturePreference());
    bundle.putBoolean(ChargeActivity.OPTION_CARD_READER_PROMPT,paymentOptionsFragment.getPromptReaderPreference());
    bundle.putBoolean(ChargeActivity.OPTION_APP_PROMPT,paymentOptionsFragment.getPromptAppPreference());
    bundle.putBoolean(ChargeActivity.OPTION_TIP_ON_READER,paymentOptionsFragment.getTipOnReaderPreference());
    bundle.putBoolean(ChargeActivity.OPTION_AMOUNT_TIP,paymentOptionsFragment.getAmountTippingPreference());
    bundle.putBoolean(ChargeActivity.OPTION_MAGNETIC_SWIPE,paymentOptionsFragment.getMagneticSwipePreference());
    bundle.putBoolean(ChargeActivity.OPTION_CHIP,paymentOptionsFragment.getChipPreference());
    bundle.putBoolean(ChargeActivity.OPTION_CONTACTLESS,paymentOptionsFragment.getContactlessPreference());
    bundle.putBoolean(ChargeActivity.OPTION_MANUAL_CARD,paymentOptionsFragment.getManualCardPreference());
    bundle.putBoolean(ChargeActivity.OPTION_SECURE_MANUAL,paymentOptionsFragment.getSecureManualPreference());
    bundle.putString(ChargeActivity.OPTION_TAG,paymentOptionsFragment.getTagString());
    return bundle;
  }
}
