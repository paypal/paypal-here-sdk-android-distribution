package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionManager;

/**
 * Created by muozdemir on 1/8/18.
 */


public class AuthCaptureActivity extends ToolbarActivity implements View.OnClickListener
{
  private static final String LOG_TAG = AuthCaptureActivity.class.getSimpleName();
  public static final String INTENT_AUTH_TOTAL_AMOUNT = "TOTAL_AMOUNT";
  public static final String INTENT_AUTH_ID = "AUTH_ID";
  public static final String INTENT_INVOICE_ID = "INVOICE_ID";

  public static Invoice invoiceForRefund = null;

  BigDecimal authAmount;
  String authId;
  String invoiceId;

  private StepView voidAuthStep;
  private StepView captureAuthStep;

  @Override
  public int getLayoutResId()
  {
    return R.layout.authorization_activity;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");

    voidAuthStep = (StepView)findViewById(R.id.void_auth_step);
    captureAuthStep = (StepView)findViewById(R.id.capture_auth_step);
    voidAuthStep.setOnButtonClickListener(this);
    captureAuthStep.setOnButtonClickListener(this);
    Intent intent = getIntent();
    authAmount = new BigDecimal(0.0);
    if (intent.hasExtra(INTENT_AUTH_TOTAL_AMOUNT))
    {
      authAmount = (BigDecimal) intent.getSerializableExtra(INTENT_AUTH_TOTAL_AMOUNT);
      authId = (String) intent.getSerializableExtra(INTENT_AUTH_ID);
      invoiceId = (String) intent.getSerializableExtra(INTENT_INVOICE_ID);
      Log.d(LOG_TAG, "onCreate amount:" + authAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      txtAmount.setText("Your authorization of " + currencyFormat(authAmount) + " was successful");
    }


  }




  public static String currencyFormat(BigDecimal n)
  {
    return NumberFormat.getCurrencyInstance().format(n);
  }


  public void onVoidAuthClicked()
  {
    voidAuthStep.showProgressBar();

    RetailSDK.getTransactionManager().voidAuthorization(authId, new TransactionManager.VoidAuthorizationCallback()
    {
      @Override
      public void voidAuthorization(final RetailSDKException error)
      {
        AuthCaptureActivity.this.runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            if (error != null)
            {

              if (error.getDeveloperMessage() == null || error.getDeveloperMessage().isEmpty())
              {
                Toast.makeText(getApplicationContext(), "void Authorization error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
              }
              else
              {
                Toast.makeText(getApplicationContext(), "void Authorization error: " + error.getDeveloperMessage(), Toast.LENGTH_SHORT).show();
              }
            }
            else
            {
              captureAuthStep.setStepDisabled();
              voidAuthStep.hideProgressBarShowTick();
              Toast.makeText(getApplicationContext(), authId + " voided ", Toast.LENGTH_SHORT).show();
              goToChargeActivity();

            }
          }
        });
      }
    });
  }

  public void onCaptureAuthClicked()
  {
    Log.d(LOG_TAG, "goToCaptureActivity");
    // CaptureActivity.invoiceForRefund = invoiceForRefund;
    CaptureActivity.paymentMethod = invoiceForRefund.getPayments().get(0).getMethod();
    Intent intent = new Intent(AuthCaptureActivity.this, CaptureActivity.class);
    intent.putExtra(INTENT_AUTH_TOTAL_AMOUNT, authAmount);
    intent.putExtra(INTENT_AUTH_ID, authId);
    intent.putExtra(INTENT_INVOICE_ID, invoiceId);
    startActivity(intent);
  }

  public void goToChargeActivity()
  {
    Log.d(LOG_TAG, "goToChargeActivity");
    Intent intent = new Intent(AuthCaptureActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }


  @Override
  public void onClick(View v)
  {
    if (v == voidAuthStep.getButton()){
      onVoidAuthClicked();
    }else if(v == captureAuthStep.getButton()){
      onCaptureAuthClicked();
    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if(item.getItemId()==android.R.id.home){
      goToChargeActivity();
    }
    return true;
  }
}
