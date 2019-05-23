package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.RetailInvoice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;

/**
 * Created by muozdemir on 11/1/18.
 */

public class VaultActivity extends ToolbarActivity
{
  private static final String LOG_TAG = VaultActivity.class.getSimpleName();
  public static final String INTENT_VAULT_ID = "VAULT_ID";

  StepView goBack;
  String vaultId;

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if(item.getItemId()==android.R.id.home){
      goBackToChargeActivity();

    }
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");

    Intent intent = getIntent();
    if (intent.hasExtra(INTENT_VAULT_ID))
    {
      vaultId = (String) intent.getSerializableExtra(INTENT_VAULT_ID);
      Log.d(LOG_TAG, "onCreate vaultId:" + vaultId);
      final TextView vaultIdTxt = (TextView) findViewById(R.id.vault_id);
      vaultIdTxt.setText("Your vault of " + vaultId +" was successful");
    }

    goBack = (StepView) findViewById(R.id.go_back_step);
    goBack.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        goBackToChargeActivity();
      }
    });
  }


  @Override
  public int getLayoutResId()
  {
    return R.layout.vault_activity;
  }

  public void goBackToChargeActivity()
  {
    Log.d(LOG_TAG, "goToChargeActivity");
    Intent intent = new Intent(VaultActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

}
