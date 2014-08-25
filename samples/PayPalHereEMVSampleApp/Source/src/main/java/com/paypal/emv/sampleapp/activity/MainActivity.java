package com.paypal.emv.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.utils.LocalPreferences;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.PPError;

import java.math.BigDecimal;

public class MainActivity extends Activity {
    public static final int ACTIVITY_DEVICE_CONNECT_REQUEST_CODE = 1001;
    public static final int ACTIVITY_RESULT_CODE_SUCCESS = 2001;
    public static final int ACTIVITY_RESULT_CODE_FAILURE = 2002;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private TransactionManager mTransactionManager = null;
    private CardReaderManager mCardReaderManager = null;

    private TextView mReaderStatusButton = null;
    private Button mReaderConnectButton = null;
    private Button mSalesHistoryButton = null;
    private Button mChargeButton = null;
    private EditText mChargeAmount = null;
    private double mAmount = -1;

    private String mReaderConnectedText;
    private String mReaderNotConnectedText;

    private Boolean mIsReaderDeviceConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
        setContentView(R.layout.activity_main);
        LocalPreferences.init(this);

        mReaderStatusButton = (TextView) findViewById(R.id.reader_status);
        mReaderConnectButton = (Button) findViewById(R.id.reader_connect);
        mChargeAmount = (EditText) findViewById(R.id.amount);


        mTransactionManager = PayPalHereSDK.getTransactionManager();
        mCardReaderManager = PayPalHereSDK.getCardReaderManager();

        CardReaderManager.BeginMonitoringStatus status = mCardReaderManager.beginMonitoring(CardReaderListener.ReaderConnectionTypes.Bluetooth);
        if(!(CardReaderManager.BeginMonitoringStatus.Success == status)){
            showAlertDialog(R.string.merchant_error_title, R.string.merchant_not_supported_msg,true);
            return;
        }

        mReaderConnectedText = mReaderStatusButton.getText().toString() + "\n" + getString(R.string.reader_connected);
        mReaderNotConnectedText = mReaderStatusButton.getText().toString() + "\n" + getString(R.string.reader_not_connected);

        mReaderStatusButton.setText(mReaderNotConnectedText);
        mReaderStatusButton.setTextColor(Color.RED);

        mReaderConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mReaderConnectButton.getText().toString().equalsIgnoreCase(MainActivity.this.getString(R.string.reader_connect))) {
                    Log.d(LOG_TAG, "mReaderConnectButton onClick. Calling the mEMVTransaction to connect to the reader");
                    startActivityForResult(new Intent(MainActivity.this, DeviceConnectActivity.class), ACTIVITY_DEVICE_CONNECT_REQUEST_CODE);
                } else if (mReaderConnectButton.getText().toString().equalsIgnoreCase(MainActivity.this.getString(R.string.reader_disconnect))) {
                    disconnectTheEMVDevice();
                }
            }
        });

        mChargeButton = (Button) findViewById(R.id.charge_button);
        mChargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (false == mIsReaderDeviceConnected) {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.no_charge_because_reader_not_connected), Toast.LENGTH_SHORT).show();
                    return;
                }
                String amountText = mChargeAmount.getText().toString();
                if(amountText == null || amountText.length() <= 0) {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.no_charge_because_amount_not_valid), Toast.LENGTH_SHORT).show();
                    return;
                }

                amountText = String.format("%.2f", Double.parseDouble(amountText));

                if (null == amountText || amountText.length() <= 0 ||
                        amountText.equalsIgnoreCase(MainActivity.this.getString(R.string.amount_hint))) {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.no_charge_because_amount_not_valid), Toast.LENGTH_SHORT).show();
                    return;
                }
                mAmount = Double.parseDouble(amountText);
                if (0 >= mAmount) {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.no_charge_because_amount_not_valid), Toast.LENGTH_SHORT).show();
                    return;
                }
                BigDecimal amount = new BigDecimal(mAmount);
                mTransactionManager.beginPayment(amount);
                mTransactionManager.processPayment(MainActivity.this, new CallbackHandler());
            }
        });

        mSalesHistoryButton = (Button)findViewById(R.id.id_sales_history);
        mSalesHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Sales history button onClick");
                Intent intent = new Intent(MainActivity.this,SalesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTIVITY_DEVICE_CONNECT_REQUEST_CODE == requestCode) {
            if (ACTIVITY_RESULT_CODE_SUCCESS == resultCode) {
                Toast.makeText(MainActivity.this, getString(R.string.reader_connected_toast_msg), Toast.LENGTH_SHORT).show();
                if (null != mReaderConnectButton) {
                    mReaderStatusButton.setText(mReaderConnectedText);
                    mReaderStatusButton.setTextColor(Color.GREEN);
                }
                if (null != mReaderConnectButton) {
                    mIsReaderDeviceConnected = true;
                    mReaderConnectButton.setText(R.string.reader_disconnect);
                }
            } else if (ACTIVITY_RESULT_CODE_FAILURE == resultCode) {
                Toast.makeText(MainActivity.this, getString(R.string.reader_connect_fail_toast_msg), Toast.LENGTH_SHORT).show();
                if (null != mReaderConnectButton) {
                    mReaderStatusButton.setText(mReaderNotConnectedText);
                    mReaderStatusButton.setTextColor(Color.RED);
                    if (null != mReaderConnectButton) {
                        mIsReaderDeviceConnected = false;
                        mReaderConnectButton.setText(R.string.reader_connect);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu IN");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onPrepareOptionsMenu IN");
        MenuItem deviceDetailsItem = menu.findItem(R.id.action_device_details);
        if(mIsReaderDeviceConnected){
            deviceDetailsItem.setEnabled(true);
        }else{
            deviceDetailsItem.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected IN Item: " + item.getTitle());
        switch (item.getItemId()) {
            case R.id.action_device_details: {
                Log.d(LOG_TAG, "onMenuItemSelected:action_device_details");
                Intent intent = new Intent(MainActivity.this,DeviceDetailsActivity.class);
                startActivity(intent);
            }
            break;
            default: {

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(LOG_TAG, "onConfigurationChanged IN");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed IN");
        showExitDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart IN");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause IN");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume IN");
        hideKeyboard();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop IN");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy IN");
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

    private void disconnectTheEMVDevice() {
        if (null != DeviceConnectActivity.mSelectedBluetoothDevice) {
            PayPalHereSDK.getCardReaderManager().disconnectFromDevice(DeviceConnectActivity.mSelectedBluetoothDevice);
            mReaderStatusButton.setText(mReaderNotConnectedText);
            mReaderStatusButton.setTextColor(Color.RED);
        }
    }

    private void showExitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(MainActivity.this.getString(R.string.exit_dialog_title));
        builder.setMessage(MainActivity.this.getString(R.string.exit_dialog_msg));
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
                if (mIsReaderDeviceConnected) {
                    disconnectTheEMVDevice();
                }
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mChargeAmount.getWindowToken(), 0);
    }

    private void clearChargeAmount() {
        if (null != mChargeAmount) {
            mChargeAmount.setText("");
        }
    }

    private class CallbackHandler implements DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.EMVPaymentErrors>> {
        @Override
        public void onSuccess(TransactionManager.PaymentResponse responseObject) {
            Log.d(LOG_TAG, "CallbackHandler onSuccess");
            if(null != responseObject && null != responseObject.getTransactionRecord()) {
                LocalPreferences.storeCompletedTransactionRecord(responseObject.getTransactionRecord());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearChargeAmount();
                }
            });
        }

        @Override
        public void onError(final PPError<TransactionManager.EMVPaymentErrors> error) {
            Log.e(LOG_TAG, "CallbackHandler onFailure");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                clearChargeAmount();
                if(TransactionManager.EMVPaymentErrors.AmountTooLow == error.getErrorCode()){
                    showAlertDialog(R.string.merchant_error_title,R.string.merchant_amount_too_low,false);
                }else if(TransactionManager.EMVPaymentErrors.AmountTooHigh == error.getErrorCode()){
                    showAlertDialog(R.string.merchant_error_title,R.string.merchant_amount_too_high,false);
                }else if(TransactionManager.EMVPaymentErrors.BatteryLow == error.getErrorCode()){
                    showAlertDialog(R.string.merchant_error_title,R.string.merchant_battery_too_low,false);
                }
                }
            });
        }
    }
}
