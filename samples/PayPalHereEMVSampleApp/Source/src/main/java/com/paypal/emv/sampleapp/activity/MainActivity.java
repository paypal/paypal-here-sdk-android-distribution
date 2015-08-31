package com.paypal.emv.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.utils.LocalPreferences;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.TransactionController;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionListener;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.DomainFactory;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.InvoiceItem;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SDKReceiptScreenOptions;
import com.paypal.merchant.sdk.domain.SDKSignatureScreenOptions;
import com.paypal.merchant.sdk.domain.shopping.Tip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements TransactionController, TransactionListener{
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
    private EditText mTipAmount = null;
    private double mAmount = 0;
    private double mTip = 0;
    private boolean mIsProcessingPayment = false;

    private String mReaderConnectedText;
    private String mReaderNotConnectedText;

    private boolean mIsReaderDeviceConnected = false;

    private AlertDialog mCurrentDisplayDialog;
    private BluetoothDevice mSelectedBluetoothDevice;
    private boolean mSoftwareUpdateRequired = false;
    private Invoice mInvoice;
    private InvoiceItem mItem;
    private int mRetryCount = 0;

    private TextWatcher mTotalWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(LOG_TAG, "char seq:" + s + " : start:" + start + " : count:" + count +" : after:" + after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(LOG_TAG, "char seq:" + s + " : start:" + start + " : count:" + count +" : before:" + before);
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(LOG_TAG, "Editable:" + s);
            updateInvoice();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
        setContentView(R.layout.activity_main);

        LocalPreferences.init(this);

        mReaderStatusButton = (TextView) findViewById(R.id.reader_status);
        mReaderConnectButton = (Button) findViewById(R.id.reader_connect);

        mChargeAmount = (EditText) findViewById(R.id.amount);
        mChargeAmount.addTextChangedListener(mTotalWatcher);

        mTipAmount = (EditText) findViewById(R.id.tip);
        mTipAmount.addTextChangedListener(mTotalWatcher);

        mTransactionManager = PayPalHereSDK.getTransactionManager();
        mCardReaderManager = PayPalHereSDK.getCardReaderManager();

        mTransactionManager.registerListener(this);

        mReaderConnectedText = mReaderStatusButton.getText().toString() + "\n" + getString(R.string.reader_connected);
        mReaderNotConnectedText = mReaderStatusButton.getText().toString() + "\n" + getString(R.string.reader_not_connected);

        mReaderStatusButton.setText(mReaderNotConnectedText);
        mReaderStatusButton.setTextColor(Color.RED);

        mReaderConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mReaderConnectButton.getText().toString().equalsIgnoreCase(MainActivity.this.getString(R.string.reader_connect))) {
                    Log.d(LOG_TAG, "mReaderConnectButton onClick. Calling the mEMVTransaction to connect to the reader");
                    showAvailableDevices();
                } else if (mReaderConnectButton.getText().toString().equalsIgnoreCase(MainActivity.this.getString(R.string.reader_disconnect))) {
                    disconnectTheEMVDevice();
                } else if(mReaderConnectButton.getText().toString().equalsIgnoreCase(getString(R.string.reader_update))){
                    updateReader();
                }
            }
        });

        mChargeButton = (Button) findViewById(R.id.charge_button);
        mChargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if(validateInput(true)) {
                    if (LocalPreferences.isTakePaymentAfterUserInsertsOrTapsCard()) {
                        mTransactionManager.activateReaderForPayment();
                    } else {
                        startTakingPayment();
                    }
                }
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
            case R.id.action_settings:{
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_device_details: {
                Log.d(LOG_TAG, "onMenuItemSelected:action_device_details");
                Intent intent = new Intent(MainActivity.this,DeviceDetailsActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_about: {
                Log.d(LOG_TAG, "onMenuItemSelected:action_about");
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
            default: {

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startTransaction() {
        clearActiveInvoice();
        mInvoice = mTransactionManager.beginPayment(this);
        mItem = DomainFactory.newInvoiceItem("TestItem", "1234", BigDecimal.ZERO);
        mInvoice.addItem(mItem, BigDecimal.ONE);
    }

    private void updateInvoice(){
        //update the invoice
        BigDecimal amount = (validateInput(false)) ? new BigDecimal(mAmount) : BigDecimal.ZERO;
        validateTip();
        mItem.setPrice(amount);
        Tip tip = new Tip(Tip.Type.AMOUNT,new BigDecimal(mTip));
        mInvoice.setTip(tip);
        mInvoice.recalculate();
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
        startTransaction();
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

    private boolean isValidAmount(String amountText) {
        if(amountText == null || amountText.length() <= 0) {
            return false;
        }

        String regExp = "[0-9]+([.][0-9]*)?";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(amountText);
        return matcher.find();
    }

    private boolean validateInput(boolean isChargePressed){
        if (!mIsReaderDeviceConnected) {
            if (isChargePressed) {
                showAlertDialog(R.string.merchant_error_title,R.string.no_charge_because_reader_not_connected,false);
            }
            return false;
        }

        String amountText = mChargeAmount.getText().toString();
        if (null != amountText && amountText.length() > 0) {
            amountText = String.format("%.2f", Double.parseDouble(amountText));
            mAmount = Double.parseDouble(amountText);
            return true;
        }else{
            mAmount = 0;
            if (isChargePressed) {
                showAlertDialog(R.string.merchant_error_title,R.string.no_charge_because_amount_not_valid,false);
            }
            return false;
        }
    }

    private void validateTip(){
        String tipText = mTipAmount.getText().toString();
        if (null != tipText && tipText.length() > 0) {
            tipText = String.format("%.2f", Double.parseDouble(tipText));
            mTip = Double.parseDouble(tipText);
        }else{
            mTip = 0;
        }
    }

    private void startTakingPayment(){

        if(mIsProcessingPayment || !validateInput(true)){
            return;
        }

        updateInvoice();
        mIsProcessingPayment = true;
        mTransactionManager.processPaymentWithSDKUI(TransactionManager.PaymentType.CardReader, new CallbackHandler());
    }

    private void showAlertDialogWithActivity(Activity activity, int titleResID, int msgResID, DialogInterface.OnClickListener clickListener){
        Log.d(LOG_TAG, "showAlertDialogWithActivity IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titleResID);
        builder.setMessage(msgResID);
        builder.setCancelable(false);
        builder.setNeutralButton(com.paypal.merchant.sdk.R.string.sdk_OK, clickListener);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
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

    private void showAlertDialogWithTwoButtonOptions(Activity activity, int titleResID, int msgResID,
                                                     int positiveButtonResId, int negativeButtonResId,
                                                     final DialogInterface.OnClickListener posClickListener, final DialogInterface.OnClickListener negClickListener){
        Log.d(LOG_TAG, "showAlertDialogWithActivity IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titleResID);
        builder.setMessage(msgResID);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveButtonResId, posClickListener);
        builder.setNegativeButton(negativeButtonResId, negClickListener);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    private void disconnectTheEMVDevice() {
        if (null != mSelectedBluetoothDevice) {
            PayPalHereSDK.getCardReaderManager().disconnectFromDevice(mSelectedBluetoothDevice);
            mReaderStatusButton.setText(mReaderNotConnectedText);
            mReaderStatusButton.setTextColor(Color.RED);
            mReaderConnectButton.setText(R.string.reader_connect);
        }
    }

    private void updateReader(){
        PayPalHereSDK.getCardReaderManager().initiateSoftwareUpdate(MainActivity.this, mSelectedBluetoothDevice, new DefaultResponseHandler<Boolean, PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>>() {
            @Override
            public void onSuccess(Boolean responseObject) {
                Log.d(LOG_TAG, "updateReader: response handler onSuccess");
                mSoftwareUpdateRequired = false;
                updateReaderConnectedMessage(true);
            }

            @Override
            public void onError(PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus> error) {
                mSoftwareUpdateRequired = true;
                updateReaderConnectedMessage(true);
            }
        });
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
        mRetryCount = 0;
        if (null != mChargeAmount) {
            mChargeAmount.setText("");
        }
    }

    private void updateReaderConnectedMessage(boolean connected){
        if(null != mReaderConnectButton && null != mReaderStatusButton) {
            if (connected) {
                mReaderStatusButton.setText(mReaderConnectedText);
                mReaderStatusButton.setTextColor(Color.GREEN);
                mIsReaderDeviceConnected = true;
                mReaderConnectButton.setText(R.string.reader_disconnect);
            } else {
                mReaderStatusButton.setText(mReaderNotConnectedText);
                mReaderStatusButton.setTextColor(Color.RED);
                mIsReaderDeviceConnected = false;
                mReaderConnectButton.setText(R.string.reader_connect);
            }

            if(mSoftwareUpdateRequired){
                mReaderConnectButton.setText(R.string.reader_update);
            }
        }
    }

    private void connectDevice(final BluetoothDevice device){
        mSelectedBluetoothDevice = device;
        PayPalHereSDK.getCardReaderManager().connectToDevice(MainActivity.this, device, new DefaultResponseHandler<BluetoothDevice, PPError<CardReaderManager.ChipAndPinConnectionStatus>>() {

            @Override
            public void onSuccess(BluetoothDevice responseObject) {
                Log.d(LOG_TAG, " connectDevice Response Handler: onSuccess");
                mSelectedBluetoothDevice = responseObject;
                updateReaderConnectedMessage(true);
            }

            @Override
            public void onError(PPError<CardReaderManager.ChipAndPinConnectionStatus> error) {
                Log.d(LOG_TAG, "connectDevice Response Handler: onError: " + error.getErrorCode());
                if (CardReaderManager.ChipAndPinConnectionStatus.ErrorSoftwareUpdateRequired == error.getErrorCode()
                        || CardReaderManager.ChipAndPinConnectionStatus.ErrorSoftwareUpdateTriedAndFailed == error.getErrorCode()) {
                    Log.d(LOG_TAG, "connectDevice Response Handler: onError: SoftwareUpdate Recommended. Hence allowing to take payments..");
                    mSoftwareUpdateRequired = true;
                    updateReaderConnectedMessage(true);
                } else if (CardReaderManager.ChipAndPinConnectionStatus.ErrorSoftwareUpdateRecommended == error.getErrorCode()) {
                    Log.e(LOG_TAG, "connectDevice Response Handler: onError. Not allowing to take payments");
                    mSoftwareUpdateRequired = true;
                    updateReaderConnectedMessage(true);
                } else if (CardReaderManager.ChipAndPinConnectionStatus.ConnectedAndReady == error.getErrorCode()) {
                    mSoftwareUpdateRequired = false;
                    updateReaderConnectedMessage(true);
                }
            }
        });
    }

    private void showAvailableDevices(){
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() <= 0) {
            showNoPairedDevicesAlertDialog();
        } else {
            showAlertDialogWithPairedDevices(pairedDevices);
        }
    }

    private void showAlertDialogWithPairedDevices(final Set<BluetoothDevice> deviceSet) {
        Log.d(LOG_TAG, "showAlertDialogWithPairedDevices IN");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, com.paypal.merchant.sdk.R.layout.sdk_device_name);
        final ArrayList<BluetoothDevice> deviceArray = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : deviceSet) {
            String deviceName = device.getName();
            if (deviceName.contains("PayPal")) {
                Log.d(LOG_TAG, "Adding the device: " + deviceName + " to the adapter");
                adapter.add(deviceName);
                deviceArray.add(device);
            }
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(com.paypal.merchant.sdk.R.string.sdk_dlg_title_paired_devices);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
                final BluetoothDevice device = deviceArray.get(i);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_TAG,"showAlertDialogWithPairedDevices device selected onClick");
                        connectDevice(device);
                    }
                }).run();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mCurrentDisplayDialog.dismiss();
                mCurrentDisplayDialog = null;
                finish();
            }
        });
        mCurrentDisplayDialog = builder.create();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentDisplayDialog.show();
            }
        });
    }

    private void showNoPairedDevicesAlertDialog() {
        Log.d(LOG_TAG, "showNoPairedDevicesAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(com.paypal.merchant.sdk.R.string.sdk_dlg_title_paired_devices);
        builder.setMessage(com.paypal.merchant.sdk.R.string.sdk_dlg_msg_no_paired_devices);
        builder.setCancelable(false);
        builder.setNeutralButton(com.paypal.merchant.sdk.R.string.sdk_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "showNoPairedDevicesAlertDialog:run:onClick IN");
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
            }
        });
        mCurrentDisplayDialog = builder.create();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentDisplayDialog.show();
            }
        });
    }

    @Override
    public void onPrintRequested(Activity activity, Invoice invoice) {
        Log.d(LOG_TAG,"printReceiptRequested");
        Intent intent = new Intent(MainActivity.this,PrintReceiptActivity.class);
        startActivity(intent);
    }


    @Override
    public void onPaymentEvent(PaymentEvent e) {
        Log.d(LOG_TAG,"onPaymentEvent: "+e.getEventType());
    }

    @Override
    public TransactionControlAction onPreAuthorize(Invoice inv, String preAuthJSON) {
        return TransactionControlAction.CONTINUE;
    }

    @Override
    public void onPostAuthorize(boolean didFail) {

    }

    @Override
    public Activity getCurrentActivity() {
        return this;
    }

    @Override
    public com.paypal.merchant.sdk.domain.SDKSignatureScreenOptions getSignatureScreenOpts() {
        return new SDKSignatureScreenOptions() {
            @Override
            public boolean isFullScreen() {
                return LocalPreferences.isSignatureInFullScreen();
            }
        };
    }

    @Override
    public SDKReceiptScreenOptions getReceiptScreenOptions() {
        return new SDKReceiptScreenOptions() {
            @Override
            public boolean isFullScreen() {
                return LocalPreferences.isReceiptOptionsInFullScreen();
            }

            @Override
            public boolean isSubsequentScreensAsFullScreens() {
                return LocalPreferences.isReceiptsInFullScreen();
            }

            @Override
            public Map<String, SDKReceiptScreenOptions.SDKTransactionScreenOptionCallback> getScreenOptions() {
                return null;
            }
        };
    }

    @Override
    public void onUserPaymentOptionSelected(PaymentOption paymentOption) {
        startTakingPayment();
    }

    @Override
    public void onUserRefundOptionSelected(PaymentOption paymentOption) {

    }

    @Override
    public void onTokenExpired(Activity activity, TokenExpirationHandler listener) {

    }

    @Override
    public void onReadyToCancelTransaction(CancelTransactionReason cancelTransactionReason) {

    }

    @Override
    public void onContactlessReaderTimeout(Activity activity, final ContactlessReaderTimeoutOptionsHandler handler) {
        showAlertDialogWithTwoButtonOptions(activity, R.string.error_title_contactless_reader_timeout, R.string.error_message_contactless_reader_timeout,
                R.string.contactless_reader_timeout_try_again, R.string.contactless_reader_timeout_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRetryCount++;
                        handler.onTimeout(onTimeout());
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.onTimeout(ContactlessReaderTimeoutOptions.CANCEL_TRANSACTION);
                    }
                });
    }

    private ContactlessReaderTimeoutOptions onTimeout() {
        ContactlessReaderTimeoutOptions options;
        switch (mRetryCount) {
            case 0:
            case 1:
                options = ContactlessReaderTimeoutOptions.RETRY_WITH_CONTACTLESS;
                break;

            case 2:
            default:
                options = ContactlessReaderTimeoutOptions.RETRY_WITHOUT_CONTACTLESS;
                break;
        }
        return options;
    }

    private void clearActiveInvoice() {
        mInvoice = null;
    }

    private class CallbackHandler implements DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>> {
        @Override
        public void onSuccess(TransactionManager.PaymentResponse responseObject) {
            Log.d(LOG_TAG, "CallbackHandler onSuccess");
            if(null != responseObject && null != responseObject.getTransactionRecord()) {
                LocalPreferences.storeCompletedTransactionRecord(responseObject.getTransactionRecord());
            }
            mIsProcessingPayment = false;
            startTransaction();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearChargeAmount();
                }
            });
        }

        @Override
        public void onError(final PPError<TransactionManager.PaymentErrors> error) {
            Log.e(LOG_TAG, "CallbackHandler onFailure");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsProcessingPayment = false;
                    startTransaction();
                    clearChargeAmount();
                    if(TransactionManager.PaymentErrors.AmountTooLow == error.getErrorCode()){
                        Log.d(LOG_TAG, "payment error :  AmountTooLow");
                    }else if(TransactionManager.PaymentErrors.AmountTooHigh == error.getErrorCode()){
                        Log.d(LOG_TAG, "payment error :  AmountTooHigh");
                    }else if(TransactionManager.PaymentErrors.BatteryLow == error.getErrorCode()){
                        showAlertDialog(R.string.merchant_error_title,R.string.merchant_battery_too_low,false);
                    }else if(TransactionManager.PaymentErrors.MandatorySoftwareUpdateRequired == error.getErrorCode()){
                        showAlertDialog(R.string.merchant_error_title,R.string.error_mandatory_software_update,false);
                    }else if(TransactionManager.PaymentErrors.UpgradePPHSDK == error.getErrorCode()){
                        showAlertDialog(R.string.merchant_error_title,R.string.error_upgrade_sdk,false);
                    }
                }
            });
        }
    }
}
