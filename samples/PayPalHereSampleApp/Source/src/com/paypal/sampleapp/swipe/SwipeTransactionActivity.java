package com.paypal.sampleapp.swipe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionListener;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.ChipAndPinDecisionEvent;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.merchant.sdk.domain.TransactionRecord;
import com.paypal.merchant.sdk.domain.shopping.Tip;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.activity.MyActivity;
import com.paypal.sampleapp.activity.SignatureActivity;
import com.paypal.sampleapp.util.CommonUtils;
import com.paypal.sampleapp.util.LocalPreferences;

import java.math.BigDecimal;
import java.util.List;

public class SwipeTransactionActivity extends Activity implements CardReaderListener, TransactionListener{
    private static final String LOG_TAG = SwipeTransactionActivity.class.getSimpleName();

    private static final int SIGNATURE_ACTIVITY_REQ_CODE = 5001;
    private static final int SEND_RECEIPT_ACTIVITY_REQ_CODE = 5002;
    private static final int AUTHORIZATION_COMPLETE_ACTIVITY_REQ_CODE = 5003;

    private Button mTipButton;
    private TextView mSwiperStatusView;
    private ProgressDialog mProgressDialog = null;

    private TransactionManager mTransactionManager;
    private TransactionRecord mTransactionRecord;
    private boolean mIsAuthPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"onCreate IN");

        //set the layout file to be used for this activity
        setContentView(R.layout.activity_swipe_transaction);

        mIsAuthPayment = LocalPreferences.getAuthorizeOption();
        mTipButton = (Button)findViewById(R.id.tipButton);
        mTipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTipDialog();
            }
        });

        mSwiperStatusView = (TextView)findViewById(R.id.swiper_status);
        updateSwiperStatusMessage();
        updateUIWithCurrentInvoice();

        mTransactionManager = PayPalHereSDK.getTransactionManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG,"onActivityResult requestCode: "+requestCode);
        if(SIGNATURE_ACTIVITY_REQ_CODE == requestCode && RESULT_OK == resultCode){
            finalizePayment();
        }else if(SEND_RECEIPT_ACTIVITY_REQ_CODE == requestCode){
            finish();
        }else if(AUTHORIZATION_COMPLETE_ACTIVITY_REQ_CODE == requestCode){
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG,"onPause IN");
        PayPalHereSDK.getCardReaderManager().unregisterCardReaderListener(this);
        mTransactionManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume IN");
        PayPalHereSDK.getCardReaderManager().registerCardReaderListener(this);
        mTransactionManager.registerListener(this);
    }

    private void showTransactionFailedAlertDialog() {
        Log.d(LOG_TAG, "showTransactionFailedAlertDialog IN");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.transaction_complete_failure);
        builder.setMessage(R.string.transaction_failure_msg);
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

    private void startSendReceiptActivity(boolean isSuccess){
        if(null != mTransactionRecord) {
            Intent intent = new Intent(SwipeTransactionActivity.this, SendReceiptActivity.class);
            intent.putExtra(SendReceiptActivity.TRANSACTION_SUCCESS_INTENT_STRING, isSuccess);
            intent.putExtra(SendReceiptActivity.OPERATION_INTENT_STRING,SwipeTransactionActivity.this.getString(R.string.send_receipt_header_msg_transaction));
            startActivityForResult(intent, SEND_RECEIPT_ACTIVITY_REQ_CODE);
        }else{
            showTransactionFailedAlertDialog();
        }
    }

    private void finalizePayment() {
        Log.d(LOG_TAG,"FinalizePayment IN");
        PayPalHereSDK.getTransactionManager().finalizePayment(mTransactionRecord, SignatureActivity.getSignatureBitmap(),
                new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                    @Override
                    public void onSuccess(TransactionManager.PaymentResponse paymentResponse) {
                        Log.d(LOG_TAG,"TransactionManager:FinalizePayment onSuccess");
                        cancelProgressDialog();
                        startSendReceiptActivity(true);
                    }

                    @Override
                    public void onError(PPError<TransactionManager.PaymentErrors> paymentErrorsPPError) {
                        Log.d(LOG_TAG,"TransactionManager:FinalizePayment onError");
                        cancelProgressDialog();
                        startSendReceiptActivity(false);
                    }
                }
        );
        showProgressDialog(null,SwipeTransactionActivity.this.getString(R.string.process_dialog_finalizing_payment_msg));
    }

    private void openTipDialog() {
        AlertDialog.Builder tipDialog = new AlertDialog.Builder(SwipeTransactionActivity.this);
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
                    Toast.makeText(SwipeTransactionActivity.this, getString(R.string.emv_transaction_invalid_tip_amount), Toast.LENGTH_SHORT).show();
                    return;
                }
                BigDecimal tipVal = BigDecimal.ZERO;
                tipVal = new BigDecimal(tip);

                if (tipVal.doubleValue() <= BigDecimal.ZERO.doubleValue()) {
                    Toast.makeText(SwipeTransactionActivity.this,getString(R.string.emv_transaction_invalid_tip_amount),Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SwipeTransactionActivity.this,R.string.emv_transaction_no_invoice_found,Toast.LENGTH_SHORT).show();
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

    private void updateSwiperStatusMessage(){
        if(MyActivity.isSwiperConnected()){
            if(mIsAuthPayment){
                mSwiperStatusView.setText(R.string.swiper_authorize_msg);
            }else {
                mSwiperStatusView.setText(R.string.swiper_sale_msg);
            }
            mSwiperStatusView.setTextColor(Color.BLUE);
        }else{
            mSwiperStatusView.setText(R.string.swiper_connect);
            mSwiperStatusView.setTextColor(Color.RED);
        }
    }

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {
        Log.d(LOG_TAG,"onPaymentReaderConnected IN");
        updateSwiperStatusMessage();
    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {
        Log.d(LOG_TAG,"onPaymentReaderDisconnected IN");
        updateSwiperStatusMessage();
    }

    @Override
    public void onCardReadSuccess(SecureCreditCard paymentCard) {
        Log.d(LOG_TAG,"onCardReadSuccess IN");
        if(mIsAuthPayment) {
            Log.d(LOG_TAG,"onCardReadSuccess, calling authorizePayment");
            mTransactionManager.authorizePayment(TransactionManager.PaymentType.CardReader,new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                @Override
                public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                    Log.d(LOG_TAG,"TransactionManager:AuthorizePayment onSuccess");
                    cancelProgressDialog();
                    mTransactionRecord = responseObject.getTransactionRecord();
                    LocalPreferences.storeAuthorizedTransactionRecord(mTransactionRecord);
                    LocalPreferences.storeRecentTransactionRecord(mTransactionRecord);
                    Intent intent = new Intent(SwipeTransactionActivity.this,AuthorizationCompleteActivity.class);
                    SwipeTransactionActivity.this.startActivityForResult(intent, AUTHORIZATION_COMPLETE_ACTIVITY_REQ_CODE);
                }

                @Override
                public void onError(PPError<TransactionManager.PaymentErrors> error) {
                    Log.d(LOG_TAG,"TransactionManager:AuthorizePayment onError");
                    cancelProgressDialog();
                    startSendReceiptActivity(false);
                }
            });
            showProgressDialog(null,SwipeTransactionActivity.this.getString(R.string.process_dialog_authorizing_payment_msg));
        }else{
            Log.d(LOG_TAG,"onCardReadSuccess, calling processPayment");
            mTransactionManager.processPayment(TransactionManager.PaymentType.CardReader, null, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                @Override
                public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                    Log.d(LOG_TAG, "TransactionManager:ProcessPayment onSuccess");
                    mTransactionRecord = responseObject.getTransactionRecord();
                    LocalPreferences.storeCompletedTransactionRecord(mTransactionRecord);
                    LocalPreferences.storeRecentTransactionRecord(mTransactionRecord);
                    if (null != mTransactionRecord) {
                        Log.d(LOG_TAG, "TransactionManager:ProcessPayment onSuccess TransactionID: " + mTransactionRecord.getTransactionId());
                    }
                }

                @Override
                public void onError(PPError<TransactionManager.PaymentErrors> error) {
                    cancelProgressDialog();
                    Log.d(LOG_TAG, "TransactionManager:ProcessPayment onError. Error: " + error.getErrorCode());
                    startSendReceiptActivity(false);
                }
            });
            showProgressDialog(null,SwipeTransactionActivity.this.getString(R.string.process_dialog_processing_payment_msg));
        }
    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> reason) {
        Log.d(LOG_TAG,"onCardReadFailed IN");
    }

    @Override
    public void onCardReaderEvent(PPError<CardReaderEvents> e) {
        Log.d(LOG_TAG,"onCardReaderEvent IN");
    }

    @Override
    public void onSelectPaymentDecision(List<ChipAndPinDecisionEvent> decisionEventList) {
        Log.d(LOG_TAG,"onSelectPaymentDecision IN");
    }

    @Override
    public void onInvalidListeningPort() {
        Log.d(LOG_TAG,"onInvalidListeningPort IN");
    }

    @Override
    public void onPaymentEvent(PaymentEvent e) {
        Log.d(LOG_TAG,"onPaymentEvent IN");
        if(PaymentEventType.Idle == e.getEventType()){
            Log.d(LOG_TAG,"onPaymentEvent: IDLE");
        } else if(PaymentEventType.GettingPaymentInfo == e.getEventType()){
            Log.d(LOG_TAG,"onPaymentEvent: GettingPaymentInfo");
        }else if(PaymentEventType.CardDataReceived == e.getEventType()){
            Log.d(LOG_TAG,"onPaymentEvent: CardDataReceived");
        }else if(PaymentEventType.ProcessingPayment == e.getEventType()){
            Log.d(LOG_TAG,"onPaymentEvent: ProcessingPayment");
        }else if(PaymentEventType.WaitingForSignature == e.getEventType()){
            Log.d(LOG_TAG,"onPaymentEvent: WaitingForSignature");
            if(!mIsAuthPayment) {
                cancelProgressDialog();
                Intent intent = new Intent(SwipeTransactionActivity.this, SignatureActivity.class);
                startActivityForResult(intent, SIGNATURE_ACTIVITY_REQ_CODE);
            }
        }else if(PaymentEventType.TransactionCanceled == e.getEventType()){
            cancelProgressDialog();
            Log.d(LOG_TAG,"onPaymentEvent: TransactionCanceled");
        }else if(PaymentEventType.TransactionDeclined == e.getEventType()){
            cancelProgressDialog();
            Log.d(LOG_TAG,"onPaymentEvent: TransactionDeclined");
        }
    }
}
