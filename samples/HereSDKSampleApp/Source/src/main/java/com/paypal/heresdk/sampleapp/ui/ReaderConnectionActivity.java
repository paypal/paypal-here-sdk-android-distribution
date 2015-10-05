package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapper;
import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapperCallbacks;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.domain.PPError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReaderConnectionActivity extends Activity {

    private static final String LOG_TAG = ReaderConnectionActivity.class.getSimpleName();
    public static final String INTENT_STRING_EMV_READER = "EMV_READER";
    public static final String INTENT_STRING_AUDIO_JACK_READER = "AUDIO_JACK_READER";

    private boolean mIsEMVReader = false;
    private boolean mIsAudioJackReader = false;

    private LinearLayout mAudioReaderConnectLayout;
    private LinearLayout mAudioReaderConnectedLayout;
    private LinearLayout mEMVReaderConnectLayout;
    private LinearLayout mEMVReaderSWUpdateLayout;
    private LinearLayout mEMVReaderConnectedLayout;

    private BluetoothDevice mSelectedBluetoothDevice;
    private boolean mSoftwareUpdateRequired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_reader_connection);

        Intent intent = getIntent();

        if (intent.hasExtra(INTENT_STRING_EMV_READER)) {
            mIsEMVReader = intent.getBooleanExtra(INTENT_STRING_EMV_READER, false);
        }

        if (intent.hasExtra(INTENT_STRING_AUDIO_JACK_READER)) {
            mIsAudioJackReader = intent.getBooleanExtra(INTENT_STRING_AUDIO_JACK_READER, false);
        }

        mAudioReaderConnectLayout = (LinearLayout) findViewById(R.id.id_audio_reader_connect_layout);
        mAudioReaderConnectedLayout = (LinearLayout) findViewById(R.id.id_audio_reader_connected_layout);
        mEMVReaderConnectLayout = (LinearLayout) findViewById(R.id.id_emv_reader_connect_layout);
        mEMVReaderSWUpdateLayout = (LinearLayout) findViewById(R.id.id_emv_reader_update_layout);
        mEMVReaderConnectedLayout = (LinearLayout) findViewById(R.id.id_emv_reader_connected_layout);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume");

        setConnectionListener();
        enableCorrectLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }

    private void enableCorrectLayout(){
        if(mIsAudioJackReader){
            if(PayPalHereSDKWrapper.getInstance().isMagstripeReaderConnected()){
                makeAudioJackReaderActive();
                showMagstripeReaderConnectedLayout();
            }else{
                showMagstripeReaderConnectLayout();
            }
        }else if(mIsEMVReader){
            if(PayPalHereSDKWrapper.getInstance().isEMVReaderConnected()){
                makeEMVReaderActive();
                showEMVReaderConnectedLayout();
            }else{
                if(mSoftwareUpdateRequired){
                    showEMVReaderSWUpdateLayout();
                }else {
                    showEMVReaderConnectLayout();
                }
            }
        }
    }

    private void makeAudioJackReaderActive(){
        if(PayPalHereSDKWrapper.getInstance().getActiveReader().equals(CardReaderListener.ReaderTypes.MagneticCardReader)){
            Log.d(LOG_TAG,"Audio Jack Reader is already active reader. So nothing to do here..");
            return;
        }

        List<CardReaderManager.CardReader> cardReaders = PayPalHereSDKWrapper.getInstance().getConnectedReaders();
        for(CardReaderManager.CardReader reader: cardReaders){
            if(reader.getReaderType().equals(CardReaderListener.ReaderTypes.MagneticCardReader)){
                PayPalHereSDKWrapper.getInstance().setActiveReader(reader.getReaderType());
            }
        }
    }

    private void makeEMVReaderActive(){
        if(PayPalHereSDKWrapper.getInstance().getActiveReader().equals(CardReaderListener.ReaderTypes.ChipAndPinReader)){
            Log.d(LOG_TAG,"Audio Jack Reader is already active reader. So nothing to do here..");
            return;
        }

        List<CardReaderManager.CardReader> cardReaders = PayPalHereSDKWrapper.getInstance().getConnectedReaders();
        for(CardReaderManager.CardReader reader: cardReaders){
            if(reader.getReaderType().equals(CardReaderListener.ReaderTypes.ChipAndPinReader)){
                PayPalHereSDKWrapper.getInstance().setActiveReader(reader.getReaderType());
            }
        }
    }

    private void setConnectionListener(){
        PayPalHereSDKWrapper.getInstance().setListener(this, new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onMagstripeReaderConnected() {
                Log.d(LOG_TAG, "onMagstripeReaderConnected");
                if(mIsAudioJackReader) {
                    showMagstripeReaderConnectedLayout();
                }
            }

            @Override
            public void onMagstripeReaderDisconnected() {
                Log.d(LOG_TAG, "onMagstripeReaderDisconnected");
                if(mIsAudioJackReader) {
                    showMagstripeReaderConnectLayout();
                }
            }

            @Override
            public void onEMVReaderDisconnected() {
                Log.d(LOG_TAG, "onEMVReaderDisconnected");
                if(mIsEMVReader) {
                    showEMVReaderConnectLayout();
                }
            }

            @Override
            public void onEMVReaderConnected() {
                Log.d(LOG_TAG, "onEMVReaderConnected");
                if(mIsEMVReader) {
                    showEMVReaderConnectedLayout();
                }
            }

            @Override
            public void onMultipleCardReadersConnected(List<CardReaderListener.ReaderTypes> readerList) {
                Log.d(LOG_TAG,"onMultipleCardReadersConnected. Size: "+readerList.size());
                if(mIsAudioJackReader){
                    for(CardReaderListener.ReaderTypes readerType: readerList){
                        if(readerType.equals(CardReaderListener.ReaderTypes.MagneticCardReader)){
                            PayPalHereSDKWrapper.getInstance().setActiveReader(readerType);
                            break;
                        }
                    }
                }else if(mIsEMVReader){
                    for(CardReaderListener.ReaderTypes readerType: readerList){
                        if(readerType.equals(CardReaderListener.ReaderTypes.ChipAndPinReader)){
                            PayPalHereSDKWrapper.getInstance().setActiveReader(readerType);
                            break;
                        }
                    }
                }
            }
        });
    }

    public void onContinueToChargeScreen(View view){
        Log.d(LOG_TAG, "onContinueToChargeScreen");
        Intent chargeActivityIntent = new Intent(ReaderConnectionActivity.this,ChargeActivity.class);
        chargeActivityIntent.putExtra(INTENT_STRING_EMV_READER,mIsEMVReader);
        chargeActivityIntent.putExtra(INTENT_STRING_AUDIO_JACK_READER,mIsAudioJackReader);
        startActivity(chargeActivityIntent);
    }

    public void onConnectToEMVReader(View view){
        Log.d(LOG_TAG, "onConnectToEMVReader");
        showAvailableDevices();
    }

    public void onUpdateSoftwareOnEMVReader(View view){
        Log.d(LOG_TAG, "onUpdateSoftwareOnEMVReader");
    }

    public void onDisconnectEMVReader(View view){
        Log.d(LOG_TAG, "onDisconnectEMVReader");
        PayPalHereSDKWrapper.getInstance().disConnectEMVReader(mSelectedBluetoothDevice);
    }

    public void showMagstripeReaderConnectLayout() {
        mAudioReaderConnectLayout.setVisibility(View.VISIBLE);
        mAudioReaderConnectedLayout.setVisibility(View.GONE);
        mEMVReaderConnectLayout.setVisibility(View.GONE);
        mEMVReaderSWUpdateLayout.setVisibility(View.GONE);
        mEMVReaderConnectedLayout.setVisibility(View.GONE);
    }

    public void showMagstripeReaderConnectedLayout() {
        mAudioReaderConnectLayout.setVisibility(View.GONE);
        mAudioReaderConnectedLayout.setVisibility(View.VISIBLE);
        mEMVReaderConnectLayout.setVisibility(View.GONE);
        mEMVReaderSWUpdateLayout.setVisibility(View.GONE);
        mEMVReaderConnectedLayout.setVisibility(View.GONE);
    }

    public void showEMVReaderConnectLayout() {
        mAudioReaderConnectLayout.setVisibility(View.GONE);
        mAudioReaderConnectedLayout.setVisibility(View.GONE);
        mEMVReaderConnectLayout.setVisibility(View.VISIBLE);
        mEMVReaderSWUpdateLayout.setVisibility(View.GONE);
        mEMVReaderConnectedLayout.setVisibility(View.GONE);
    }

    public void showEMVReaderSWUpdateLayout() {
        mAudioReaderConnectLayout.setVisibility(View.GONE);
        mAudioReaderConnectedLayout.setVisibility(View.GONE);
        mEMVReaderConnectLayout.setVisibility(View.GONE);
        mEMVReaderSWUpdateLayout.setVisibility(View.VISIBLE);
        mEMVReaderConnectedLayout.setVisibility(View.GONE);
    }

    public void showEMVReaderConnectedLayout() {
        mAudioReaderConnectLayout.setVisibility(View.GONE);
        mAudioReaderConnectedLayout.setVisibility(View.GONE);
        mEMVReaderConnectLayout.setVisibility(View.GONE);
        mEMVReaderSWUpdateLayout.setVisibility(View.GONE);
        mEMVReaderConnectedLayout.setVisibility(View.VISIBLE);
    }

    /*
     * EMV Device Connection related UI
     */
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
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, com.paypal.merchant.sdk.R.layout.sdk_device_name);
        final ArrayList<BluetoothDevice> deviceArray = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : deviceSet) {
            String deviceName = device.getName();
            if (deviceName.contains("PayPal")) {
                Log.d(LOG_TAG, "Adding the device: " + deviceName + " to the adapter");
                adapter.add(deviceName);
                deviceArray.add(device);
            }
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(com.paypal.merchant.sdk.R.string.sdk_dlg_title_paired_devices);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final BluetoothDevice device = deviceArray.get(i);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_TAG, "showAlertDialogWithPairedDevices device selected onClick");
                        connectToEMVReader(device);
                    }
                }).run();
            }
        });

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showNoPairedDevicesAlertDialog() {
        Log.d(LOG_TAG, "showNoPairedDevicesAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(com.paypal.merchant.sdk.R.string.sdk_dlg_title_paired_devices);
        builder.setMessage(com.paypal.merchant.sdk.R.string.sdk_dlg_msg_no_paired_devices);

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setNeutralButton(com.paypal.merchant.sdk.R.string.sdk_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "showNoPairedDevicesAlertDialog:run:onClick IN");
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void connectToEMVReader(final BluetoothDevice device){
        mSelectedBluetoothDevice = device;
        PayPalHereSDKWrapper.getInstance().connectoToEMVReader(this, device, new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onEMVReaderConnected() {
                Log.d(LOG_TAG, "connectToEMVReader onEMVReaderConnected");
                showEMVReaderConnectedLayout();
            }

            @Override
            public void onEMVReaderConnectionFailure(PPError<CardReaderManager.ChipAndPinConnectionStatus> error) {
                Log.d(LOG_TAG, "connectToEMVReader onEMVReaderConnectionFailure error: " + error);
                if (CardReaderManager.ChipAndPinConnectionStatus.ErrorSoftwareUpdateRequired == error.getErrorCode()
                        || CardReaderManager.ChipAndPinConnectionStatus.ErrorSoftwareUpdateTriedAndFailed == error.getErrorCode()) {
                    mSoftwareUpdateRequired = true;
                    showEMVReaderSWUpdateLayout();
                } else if (CardReaderManager.ChipAndPinConnectionStatus.ErrorSoftwareUpdateRecommended == error.getErrorCode()) {
                    mSoftwareUpdateRequired = true;
                    showEMVReaderSWUpdateLayout();
                } else if (CardReaderManager.ChipAndPinConnectionStatus.ConnectedAndReady == error.getErrorCode()) {
                    mSoftwareUpdateRequired = false;
                    showEMVReaderConnectedLayout();
                }
            }
        });
    }
}
