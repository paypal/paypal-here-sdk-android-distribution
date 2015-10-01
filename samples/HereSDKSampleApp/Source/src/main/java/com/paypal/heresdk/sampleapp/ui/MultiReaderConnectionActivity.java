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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapper;
import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapperCallbacks;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.domain.PPError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultiReaderConnectionActivity extends Activity {

    private static final String LOG_TAG = MultiReaderConnectionActivity.class.getSimpleName();

    private TextView mReadersConnectedMsg;
    private LinearLayout mActiveReaderLayout;
    private TextView mActiveReaderTextView;
    private Button mActiveReaderChangeButton;
    private Button mEMVReaderConnectButton;
    private Button mProceedFurtherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_multi_reader_connection);

        mReadersConnectedMsg = (TextView) findViewById(R.id.id_readers_connected_msg);
        mActiveReaderLayout = (LinearLayout) findViewById(R.id.id_active_reader_layout);
        mActiveReaderTextView = (TextView) findViewById(R.id.id_active_reader);
        mActiveReaderChangeButton = (Button) findViewById(R.id.id_active_reader_change_button);
        mEMVReaderConnectButton = (Button) findViewById(R.id.id_emv_reader_connect_button);
        mProceedFurtherButton = (Button) findViewById(R.id.id_proceed_button);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");

        setConnectionListener();
        enableCorrectLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }

    public void onContinueToChargeScreen(View view){
        Log.d(LOG_TAG, "onContinueToChargeScreen");
        Intent chargeActivityIntent = new Intent(this,ChargeActivity.class);
        startActivity(chargeActivityIntent);
    }

    public void onConnectToEMVReader(View view) {
        Log.d(LOG_TAG, "onConnectToEMVReader");
        showAvailableDevices();
    }

    public void onChangeActiveReader(View view){
        List<CardReaderManager.CardReader> cardReaders = PayPalHereSDKWrapper.getInstance().getConnectedReaders();
        ArrayList<CardReaderListener.ReaderTypes> readerTypes = new ArrayList<CardReaderListener.ReaderTypes>();
        for(CardReaderManager.CardReader reader: cardReaders){
            readerTypes.add(reader.getReaderType());
        }

        chooseActiveReader(readerTypes);
    }

    private void enableCorrectLayout() {
        boolean isAudioJackReaderConnected = PayPalHereSDKWrapper.getInstance().isMagstripeReaderConnected();
        boolean isEMVReaderConnected = PayPalHereSDKWrapper.getInstance().isEMVReaderConnected();

        if (isAudioJackReaderConnected && isEMVReaderConnected) {
            mReadersConnectedMsg.setText(R.string.multi_readers_both_connected_msg);
            mEMVReaderConnectButton.setVisibility(View.GONE);

            mActiveReaderLayout.setVisibility(View.VISIBLE);
            mActiveReaderChangeButton.setVisibility(View.VISIBLE);
            mProceedFurtherButton.setVisibility(View.VISIBLE);

            updateActiveReaderOnUI();
        } else if (isAudioJackReaderConnected) {
            mReadersConnectedMsg.setText(R.string.multi_readers_audio_jack_connected_msg);
            mEMVReaderConnectButton.setVisibility(View.VISIBLE);

            mActiveReaderLayout.setVisibility(View.VISIBLE);
            mActiveReaderChangeButton.setVisibility(View.GONE);
            mProceedFurtherButton.setVisibility(View.VISIBLE);

            updateActiveReaderOnUI();
        } else if (isEMVReaderConnected) {
            mReadersConnectedMsg.setText(R.string.multi_readers_emv_connected_msg);
            mEMVReaderConnectButton.setVisibility(View.GONE);

            mActiveReaderLayout.setVisibility(View.VISIBLE);
            mActiveReaderChangeButton.setVisibility(View.GONE);
            mProceedFurtherButton.setVisibility(View.VISIBLE);

            updateActiveReaderOnUI();
        } else {
            mReadersConnectedMsg.setText(R.string.multi_readers_no_reader_connected);
            mEMVReaderConnectButton.setVisibility(View.VISIBLE);

            mActiveReaderLayout.setVisibility(View.GONE);
            mActiveReaderChangeButton.setVisibility(View.GONE);
            mProceedFurtherButton.setVisibility(View.GONE);
        }
    }

    private void updateActiveReaderOnUI() {
        CardReaderListener.ReaderTypes readerType = PayPalHereSDKWrapper.getInstance().getActiveReader();
        if (null != readerType && readerType.equals(CardReaderListener.ReaderTypes.ChipAndPinReader)) {
            mActiveReaderTextView.setText(R.string.active_reader_emv);
        } else if (null != readerType && readerType.equals(CardReaderListener.ReaderTypes.MagneticCardReader)) {
            mActiveReaderTextView.setText(R.string.active_reader_audio_jack);
        }
    }

    private String getReaderName(CardReaderListener.ReaderTypes readerType) {
        if (readerType.equals(CardReaderListener.ReaderTypes.ChipAndPinReader)) {
            return "EMV Reader";
        } else if (readerType.equals(CardReaderListener.ReaderTypes.MagneticCardReader)) {
            return "Audio Jack Reader";
        }
        return "None";
    }

    private void chooseActiveReader(final List<CardReaderListener.ReaderTypes> readerTypes) {
        Log.d(LOG_TAG, "chooseActiveReader IN");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, com.paypal.merchant.sdk.R.layout.sdk_device_name);
        final ArrayList<BluetoothDevice> deviceArray = new ArrayList<BluetoothDevice>();
        for (CardReaderListener.ReaderTypes reader : readerTypes) {
            adapter.add(getReaderName(reader));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_reader_title);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                CardReaderListener.ReaderTypes readerType = readerTypes.get(i);
                PayPalHereSDKWrapper.getInstance().setActiveReader(readerType);
                enableCorrectLayout();
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

    private void setConnectionListener() {
        PayPalHereSDKWrapper.getInstance().setListener(this, new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onMagstripeReaderConnected() {
                Log.d(LOG_TAG, "onMagstripeReaderConnected");
                enableCorrectLayout();
            }

            @Override
            public void onMagstripeReaderDisconnected() {
                Log.d(LOG_TAG, "onMagstripeReaderDisconnected");
                enableCorrectLayout();
            }

            @Override
            public void onEMVReaderDisconnected() {
                Log.d(LOG_TAG, "onEMVReaderDisconnected");
                enableCorrectLayout();
            }

            @Override
            public void onEMVReaderConnected() {
                Log.d(LOG_TAG, "onEMVReaderConnected");
                enableCorrectLayout();
            }

            @Override
            public void onMultipleCardReadersConnected(List<CardReaderListener.ReaderTypes> readerList) {
                Log.d(LOG_TAG, "onMultipleCardReadersConnected. Size: " + readerList.size());
                chooseActiveReader(readerList);
            }
        });
    }

    /*
     * EMV Device Connection related UI
     */
    private void showAvailableDevices() {
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

    private void connectToEMVReader(final BluetoothDevice device) {
        PayPalHereSDKWrapper.getInstance().connectoToEMVReader(this, device, new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onEMVReaderConnected() {
                Log.d(LOG_TAG, "connectToEMVReader onEMVReaderConnected");
                enableCorrectLayout();
            }

            @Override
            public void onEMVReaderConnectionFailure(PPError<CardReaderManager.ChipAndPinConnectionStatus> error) {
                Log.d(LOG_TAG, "connectToEMVReader onEMVReaderConnectionFailure error: " + error);
                enableCorrectLayout();
            }
        });
    }
}
