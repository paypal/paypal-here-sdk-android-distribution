package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.sampleapp.R;

public class CashActivity extends Activity {

    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);

        Invoice invoice = PayPalHereSDK.getTransactionManager().getActiveInvoice();

        TextView totalAmount = (TextView) findViewById(R.id.id_cash_amount);
        totalAmount.setText("Amount: " + invoice.getGrandTotal().toString());

        Button purchaseButton = (Button) findViewById(R.id.id_cash_purchase_button);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProgressDialog(null, CashActivity.this.getString(R.string.process_dialog_processing_payment_msg));

                PayPalHereSDK.getTransactionManager().processPayment(TransactionManager.PaymentType.Cash,
                        null, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {

                            @Override
                            public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                                cancelProgressDialog();
                                showTransactionSuccessfulDialog();
                            }

                            @Override
                            public void onError(PPError<TransactionManager.PaymentErrors> error) {
                                cancelProgressDialog();
                                showTransactionFailedAlertDialog();

                            }
                        });
            }
        });

    }

    private void showTransactionSuccessfulDialog() {
        showTransactionCompletionDialog(R.string.transaction_complete_success);
    }

    private void showTransactionFailedAlertDialog() {
        showTransactionCompletionDialog(R.string.transaction_complete_failure);
    }

    private void showTransactionCompletionDialog(int stringId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(stringId);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.sdk_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        final AlertDialog dialog = builder.create();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void showProgressDialog(String title, String message) {
        mProgressDialog = new ProgressDialog(this);
        if (null != title) {
            mProgressDialog.setTitle(title);
        }
        if (null != message) {
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }

    private void cancelProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
