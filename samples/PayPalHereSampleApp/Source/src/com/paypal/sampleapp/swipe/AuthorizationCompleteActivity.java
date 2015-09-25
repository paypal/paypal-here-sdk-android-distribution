package com.paypal.sampleapp.swipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.TransactionRecord;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.LocalPreferences;

public class AuthorizationCompleteActivity extends Activity{
    private static final String LOG_TAG = AuthorizationCompleteActivity.class.getSimpleName();
    private static final int SEND_RECEIPT_ACTIVITY_REQ_CODE = 6001;

    private TextView mHeaderView;
    private Button mCaptureButton;
    private Button mVoidButton;
    private Button mNewTransactionButton;

    private TransactionRecord mTransactionRecord;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"onCreate IN");

        setContentView(R.layout.activity_authorization_complete);

        mTransactionRecord = LocalPreferences.getmRecentTransactionRecord();
        if(null == mTransactionRecord){
            Log.d(LOG_TAG,"Recent TransactionRecord from LocalPreferences is NULL. So can not proceed further with this activity with out TransactionRecord.");
            finish();
        }

        mHeaderView = (TextView)findViewById(R.id.id_header_view);
        String headerText = getString(R.string.authorization_success_message);
        headerText = headerText.replace("%s", mTransactionRecord.getInvoice().getGrandTotal().toString());
        mHeaderView.setText(headerText);

        mCaptureButton = (Button)findViewById(R.id.id_capture_payment);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG,"CaptureButton onClick");
                PayPalHereSDK.getTransactionManager().capturePayment(mTransactionRecord,new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                    @Override
                    public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                        Log.d(LOG_TAG,"TransactionManager:CapturePayment onSuccess");
                        LocalPreferences.removeTransactionRecordFromAuthorizedList(responseObject.getTransactionRecord());
                        LocalPreferences.storeCompletedTransactionRecord(responseObject.getTransactionRecord());
                        cancelProgressDialog();
                        startSendReceiptActivity(true,AuthorizationCompleteActivity.this.getString(R.string.send_receipt_header_msg_capture));
                    }

                    @Override
                    public void onError(PPError<TransactionManager.PaymentErrors> error) {
                        Log.d(LOG_TAG,"TransactionManager:CapturePayment onFailure");
                        cancelProgressDialog();
                        startSendReceiptActivity(false,AuthorizationCompleteActivity.this.getString(R.string.send_receipt_header_msg_capture));
                    }
                });
                showProgressDialog(null,AuthorizationCompleteActivity.this.getString(R.string.process_dialog_capturing_payment_msg));
            }
        });

        mVoidButton = (Button)findViewById(R.id.id_void_authorization);
        mVoidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG,"VoidButton onClick");
                PayPalHereSDK.getTransactionManager().voidAuthorization(mTransactionRecord,new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                    @Override
                    public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                        Log.d(LOG_TAG,"TransactionManager:VoidPayment onSuccess");
                        cancelProgressDialog();
                        startSendReceiptActivity(true,AuthorizationCompleteActivity.this.getString(R.string.send_receipt_header_msg_void));
                    }

                    @Override
                    public void onError(PPError<TransactionManager.PaymentErrors> error) {
                        Log.d(LOG_TAG,"TransactionManager:VoidPayment onFailure");
                        cancelProgressDialog();
                        startSendReceiptActivity(false,AuthorizationCompleteActivity.this.getString(R.string.send_receipt_header_msg_void));
                    }
                });
                showProgressDialog(null,AuthorizationCompleteActivity.this.getString(R.string.process_dialog_voiding_payment_msg));
            }
        });

        mNewTransactionButton = (Button)findViewById(R.id.id_new_transaction);
        mNewTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG,"New Transaction onClick");
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG,"onActivityResult requestCode: "+requestCode);
        if(SEND_RECEIPT_ACTIVITY_REQ_CODE == requestCode){
            finish();
        }
    }

    private void showProgressDialog(String title, String message) {
        Log.d(LOG_TAG, "showProgressDialog IN");
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

    private void startSendReceiptActivity(boolean isSuccess, String operation){
        if(null != mTransactionRecord) {
            Intent intent = new Intent(AuthorizationCompleteActivity.this, SendReceiptActivity.class);
            intent.putExtra(SendReceiptActivity.TRANSACTION_SUCCESS_INTENT_STRING, isSuccess);
            intent.putExtra(SendReceiptActivity.OPERATION_INTENT_STRING,operation);
            startActivityForResult(intent, SEND_RECEIPT_ACTIVITY_REQ_CODE);
        }
    }
}
