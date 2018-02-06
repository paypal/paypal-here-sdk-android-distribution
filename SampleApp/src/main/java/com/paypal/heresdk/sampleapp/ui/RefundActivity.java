package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
 * Created by muozdemir on 12/19/17.
 */

public class RefundActivity extends Activity
{
  private static final String LOG_TAG = RefundActivity.class.getSimpleName();
  public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
  public static final String INTENT_CAPTURE_TOTAL_AMOUNT = "CAPTURE_AMOUNT";


  public static Invoice invoiceForRefund = null;
  public static RetailInvoice invoiceForRefundCaptured = null;

  TransactionContext currentTransaction;
  BigDecimal currentAmount;
  boolean isCaptured = false;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.refund_activity);

    Intent intent = getIntent();
    currentAmount = new BigDecimal(0.0);
    if (intent.hasExtra(INTENT_TRANX_TOTAL_AMOUNT))
    {
      isCaptured = false;
      currentAmount = (BigDecimal) intent.getSerializableExtra(INTENT_TRANX_TOTAL_AMOUNT);
      Log.d(LOG_TAG, "onCreate amount:" + currentAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      txtAmount.setText(currencyFormat(currentAmount));
    }
    else if (intent.hasExtra(INTENT_CAPTURE_TOTAL_AMOUNT))
    {
      isCaptured = true;
      currentAmount = (BigDecimal) intent.getSerializableExtra(INTENT_CAPTURE_TOTAL_AMOUNT);
      Log.d(LOG_TAG, "onCreate captur amount:" + currentAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      txtAmount.setText(currencyFormat(currentAmount));
      final TextView txt5 = (TextView) findViewById(R.id.textView5);
      txt5.setText("was successfully captured.");
    }

  }


  public void onProvideRefundViewCodeClicked(View view)
  {
    final TextView txtViewCode = (TextView) findViewById(R.id.provideRefundCode);

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


  public void onNoThanksClicked(View view)
  {
    goBackToChargeActivity();
  }


  public void onProvideRefundClicked(View view)
  {
    RetailSDK.setCurrentApplicationActivity(this);

    if (isCaptured)
    {
     RetailSDK.getTransactionManager().createTransaction(invoiceForRefundCaptured, new TransactionManager.TransactionCallback()
     {
       @Override
       public void transaction(RetailSDKException error, TransactionContext transactionContext)
       {
         transactionContext.setCompletedHandler(new TransactionContext.TransactionCompletedCallback()
         {
           @Override
           public void transactionCompleted(RetailSDKException error, TransactionRecord record)
           {
             Log.d(LOG_TAG, "createTx for captured -> transactionCompleted");
             RefundActivity.this.refundCompleted(error, record);
           }
         });
         transactionContext.beginRefund(true, currentAmount);
       }
     });

     return;
    }

    currentTransaction = RetailSDK.createTransaction(invoiceForRefund);
    currentTransaction.setCompletedHandler(new TransactionContext.TransactionCompletedCallback()
    {
      @Override
      public void transactionCompleted(RetailSDKException error, TransactionRecord record)
      {
        RefundActivity.this.refundCompleted(error, record);
      }
    });
    currentTransaction.beginRefund(true, currentAmount);
  }


  private void refundCompleted(RetailSDKException error, TransactionRecord record)
  {
    if (error != null)
    {
      final String errorTxt = error.toString();
      this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          Toast.makeText(getApplicationContext(), "refund error: " + errorTxt, Toast.LENGTH_SHORT).show();
        }
      });

    }
    else
    {
      final String txnNumber = record.getTransactionNumber();
      this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          Toast.makeText(getApplicationContext(), String.format("Completed refund for Transaction %s", txnNumber), Toast.LENGTH_SHORT).show();
          RefundActivity.this.goBackToChargeActivity();
        }
      });
    }
  }


  public void goBackToChargeActivity()
  {
    Log.d(LOG_TAG, "goToChargeActivity");
    Intent intent = new Intent(RefundActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

}
