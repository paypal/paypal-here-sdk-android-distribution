package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.paypal.heresdk.sampleapp.R;

/**
 * Created by muozdemir on 12/19/17.
 */

public class EndActivity extends Activity
{
  private static final String LOG_TAG = EndActivity.class.getSimpleName();
  public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.end_activity);

    Intent intent = getIntent();
    if (intent.hasExtra(INTENT_TRANX_TOTAL_AMOUNT))
    {
      BigDecimal amount = (BigDecimal) intent.getSerializableExtra (INTENT_TRANX_TOTAL_AMOUNT);
      Log.d(LOG_TAG, "onCreate amount:" + amount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      txtAmount.setText(currencyFormat(amount));
    }

  }

  public static String currencyFormat(BigDecimal n) {
    return NumberFormat.getCurrencyInstance().format(n);
  }

  public void onRunMoreClicked(View view)
  {
    Intent chargeIntent = new Intent(EndActivity.this, ChargeActivity.class);
    Log.d(LOG_TAG, "goToChargeActivity");
    chargeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(chargeIntent);
  }

}
