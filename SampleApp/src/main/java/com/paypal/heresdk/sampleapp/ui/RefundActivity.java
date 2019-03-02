package com.paypal.heresdk.sampleapp.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.InvoicePaymentMethod;
import com.paypal.paypalretailsdk.RetailInvoice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;

/**
 * Created by muozdemir on 12/19/17.
 */

public class RefundActivity extends ToolbarActivity {
    private static final String LOG_TAG = RefundActivity.class.getSimpleName();
    public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
    public static final String INTENT_CAPTURE_TOTAL_AMOUNT = "CAPTURE_AMOUNT";

    public static Invoice invoiceForRefund = null;
    public static RetailInvoice invoiceForRefundCaptured = null;

    BigDecimal currentAmount;
    boolean isCaptured = false;
    private StepView issueRefundStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setUpData();
        setUpDefaultView();
    }

    public void setUpData(){
        Intent intent = getIntent();
        currentAmount = new BigDecimal(0.0);
        if (intent.hasExtra(INTENT_TRANX_TOTAL_AMOUNT)) {
            isCaptured = false;
            currentAmount = (BigDecimal) intent.getSerializableExtra(INTENT_TRANX_TOTAL_AMOUNT);
            Log.d(LOG_TAG, "onCreate amount:" + currentAmount);
            final TextView txtAmount = (TextView) findViewById(R.id.amount);
            txtAmount.setText("Your payment of " + currencyFormat(currentAmount) + " was successful");
        } else if (intent.hasExtra(INTENT_CAPTURE_TOTAL_AMOUNT)) {
            isCaptured = true;
            currentAmount = (BigDecimal) intent.getSerializableExtra(INTENT_CAPTURE_TOTAL_AMOUNT);
            Log.d(LOG_TAG, "onCreate captur amount:" + currentAmount);
            final TextView txtAmount = (TextView) findViewById(R.id.amount);
            txtAmount.setText("Your payment of " + currencyFormat(currentAmount) + " was successfully captured.");
        }
    }

    public void onProvideRefundClicked() {
        RetailSDK.setCurrentApplicationActivity(this);

        String invoiceId;
        String transactionNumber;
        InvoicePaymentMethod paymentMethod;

        if (isCaptured) {
            invoiceId = invoiceForRefundCaptured.getPayPalId();
            transactionNumber = invoiceForRefundCaptured.getPayments().get(0).getTransactionID();
            paymentMethod = invoiceForRefundCaptured.getPayments().get(0).getMethod();
        } else {
            invoiceId = invoiceForRefund.getPayPalId();
            transactionNumber = invoiceForRefund.getPayments().get(0).getTransactionID();
            paymentMethod = invoiceForRefund.getPayments().get(0).getMethod();
        }

        // Create a transactionContext here for Refund
        RetailSDK.getTransactionManager().createRefundTransaction(invoiceId, transactionNumber, paymentMethod, new TransactionManager.TransactionCallback() {
            @Override
            public void transaction(RetailSDKException error, TransactionContext transactionContext) {
                if (error == null) {
                    RefundActivity.this.refundHandler(transactionContext);
                } else {
                    Log.e(LOG_TAG, "Failed to create a refund transactionContext for refund due to error: " + error.toString());
                }
            }
        });
    }

    private void refundHandler(TransactionContext transactionContext) {
        // This is the callback that will be called after the Refund call is complete.
        // Result of Refund will be in this callback
        transactionContext.setCompletedHandler(new TransactionContext.TransactionCompletedCallback() {
            @Override
            public void transactionCompleted(RetailSDKException error, TransactionRecord transactionRecord) {
                // Result of Refund -> Your Code here
                // Go back to ChargeActivity -> UI Only
                final String txnNumber = transactionRecord.getTransactionNumber();
                if (error == null) {
                    Log.d(LOG_TAG, "Refund complete for transactionNumber: " + txnNumber);
                    RefundActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), String.format("Refund complete for transactionNumber: %s", txnNumber), Toast.LENGTH_SHORT).show();
                            RefundActivity.this.goBackToChargeActivity();
                        }
                    });
                } else {
                    Log.e(LOG_TAG, "Refund failed for transactionNumber: " + txnNumber);
                    final String errorTxt = error.toString();
                    RefundActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "refund error: " + errorTxt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // This API will BeginRefund -> Need to call this in order to do a refund
        transactionContext.beginRefund(true, currentAmount);
    }

    public void onSkipRefundClicked(View view) {
        goBackToChargeActivity();
    }

    // Helper Methods

    public void setUpDefaultView() {
        issueRefundStep = (StepView) findViewById(R.id.refund_step);
        issueRefundStep.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProvideRefundClicked();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBackToChargeActivity();
        }
        return true;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.refund_activity;
    }

    public static String currencyFormat(BigDecimal n) {
        return NumberFormat.getCurrencyInstance().format(n);
    }

    public void goBackToChargeActivity() {
        Log.d(LOG_TAG, "goToChargeActivity");
        Intent intent = new Intent(RefundActivity.this, ChargeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
