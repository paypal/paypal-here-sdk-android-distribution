package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.login.LoginActivity;
import com.paypal.paypalretailsdk.DeviceUpdate;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.PaymentDevice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionBeginOptions;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;

@EActivity
public class ChargeActivity extends Activity
{
    private static final String LOG_TAG = ChargeActivity.class.getSimpleName();
    public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
    public static final String INTENT_AUTH_ID = "AUTH_ID";
    public static final String INTENT_INVOICE_ID = "INVOICE_ID";

    TransactionContext currentTransaction;
    Invoice currentInvoice;
    Invoice invoiceForRefund;

    @ViewById
    RadioButton radioAuthCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.transaction_activity);

        radioAuthCapture = ((RadioButton) findViewById(R.id.radioAuthCapture));
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed");
        goBackToLoginActivity(null);
    }

    public void onCreateInvoiceViewCodeClicked(View view)
    {
        final TextView txtViewCode = (TextView) findViewById(R.id.txtCreateInvoiceCode);

        if (txtViewCode.getVisibility() == View.GONE)
        {
            txtViewCode.setVisibility(View.VISIBLE);
        } else
        {
            txtViewCode.setVisibility(View.GONE);
        }

    }
    public void onCreateTranxViewCodeClicked(View view)
    {
        final TextView txtViewCode = (TextView) findViewById(R.id.txtCreateTranxCode);

        if (txtViewCode.getVisibility() == View.GONE)
        {
            txtViewCode.setVisibility(View.VISIBLE);
        } else
        {
            txtViewCode.setVisibility(View.GONE);
        }
    }

    public void onViewCodeAcceptTranxClicked(View view)
    {
      final TextView txtViewCode = (TextView) findViewById(R.id.txtAcceptTransactionCode);

      if (txtViewCode.getVisibility() == View.GONE)
      {
        txtViewCode.setVisibility(View.VISIBLE);
      } else
      {
        txtViewCode.setVisibility(View.GONE);
      }
    }

    public void onCreateInvoiceClicked(View view)
    {
        Log.d(LOG_TAG, "onCreateInvoiceClicked");
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        EditText amountEditText = (EditText) findViewById(R.id.amount);
        String amountText = amountEditText.getText().toString();
        BigDecimal amount = BigDecimal.ZERO;
        if (null != amountText && amountText.length() > 0) {
            amountText = String.format("%.2f", Double.parseDouble(amountText));
            amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) == 0)
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
        Log.d(LOG_TAG, "onCreateInvoiceClicked amount:" + amount);

        currentInvoice = new Invoice(null);
        BigDecimal quantity = new BigDecimal(1);
        currentInvoice.addItem("Item", quantity, amount, 1, null);
        // BigDecimal gratuityAmt = new BigDecimal(gratuityField.getText().toString());
        // if(gratuityAmt.intValue() > 0){
        //    invoice.setGratuityAmount(gratuityAmt);
        // }

        final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButton1);
        final TextView txtCreateInvoice = (TextView) findViewById(R.id.txtCreateInvoice);
        final TextView txtCreateTranx = (TextView) findViewById(R.id.txtCreateTransaction);

        imgView.setImageResource(R.drawable.small_greenarrow);
        imgView.setClickable(false);
        txtCreateInvoice.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
        txtCreateInvoice.setClickable(false);

        txtCreateTranx.setClickable(true);
        txtCreateTranx.setTextColor(getResources().getColor(R.color.sdk_blue));
    }


    public void onCreateTransactionClicked(View view)
    {
        Log.d(LOG_TAG, "onCreateTransactionClicked");
        final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButton2);
        final TextView txtCreateTranx = (TextView) findViewById(R.id.txtCreateTransaction);
        final TextView txtAcceptTranx = (TextView) findViewById(R.id.txtAcceptTransaction);

        RetailSDK.getTransactionManager().createTransaction(currentInvoice, new TransactionManager.TransactionCallback()
        {
            @Override
            public void transaction(RetailSDKException e, final TransactionContext context)
            {
                if (e != null) {
                    final String errorTxt = e.toString();
                    ChargeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "create transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    ChargeActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            currentTransaction = context;

                            imgView.setImageResource(R.drawable.small_greenarrow);
                            imgView.setClickable(false);
                            txtCreateTranx.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
                            txtCreateTranx.setClickable(false);

                            txtAcceptTranx.setClickable(true);
                            txtAcceptTranx.setTextColor(getResources().getColor(R.color.sdk_blue));
                        }
                    });
                }
            }
        });
    }

    public void onAcceptTransactionClicked(View view)
    {
        Log.d(LOG_TAG, "onAcceptTransactionClicked");

        PaymentDevice activeDevice = RetailSDK.getDeviceManager().getActiveReader();
        DeviceUpdate deviceUpdate = activeDevice.getPendingUpdate();
        if (deviceUpdate != null && deviceUpdate.getIsRequired() && !deviceUpdate.getWasInstalled())
        {
            deviceUpdate.offer(new DeviceUpdate.CompletedCallback()
            {
                @Override
                public void completed(RetailSDKException e, Boolean aBoolean)
                {
                    Log.d(LOG_TAG, "device update completed");
                    ChargeActivity.this.beginPayment();
                }
            });

        }
        else {
            beginPayment();
        }
    }

    private void beginPayment()
    {
        currentTransaction.setCompletedHandler(new TransactionContext.TransactionCompletedCallback() {
            @Override
            public void transactionCompleted(RetailSDKException error, TransactionRecord record) {
                ChargeActivity.this.transactionCompleted(error, record);
            }
        });

        TransactionBeginOptions options = new TransactionBeginOptions();
        options.setShowPromptInCardReader(true);
        options.setShowPromptInApp(true);
        options.setIsAuthCapture(radioAuthCapture.isChecked());
        currentTransaction.beginPayment(options);
    }

    void transactionCompleted(RetailSDKException error, final TransactionRecord record) {
        if (error != null) {
            final String errorTxt = error.toString();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                    //refundButton.setEnabled(false);
                }
            });
        } else {
            invoiceForRefund = currentTransaction.getInvoice();
            final String recordTxt =  record.getTransactionNumber();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (radioAuthCapture.isChecked()) {
                        goToAuthCaptureActivity(record);
                    }
                    else
                    {
                        goToRefundActivity();
                    }
                    Toast.makeText(getApplicationContext(), String.format("Completed Transaction %s", recordTxt), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void goToAuthCaptureActivity(TransactionRecord record){
        Log.d(LOG_TAG, "goToAuthCaptureActivity");
        AuthCaptureActivity.invoiceForRefund = invoiceForRefund;
        Intent intent = new Intent(ChargeActivity.this, AuthCaptureActivity.class);
        String authId = record.getTransactionNumber();
        String invoiceId = record.getInvoiceId();
        BigDecimal amount = currentInvoice.getTotal();
        Log.d(LOG_TAG, "goToAuthCaptureActivity total: " + amount);
        intent.putExtra(INTENT_TRANX_TOTAL_AMOUNT, amount);
        intent.putExtra(INTENT_AUTH_ID, authId);
        intent.putExtra(INTENT_INVOICE_ID, invoiceId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToRefundActivity(){
        Log.d(LOG_TAG, "goToRefundActivity");
        RefundActivity.invoiceForRefund = invoiceForRefund;
        Intent refundIntent = new Intent(ChargeActivity.this, RefundActivity.class);
        BigDecimal amount = currentInvoice.getTotal();
        Log.d(LOG_TAG, "goToRefundActivity total: " + amount);
        refundIntent.putExtra(INTENT_TRANX_TOTAL_AMOUNT, amount);
        refundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(refundIntent);
    }


    public void goBackToLoginActivity(View view){
        Log.d(LOG_TAG, "goBackToLoginActivity");
        RetailSDK.logout();
        Intent loginIntent = new Intent(ChargeActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    private void showInvalidAmountAlertDialog(){
        Log.d(LOG_TAG, "showInvalidAmountAlertDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(ChargeActivity.this);
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
}
