package com.paypal.sampleapp.swipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.TransactionRecord;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.LocalPreferences;

public class SendReceiptActivity extends Activity {
    private static final String LOG_TAG = SendReceiptActivity.class.getSimpleName();
    public static final String TRANSACTION_SUCCESS_INTENT_STRING = "TRANSACTION_SUCCESS_INTENT_STRING";
    public static final String OPERATION_INTENT_STRING = "OPERATION_INTENT_STRING";
    private TextView mHeaderMsgView;
    private Button mEmailView;
    private Button mTextView;
    private Button mNoThanksView;
    private ProgressDialog mProgressDialog = null;

    private TransactionRecord mTransactionRecord;
    private TransactionManager mTransactionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
        mTransactionManager = PayPalHereSDK.getTransactionManager();

        mTransactionRecord = LocalPreferences.getmRecentTransactionRecord();
        if(null == mTransactionRecord){
            Log.d(LOG_TAG,"Recent TransactionRecord from LocalPreferences is NULL. So can not proceed further with this activity with out TransactionRecord.");
            finish();
        }

        Intent intent = getIntent();
        boolean isSuccessfulTransaction = false;
        if(intent.hasExtra(TRANSACTION_SUCCESS_INTENT_STRING)){
            isSuccessfulTransaction = intent.getBooleanExtra(TRANSACTION_SUCCESS_INTENT_STRING, false);
        }

        String msg = null;
        if(intent.hasExtra(OPERATION_INTENT_STRING)){
            msg = intent.getStringExtra(OPERATION_INTENT_STRING);
        }

        if(null == mTransactionRecord || null == mTransactionRecord.getTransactionId()){
            Log.e(LOG_TAG,"Incoming TransactionRecord is NULL. Hence we can not proceed further...");
        }
        setContentView(R.layout.transaction_complete);

        mHeaderMsgView = (TextView)findViewById(R.id.header_view);
        if(isSuccessfulTransaction){
            String headerMsg = getString(R.string.send_receipt_header_msg_success);
            headerMsg = headerMsg.replace("%s",msg);
            mHeaderMsgView.setText(headerMsg);
            mHeaderMsgView.setTextColor(Color.GREEN);
        }else{
            String headerMsg = getString(R.string.send_receipt_header_msg_failure);
            headerMsg = headerMsg.replace("%s",msg);
            mHeaderMsgView.setText(headerMsg);
            mHeaderMsgView.setTextColor(Color.RED);
        }

        mEmailView = (Button)findViewById(R.id.email);
        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReceipt(true);
            }
        });

        mTextView = (Button)findViewById(R.id.text);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReceipt(false);
            }
        });

        mNoThanksView = (Button)findViewById(R.id.no_thanks);
        mNoThanksView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void sendReceipt(final Boolean byEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(SendReceiptActivity.this);
        //editText.setPadding(10, 10, 10, 0);
        if (byEmail) {
            builder.setTitle(SendReceiptActivity.this.getString(R.string.send_receipt_enter_email_alert_dialog_title));
            editText.setHint(R.string.enter_your_email_hint);
        } else {
            builder.setTitle(SendReceiptActivity.this.getString(R.string.send_receipt_enter_mobile_alert_dialog_title));
            editText.setHint(R.string.enter_your_mobile_number_hint);
        }
        builder.setView((View) editText);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Hide the Keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(SendReceiptActivity.this.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                if (byEmail) {
                    String email = editText.getText().toString();
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(SendReceiptActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendReceipt(true);
                            }
                        }).run();
                        return;
                    }
                    mTransactionManager.sendReceipt(mTransactionRecord, email, TransactionManager.SendReceiptTransactionType.SaleTransaction, new SendReceiptHandler());
                } else {
                    String mobile = editText.getText().toString();
                    if (!Patterns.PHONE.matcher(mobile).matches()) {
                        Toast.makeText(SendReceiptActivity.this,R.string.invalid_mobile, Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendReceipt(false);
                            }
                        }).run();
                        return;
                    }
                    mTransactionManager.sendReceipt(mTransactionRecord, mobile, TransactionManager.SendReceiptTransactionType.SaleTransaction,new SendReceiptHandler());
                }
                dialogInterface.dismiss();
                showProgressDialog(null, SendReceiptActivity.this.getString(R.string.send_receipt_processing_dialog));
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

    private class SendReceiptHandler implements DefaultResponseHandler<Boolean, PPError<TransactionManager.SendReceiptErrors>> {

        @Override
        public void onSuccess(Boolean responseObject) {
            Log.d(LOG_TAG, "SendReceiptHandler: onSuccess");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelProgressDialog();
                    Toast.makeText(SendReceiptActivity.this,R.string.send_receipt_complete,Toast.LENGTH_SHORT).show();
                    mTransactionRecord = null;
                    finish();
                }
            });
        }

        @Override
        public void onError(PPError<TransactionManager.SendReceiptErrors> error) {
            Log.e(LOG_TAG, "SendReceiptHandler: onError Error: " + error.getDetailedMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelProgressDialog();
                    Toast.makeText(SendReceiptActivity.this,R.string.send_receipt_error,Toast.LENGTH_SHORT).show();
                    mTransactionRecord = null;
                    finish();
                }
            });
        }
    }
}
