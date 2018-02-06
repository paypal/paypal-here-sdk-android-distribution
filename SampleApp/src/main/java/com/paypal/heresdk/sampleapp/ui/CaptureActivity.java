package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.InvoicePayment;
import com.paypal.paypalretailsdk.InvoiceStatus;
import com.paypal.paypalretailsdk.RetailInvoice;
import com.paypal.paypalretailsdk.RetailInvoicePayment;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;

/**
 * Created by muozdemir on 1/9/18.
 */

public class CaptureActivity extends Activity
{
  private static final String LOG_TAG = CaptureActivity.class.getSimpleName();
  public static final String INTENT_AUTH_TOTAL_AMOUNT = "TOTAL_AMOUNT";
  public static final String INTENT_CAPTURE_TOTAL_AMOUNT = "CAPTURE_AMOUNT";
  public static final String INTENT_AUTH_ID = "AUTH_ID";
  public static final String INTENT_INVOICE_ID = "INVOICE_ID";

  public static RetailInvoice invoiceForRefundCaptured = null;
  private ProgressDialog mProgressDialog = null;

  BigDecimal authAmount;
  BigDecimal captureAmount;
  String authId;
  String invoiceId;
  String captureId;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.capture_activity);

    Intent intent = getIntent();
    authAmount = new BigDecimal(0.0);
    if (intent.hasExtra(INTENT_AUTH_TOTAL_AMOUNT))
    {
      authAmount = (BigDecimal) intent.getSerializableExtra(INTENT_AUTH_TOTAL_AMOUNT);
      authId = (String) intent.getSerializableExtra(INTENT_AUTH_ID);
      invoiceId = (String) intent.getSerializableExtra(INTENT_INVOICE_ID);
      Log.d(LOG_TAG, "onCreate amount:" + authAmount);
      final TextView txtAmount = (TextView) findViewById(R.id.amount);
      //txtAmount.setText(authAmount.toString());
    }


  }

  public void onCaptureClicked(View view)
  {
    showProcessingProgressbar();
    EditText amountEditText = (EditText) findViewById(R.id.amount);
    String amountText = amountEditText.getText().toString();
    captureAmount = BigDecimal.ZERO;
    if (null != amountText && amountText.length() > 0) {
      amountText = String.format("%.2f", Double.parseDouble(amountText));
      captureAmount = new BigDecimal(amountText);
      if (captureAmount.compareTo(BigDecimal.ZERO) == 0)
      {
        showInvalidAmountAlertDialog();
        return;
      }
    }
    else
    {
      showInvalidAmountAlertDialog();
      return;
    }
    Log.d(LOG_TAG, "onCaptureClicked capture amount:" + captureAmount);

    BigDecimal gratuity = BigDecimal.ZERO;
    if (captureAmount.compareTo(authAmount) > 0)
    {
      gratuity = captureAmount.subtract(authAmount);
    }
    Log.d(LOG_TAG, "onCaptureClicked gratuity amount:" + gratuity);


    RetailSDK.getTransactionManager().captureAuthorization(authId, invoiceId, captureAmount, gratuity, "USD", new TransactionManager.CaptureAuthorizedTransactionCallback()
    {
      @Override
      public void captureAuthorizedTransaction(final RetailSDKException error, final String captureId)
      {
        CaptureActivity.this.runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            cancelProgressbar();
            if (error != null)
            {

              if (error.getDeveloperMessage() == null || error.getDeveloperMessage().isEmpty())
              {
                Log.d(LOG_TAG, "void capture error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "void capture error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
              }
              else
              {
                Log.d(LOG_TAG, "void capture error: " + error.getDeveloperMessage());
                Toast.makeText(getApplicationContext(), "void capture error: " + error.getDeveloperMessage(), Toast.LENGTH_SHORT).show();
              }
            }
            else
            {
              CaptureActivity.this.captureId = captureId;
              Log.d(LOG_TAG, captureId + " captured.");
              Toast.makeText(getApplicationContext(), captureId + " captured ", Toast.LENGTH_SHORT).show();
              goToRefundActivity();
            }
          }
        });
      }
    });
  }

  public void goToRefundActivity(){
    Log.d(LOG_TAG, "goToRefundActivity");
    //Get the refund data
    invoiceForRefundCaptured = new RetailInvoice(null);
    invoiceForRefundCaptured.setStatus(InvoiceStatus.PAID);

    RetailInvoicePayment invoicePayment = new RetailInvoicePayment();
    invoicePayment.setTransactionID(captureId);

    List<InvoicePayment> list = new ArrayList<>();
    list.add(invoicePayment);
    invoiceForRefundCaptured.setPayments(list);

    RefundActivity.invoiceForRefundCaptured = invoiceForRefundCaptured;
    Intent refundIntent = new Intent(CaptureActivity.this, RefundActivity.class);
    Log.d(LOG_TAG, "goToRefundActivity total: " + captureAmount);
    refundIntent.putExtra(INTENT_CAPTURE_TOTAL_AMOUNT, captureAmount);
    refundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(refundIntent);
  }

  public void goBackToChargeActivity()
  {
    Log.d(LOG_TAG, "goToChargeActivity");
    Intent intent = new Intent(CaptureActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

  private void showInvalidAmountAlertDialog(){
    Log.d(LOG_TAG, "showInvalidAmountAlertDialog");
    AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
    builder.setTitle(R.string.error_title);
    builder.setMessage(R.string.error_invalid_amount);
    builder.setCancelable(false);
    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_TAG, "takePayment invalid amount alert dialog onClick");
        dialog.dismiss();
      }
    });
    builder.create().show();
  }

  private void showProcessingProgressbar()
  {
    mProgressDialog = new ProgressDialog(CaptureActivity.this);
    mProgressDialog.setMessage(getString(R.string.capturing_msg));
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
