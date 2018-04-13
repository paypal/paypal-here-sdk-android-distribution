package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


public class AuthCaptureActivity extends Activity
{
  private static final String LOG_TAG = AuthCaptureActivity.class.getSimpleName();
  public static final String INTENT_AUTH_TOTAL_AMOUNT = "TOTAL_AMOUNT";
  public static final String INTENT_AUTH_ID = "AUTH_ID";
  public static final String INTENT_INVOICE_ID = "INVOICE_ID";

  public static Invoice invoiceForRefund = null;

  BigDecimal authAmount;
  String authId;
  String invoiceId;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.authorization_activity);

    Intent intent = getIntent();
    authAmount = new BigDecimal(0.0);
    if (intent.hasExtra(INTENT_AUTH_TOTAL_AMOUNT))
    {
      authAmount = (BigDecimal) intent.getSerializableExtra(INTENT_AUTH_TOTAL_AMOUNT);
      authId = (String) intent.getSerializableExtra(INTENT_AUTH_ID);
      invoiceId = (String) intent.getSerializableExtra(INTENT_INVOICE_ID);
      Log.d(LOG_TAG, "onCreate amount:" + authAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      txtAmount.setText(currencyFormat(authAmount));
    }

  }


  public void onVoidAuthViewCodeClicked(View view)
  {
    final TextView txtViewCode = (TextView) findViewById(R.id.txtVoidAuthCode);

    if (txtViewCode.getVisibility() == View.GONE)
    {
      txtViewCode.setVisibility(View.VISIBLE);
    }
    else
    {
      txtViewCode.setVisibility(View.GONE);
    }
  }

  public void onCaptureAuthViewCodeClicked(View view)
  {
    final TextView txtViewCode = (TextView) findViewById(R.id.txtCaptureAuthCode);

    if (txtViewCode.getVisibility() == View.GONE)
    {
      txtViewCode.setVisibility(View.VISIBLE);
    }
    else
    {
      txtViewCode.setVisibility(View.GONE);
    }
  }




  public static String currencyFormat(BigDecimal n)
  {
    return NumberFormat.getCurrencyInstance().format(n);
  }


  public void onVoidAuthClicked(View view)
  {

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
              Toast.makeText(getApplicationContext(), authId + " voided ", Toast.LENGTH_SHORT).show();
              final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButtonVoid);
              final TextView txtVoidView = (TextView) findViewById(R.id.txtVoidAuth);
              final TextView txtCaptureView = (TextView) findViewById(R.id.txtCaptureAuth);
              final LinearLayout bottomBammer = (LinearLayout) findViewById(R.id.bottomBanner);

              imgView.setImageResource(R.drawable.small_greenarrow);
              imgView.setClickable(false);
              txtVoidView.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
              txtVoidView.setClickable(false);
              txtCaptureView.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
              txtCaptureView.setClickable(false);
              bottomBammer.setVisibility(View.VISIBLE);
            }
          }
        });
      }
    });
  }

  public void onCaptureAuthClicked(View view)
  {
    Log.d(LOG_TAG, "goToCaptureActivity");
    // CaptureActivity.invoiceForRefund = invoiceForRefund;

    Intent intent = new Intent(AuthCaptureActivity.this, CaptureActivity.class);
    intent.putExtra(INTENT_AUTH_TOTAL_AMOUNT, authAmount);
    intent.putExtra(INTENT_AUTH_ID, authId);
    intent.putExtra(INTENT_INVOICE_ID, invoiceId);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

  public void onRunMoreClicked(View view)
  {
    Log.d(LOG_TAG, "goToChargeActivity");
    Intent intent = new Intent(AuthCaptureActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

}
