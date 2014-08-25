package com.paypal.sampleapp.swipe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.paypal.sampleapp.util.LocalPreferences;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureAuthorizedTransactionsActivity extends Activity {
    private static final String LOG_TAG = CaptureAuthorizedTransactionsActivity.class.getSimpleName();

    private static final int SEND_RECEIPT_ACTIVITY_REQ_CODE = 7001;
    private TextView mInvoiceIdView;
    private TextView mAmountView;
    private TextView mDateView;
    private Button mCaptureButton;
    private Button mVoidButton;
    private Button mCaptureWithDifferentAmountButton;
    private EditText mDifferentAmount;

    private TransactionRecord mTransactionRecord;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");

        setContentView(R.layout.capture_authorized_transaction);

        mTransactionRecord = LocalPreferences.getmRecentTransactionRecord();
        if (null == mTransactionRecord || null == mTransactionRecord.getInvoice()) {
            Log.d(LOG_TAG, "Transaction Record or Invoice is NULL. Can not proceed further for this activity with out Transaction Record and Invoice");
            finish();
        }

        Invoice invoice = mTransactionRecord.getInvoice();

        String invoiceId = getString(R.string.capture_authorize_activity_invoice_id);
        invoiceId = invoiceId.replace("%s", invoice.getId());
        mInvoiceIdView = (TextView) findViewById(R.id.id_invoice);
        mInvoiceIdView.setText(invoiceId);

        String amount = getString(R.string.capture_authorize_activity_amount);
        amount = amount.replace("%s", String.format("%.2f", invoice.getGrandTotal().doubleValue()));
        mAmountView = (TextView) findViewById(R.id.id_amount);
        mAmountView.setText(amount);

        Date date = mTransactionRecord.getTransactionDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(date);
        String dateString = getString(R.string.capture_authorize_activity_date);
        dateString = dateString.replace("%s", dateStr);
        mDateView = (TextView) findViewById(R.id.id_date);
        mDateView.setText(dateString);

        mCaptureButton = (Button) findViewById(R.id.id_capture_payment);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Capture Button onClick");
                capturePayment();
            }
        });

        mVoidButton = (Button) findViewById(R.id.id_void_authorization);
        mVoidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Void Button onClick");
                voidPayment();
            }
        });

        mCaptureWithDifferentAmountButton = (Button) findViewById(R.id.id_capture_for_different_amount);
        mCaptureWithDifferentAmountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredAmount = mDifferentAmount.getText().toString();
                if (null == enteredAmount || enteredAmount.length() <= 0) {
                    Toast.makeText(CaptureAuthorizedTransactionsActivity.this, R.string.capture_autorize_activity_wrong_amount_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                int amount = Integer.valueOf(enteredAmount);
                Invoice invoice1 = mTransactionRecord.getInvoice();
                invoice1.setTip(new Tip(Tip.Type.AMOUNT, new BigDecimal(amount)));
                capturePayment();
            }
        });

        mDifferentAmount = (EditText) findViewById(R.id.id_different_amount);
        String totalHint = PayPalHereSDK.getMerchantManager().getActiveMerchant().getMerchantCurrency().getCurrencyCode() + " " + String.format("%.2f", invoice.getGrandTotal().doubleValue());
        mDifferentAmount.setHint(totalHint);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG,"onActivityResult requestCode: "+requestCode);
        if(SEND_RECEIPT_ACTIVITY_REQ_CODE == requestCode){
            finish();
        }
    }

    private void capturePayment() {
        PayPalHereSDK.getTransactionManager().capturePayment(mTransactionRecord, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
            @Override
            public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                Log.d(LOG_TAG, "TransactionManager:CapturePayment onSuccess");
                LocalPreferences.removeTransactionRecordFromAuthorizedList(responseObject.getTransactionRecord());
                LocalPreferences.storeCompletedTransactionRecord(responseObject.getTransactionRecord());
                cancelProgressDialog();
                startSendReceiptActivity(true, CaptureAuthorizedTransactionsActivity.this.getString(R.string.send_receipt_header_msg_capture));
            }

            @Override
            public void onError(PPError<TransactionManager.PaymentErrors> error) {
                Log.d(LOG_TAG, "TransactionManager:CapturePayment onFailure");
                cancelProgressDialog();
                startSendReceiptActivity(false, CaptureAuthorizedTransactionsActivity.this.getString(R.string.send_receipt_header_msg_capture));
            }
        });
        showProgressDialog(null, CaptureAuthorizedTransactionsActivity.this.getString(R.string.process_dialog_capturing_payment_msg));
    }

    private void voidPayment(){
        PayPalHereSDK.getTransactionManager().voidAuthorization(mTransactionRecord, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
            @Override
            public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                Log.d(LOG_TAG, "TransactionManager:VoidPayment onSuccess");
                LocalPreferences.removeTransactionRecordFromAuthorizedList(responseObject.getTransactionRecord());
                cancelProgressDialog();
                startSendReceiptActivity(true, CaptureAuthorizedTransactionsActivity.this.getString(R.string.send_receipt_header_msg_void));
            }

            @Override
            public void onError(PPError<TransactionManager.PaymentErrors> error) {
                Log.d(LOG_TAG, "TransactionManager:VoidPayment onFailure");
                cancelProgressDialog();
                startSendReceiptActivity(false, CaptureAuthorizedTransactionsActivity.this.getString(R.string.send_receipt_header_msg_void));
            }
        });
        showProgressDialog(null, CaptureAuthorizedTransactionsActivity.this.getString(R.string.process_dialog_voiding_payment_msg));
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

    private void startSendReceiptActivity(boolean isSuccess, String operation) {
        if (null != mTransactionRecord) {
            Intent intent = new Intent(CaptureAuthorizedTransactionsActivity.this, SendReceiptActivity.class);
            intent.putExtra(SendReceiptActivity.TRANSACTION_SUCCESS_INTENT_STRING, isSuccess);
            intent.putExtra(SendReceiptActivity.OPERATION_INTENT_STRING, operation);
            startActivityForResult(intent, SEND_RECEIPT_ACTIVITY_REQ_CODE);
        }
    }
}
