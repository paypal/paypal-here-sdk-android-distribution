package com.paypal.emv.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.adapter.SalesListAdapter;
import com.paypal.emv.sampleapp.listeners.AdapterListener;
import com.paypal.emv.sampleapp.utils.LocalPreferences;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.TransactionRecord;

import java.math.BigDecimal;


public class SalesActivity extends Activity implements AdapterListener {
    private static final String LOG_TAG = SalesActivity.class.getSimpleName();
    public static final int SEND_RECEIPT_ACTIVITY_REQ_CODE = 8001;

    private ListView mListView = null;
    private SalesListAdapter mAdapter = null;
    private TransactionRecord mSelectedTransactionRecord;
    private ProgressDialog mProgressDialog;
    private boolean mPartialRefund;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter = new SalesListAdapter(this, SalesListAdapter.TransactionListType.COMPLETED_TRANSACTION_LIST, this);
        mAdapter.addItems(LocalPreferences.getCompletedTransactionRecordList());
        if (0 == mAdapter.getCount()) {
            setContentView(R.layout.no_items_view);
        } else {
            setContentView(R.layout.list_view);
            mListView = (ListView) findViewById(R.id.list_view);
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult requestCode: " + requestCode);
        if (SEND_RECEIPT_ACTIVITY_REQ_CODE == requestCode) {
            finish();
        }
    }

    @Override
    public void onItemClicked(Object item) {
        Log.d(LOG_TAG, "onItemClicked IN Item: " + item);
        if (null == item) {
            Log.e(LOG_TAG, "Error. incoming item is null..");
            return;
        }
        mSelectedTransactionRecord = (TransactionRecord) item;
        showAlertDialogForRefundOptions();
    }

    private void showAlertDialogForRefundOptions() {
        Log.d(LOG_TAG, "showAlertDialogWithPairedDevices IN");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(SalesActivity.this, com.paypal.merchant.sdk.R.layout.sdk_device_name);
        adapter.add(SalesActivity.this.getString(R.string.options_refund_full_amount));
        adapter.add(SalesActivity.this.getString(R.string.options_refund_partial_amount));
        final AlertDialog.Builder builder = new AlertDialog.Builder(SalesActivity.this);
        builder.setTitle(R.string.refund_title);
        builder.setCancelable(false);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "RefundOptions onClick Position: " + i);
                dialogInterface.dismiss();
                if (0 == i) {
                    performRefundWithSDKUI(mSelectedTransactionRecord, mSelectedTransactionRecord.getInvoice().getGrandTotal());
                } else {
                    showRefundAmountDialog();
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    private void showRefundCompleteDialog(boolean isRefundSuccess) {
        Log.d(LOG_TAG, "showRefundCompleteDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(SalesActivity.this);
        builder.setTitle(R.string.refund_title);
        if (isRefundSuccess) {
            builder.setMessage(R.string.refund_success_msg);
        } else {
            builder.setMessage(R.string.refund_failure_msg);
        }
        builder.setCancelable(false);
        builder.setNeutralButton(com.paypal.merchant.sdk.R.string.sdk_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Log.d(LOG_TAG, "refundCompleteDialog onClick");
                onResume();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    private void performRefund(final TransactionRecord record) {
        showProgressDialog(null, SalesActivity.this.getString(R.string.refund_processing));
        PayPalHereSDK.getTransactionManager().doRefund(record, record.getInvoice().getGrandTotal(), new DefaultResponseHandler<TransactionRecord, PPError<PPError.BasicErrors>>() {
            @Override
            public void onSuccess(TransactionRecord responseObject) {
                Log.d(LOG_TAG, "refund callback onSuccess");
                cancelProgressDialog();
                LocalPreferences.removeTransactionRecordFromCompletedList(record);
                showRefundCompleteDialog(true);
            }

            @Override
            public void onError(PPError<PPError.BasicErrors> error) {
                Log.e(LOG_TAG, "refund callback onFailure");
                cancelProgressDialog();
                showRefundCompleteDialog(false);
            }
        });
    }

    private void showRefundAmountDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.refund_title);
        final EditText input = new EditText(this);
        input.setHint(R.string.refund_amount_txt);
        input.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                double val = Double.valueOf(value);
                if (val > mSelectedTransactionRecord.getInvoice().getGrandTotal().doubleValue()) {
                    Toast.makeText(SalesActivity.this, R.string.refund_amount_invalid, Toast.LENGTH_SHORT).show();
                    input.setText("");
                    return;
                }
                dialog.dismiss();
                mPartialRefund = true;
                mPartialRefund = true;
                performRefundWithSDKUI(mSelectedTransactionRecord, new BigDecimal(val));
            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void performRefundWithSDKUI(final TransactionRecord record, BigDecimal amount) {
        PayPalHereSDK.getTransactionManager().processRefund(SalesActivity.this, record, amount, new DefaultResponseHandler<TransactionRecord, PPError<TransactionManager.EMVPaymentErrors>>() {
            @Override
            public void onSuccess(TransactionRecord responseObject) {
                Log.d(LOG_TAG, "refund callback onSuccess");
                if (!mPartialRefund) {
                    LocalPreferences.removeTransactionRecordFromCompletedList(record);
                }
                mPartialRefund = false;
            }

            @Override
            public void onError(PPError<TransactionManager.EMVPaymentErrors> error) {
                Log.e(LOG_TAG, "refund callback onFailure");
                if(TransactionManager.EMVPaymentErrors.BatteryLow == error.getErrorCode()){
                    showAlertDialog(R.string.merchant_error_title,R.string.merchant_battery_too_low,false);
                }else if(TransactionManager.EMVPaymentErrors.MandatorySoftwareUpdateRequired == error.getErrorCode()){
                    showAlertDialog(R.string.merchant_error_title,R.string.error_mandatory_software_update,false);
                }
            }
        });
    }

    private void showAlertDialog(int titleResID, int msgResID, final boolean finish){
        Log.d(LOG_TAG, "showTransactionCompleteAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResID);
        builder.setMessage(msgResID);
        builder.setCancelable(false);
        builder.setNeutralButton(com.paypal.merchant.sdk.R.string.sdk_OK,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(finish) {
                    finish();
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
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
}
