/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.emv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.TransactionRecord;
import com.paypal.merchant.sdk.domain.shopping.Tip;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.activity.ItemizedActivity;
import com.paypal.sampleapp.activity.MyActivity;
import com.paypal.sampleapp.util.CommonUtils;

import java.math.BigDecimal;

/**
 * This activity is meant to complete the EMV transaction by connecting the EMV device to take in the credit
 * card information.
 * <p/>
 *
 */
public class EMVTransactionActivity extends MyActivity {

    private static final String LOG_TAG = EMVTransactionActivity.class.getSimpleName();

    public static final int ACTIVITY_DEVICE_CONNECT_REQUEST_CODE = 1001;
    public static final int ACTIVITY_RESULT_CODE_SUCCESS = 2001;
    public static final int ACTIVITY_RESULT_CODE_FAILURE = 2002;

    private Button mTipButton;
    private Button mConnectButton;
    private Button mChargeButton;

    private class CallbackHandler implements DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>> {
        @Override
        public void onSuccess(TransactionManager.PaymentResponse responseObject) {
            Log.d(LOG_TAG, "CallbackHandler onSuccess");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }

        @Override
        public void onError(PPError<TransactionManager.PaymentErrors> error) {
            Log.e(LOG_TAG, "CallbackHandler onFailure");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    /**
     * Initialize the elements in the layout.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_transaction);


        mTipButton = (Button)findViewById(R.id.tipButton);
        mTipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openTipDialog();
            }
        });

        mConnectButton = (Button)findViewById(R.id.connectButton);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "mConnectButton onClick. Calling the DeviceConnect Activity to connect to the reader");
                startActivityForResult(new Intent(EMVTransactionActivity.this, DeviceConnectActivity.class), ACTIVITY_DEVICE_CONNECT_REQUEST_CODE);
            }
        });

        mChargeButton = (Button)findViewById(R.id.chargeButton);
        mChargeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PayPalHereSDK.getTransactionManager().processPaymentWithSDKUI(TransactionManager.PaymentType.CardReader,new CallbackHandler());
            }
        });
        updateButtonVisibility();
        updateUIWithCurrentInvoice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(ACTIVITY_DEVICE_CONNECT_REQUEST_CODE == requestCode){
            if(ACTIVITY_RESULT_CODE_SUCCESS == resultCode){
                updateButtonVisibility();
            }else{
                Toast.makeText(EMVTransactionActivity.this,R.string.device_connect_fail,Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG,"onBackPressed");
        showExitDialog();
    }

    private void showExitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EMVTransactionActivity.this);
        builder.setTitle(EMVTransactionActivity.this.getString(R.string.exit_dialog_title));
        builder.setMessage(EMVTransactionActivity.this.getString(R.string.exit_dialog_msg));
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                PayPalHereSDK.getTransactionManager().cancelPayment();
                finish();
            }
        });
        builder.setCancelable(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    private void openTipDialog() {
        AlertDialog.Builder tipDialog = new AlertDialog.Builder(EMVTransactionActivity.this);
        tipDialog.setTitle("Tip amount");
        final EditText vi = new EditText(this);
        vi.setHint("$0");
        vi.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tipDialog.setView(vi);
        tipDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String tip = vi.getText().toString();
                if (CommonUtils.isNullOrEmpty(tip)) {
                    Toast.makeText(EMVTransactionActivity.this,getString(R.string.emv_transaction_invalid_tip_amount),Toast.LENGTH_SHORT).show();
                    return;
                }
                BigDecimal tipVal = BigDecimal.ZERO;
                tipVal = new BigDecimal(tip);

                if (tipVal.doubleValue() <= BigDecimal.ZERO.doubleValue()) {
                    Toast.makeText(EMVTransactionActivity.this,getString(R.string.emv_transaction_invalid_tip_amount),Toast.LENGTH_SHORT).show();
                    return;
                }
                addTip(tipVal);
            }
        });

        tipDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        tipDialog.show();
    }

    private void addTip(BigDecimal tip) {
        Invoice invoice = PayPalHereSDK.getTransactionManager().getActiveInvoice();
        if (invoice == null) {
            Log.d(LOG_TAG, "invoice is null/empty");
            Toast.makeText(EMVTransactionActivity.this,R.string.emv_transaction_no_invoice_found,Toast.LENGTH_SHORT).show();
            return;
        }
        invoice.setTip(new Tip(Tip.Type.AMOUNT, tip));
        updateUIWithCurrentInvoice();
    }

    private void updateUIWithCurrentInvoice(){
        Log.d(LOG_TAG,"updateUIWithCurrentInvoice IN");
        Invoice invoice = PayPalHereSDK.getTransactionManager().getActiveInvoice();
        if(null == invoice){
            Log.e(LOG_TAG,"updateUIWithCurrentInvoice ERROR!. mInvoice is NULL");
            return;
        }
        TextView tv = (TextView)findViewById(R.id.subTotal);
        tv.setText(String.valueOf(invoice.getSubTotal().doubleValue()));

        tv = (TextView)findViewById(R.id.taxAmount);
        tv.setText(String.valueOf(invoice.getTaxAmount().doubleValue()));

        tv = (TextView)findViewById(R.id.tipAmount);
        tv.setText(String.valueOf(invoice.getTipAmount().doubleValue()));

        tv = (TextView)findViewById(R.id.totalAmount);
        tv.setText(String.valueOf(invoice.getGrandTotal().doubleValue()));
    }

    private void updateButtonVisibility(){
        // Show the connect button only when the device is not connected...
        if(!DeviceConnectActivity.isDeviceConnected()){
            mChargeButton.setVisibility(View.GONE);
            mConnectButton.setVisibility(View.VISIBLE);
        }else{
            mChargeButton.setVisibility(View.VISIBLE);
            mConnectButton.setVisibility(View.GONE);
        }
    }
}