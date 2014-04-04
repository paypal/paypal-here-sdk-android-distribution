/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.ChipAndPinDecisionEvent;
import com.paypal.merchant.sdk.domain.ChipAndPinStatusUpdateHandler;
import com.paypal.merchant.sdk.domain.EMVDeviceData;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

import java.util.Date;
import java.util.List;

public class EMVOnlyActivity extends MyActivity implements CardReaderListener {
    private static final String LOG = "EMVOnlyAcitivity";
    /**
     * Connection handler implementation.
     * This handler receives events while the device is trying to connect to the terminal and checks with the backend
     * if a software update on the terminal is required or not.
     */
    private ChipAndPinStatusUpdateHandler<CardReaderManager.ChipAndPinStatusResponse,
                PPError<CardReaderManager.ChipAndPinConnectionStatus>> mConnectionHandler
            = new ChipAndPinStatusUpdateHandler<CardReaderManager.ChipAndPinStatusResponse,
            PPError<CardReaderManager.ChipAndPinConnectionStatus>>() {
        @Override
        public void onInitiated(CardReaderManager.ChipAndPinStatusResponse object,
                                PPError<CardReaderManager.ChipAndPinConnectionStatus> updatedStatus) {
            showEMVStatusDialog("Detecting Card Reader...");
            Log.d(LOG, "connection handler : on initiated");
        }

        @Override
        public void onStatusUpdated(CardReaderManager.ChipAndPinStatusResponse object,
                                    PPError<CardReaderManager.ChipAndPinConnectionStatus> updatedStatus) {
            CardReaderManager.ChipAndPinConnectionStatus error = updatedStatus.getErrorCode();
            Log.d(LOG, "connection handler : on status updated : " + error.name());
            switch (error) {
                case ConnectionInProgress:
                    showEMVStatusDialog("Connecting to Card Reader...");
                    break;

                case ConnectedAndConfiguring:
                    dismissProgressDialog();
                    break;

                case TerminalInfoAvailable:
                    fetchTerminalInfo();
                    break;


            }
        }

        @Override
        public void onCompleted(CardReaderManager.ChipAndPinStatusResponse object,
                                PPError<CardReaderManager.ChipAndPinConnectionStatus> updatedStatus) {
            CardReaderManager.ChipAndPinConnectionStatus status = updatedStatus.getErrorCode();
            Log.d(LOG, "connection handler : on completed : " + status.name());
            switch (status) {

                case ErrorSoftwareUpdateRequired:
                    showEMVActionDialog("Software update is required!", true);
                    break;

                case ErrorSoftwareUpdateRecommended:
                    showEMVActionDialog("Software update is available for you to install.", false);
                    break;

                case ConnectedAndReady:
                    dismissProgressDialog();
                    dismissAlertDialog();
                    CommonUtils.createToastMessage(EMVOnlyActivity.this, "Software is upto date!");
                    mGetBatteryLevelButton.setEnabled(true);
                    break;
            }
        }
    };
    /**
     * Update handler implementation.
     * This handler receives events while the device is trying to update the software on the terminal.
     */
    private ChipAndPinStatusUpdateHandler<CardReaderManager.ChipAndPinStatusResponse,
            PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>> mUpdateHandler
            = new ChipAndPinStatusUpdateHandler<CardReaderManager.ChipAndPinStatusResponse,
            PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>>() {


        @Override
        public void onInitiated(CardReaderManager.ChipAndPinStatusResponse chipAndPinStatusResponse,
                                PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>
                                        chipAndPinStatusErrorsPPError) {

            showEMVStatusDialog("Starting software update...");
            Log.d(LOG, "update handler : on initiated.");
        }

        @Override
        public void onStatusUpdated(CardReaderManager.ChipAndPinStatusResponse chipAndPinStatusResponse,
                                    PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>
                                            chipAndPinStatusErrorsPPError) {

            CardReaderManager.ChipAndPinSoftwareUpdateStatus chipAndPinStatusErrors = chipAndPinStatusErrorsPPError
                    .getErrorCode();

            Log.d(LOG, "update handler : on status updated : " + chipAndPinStatusErrors.name());

            switch (chipAndPinStatusErrors) {

                case DownloadInProgress:

                    showEMVStatusDialog(((chipAndPinStatusResponse == null) ? "Downloading..." :
                            chipAndPinStatusResponse.getDisplayMessage()));
                    break;
                case InstallInProgress:
                case SoftwareUpdateInProgress:
                case KeyInjectionInProgress:
                    showEMVStatusDialog(((chipAndPinStatusResponse == null) ? "Installing..." :
                            chipAndPinStatusResponse.getDisplayMessage()));
                    break;
                case RestartingTerminal:
                    showEMVStatusDialog("Restarting the terminal...");
                    break;

            }
        }

        @Override
        public void onCompleted(CardReaderManager.ChipAndPinStatusResponse chipAndPinStatusResponse,
                                PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>
                                        chipAndPinStatusErrorsPPError) {

            CardReaderManager.ChipAndPinSoftwareUpdateStatus chipAndPinStatusErrors = chipAndPinStatusErrorsPPError
                    .getErrorCode();

            Log.d(LOG, "completed handler : " + chipAndPinStatusErrors.name());

            switch (chipAndPinStatusErrors) {
                case KeyInjectionFailedUnknownError:
                case SoftwareUpdateFailedUnknownError:
                case KeyInjectionFailedWithCause:
                case SoftwareUpdateFailedWithCause:
                    dismissAlertDialog();
                    dismissProgressDialog();
                    showSWUpdateFailedDialog(chipAndPinStatusResponse.getDisplayMessage());
                    break;
                case UpdatedAndReady:
                    // Show a toast dialog indicating that the software update was successfully completed.
                    CommonUtils.createToastMessage(EMVOnlyActivity.this, "Software update successfully completed!");
                    dismissAlertDialog();
                    dismissProgressDialog();
                    break;
            }
        }
    };
    private static final int BT_REQ_CODE = 1234;
    private BluetoothDevice mEMVDevice;
    private BluetoothAdapter mBTAdapter;
    private CardReaderManager mPeripheralsManager;
    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private Button mDisconnectBTDeviceButton;
    private Button mGetBatteryLevelButton;
    private Button mGetEmvDeviceDataButton;
    private TextView mDeviceName;
    private TextView mSerialNumber;
    private TextView mFirmwareVersion;
    private TextView mOSVersion;
    private TextView mLastKnowBatteryLevel;
    private TextView mConnectionStatus;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emv_only);

        Button b = (Button) findViewById(R.id.bt_scan_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForBTDevices();
            }
        });

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Action required!");
        mAlertDialog = builder.create();

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        mPeripheralsManager = PayPalHereSDK.getCardReaderManager();

        mDisconnectBTDeviceButton = (Button) findViewById(R.id.bt_disconnect_button);
        mDisconnectBTDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getBTDevice() != null) {
                    PayPalHereSDK.getCardReaderManager().disconnectFromDevice(getBTDevice());
                    mDisconnectBTDeviceButton.setEnabled(false);
                    mGetBatteryLevelButton.setEnabled(false);
                }
            }
        });

        mGetBatteryLevelButton = (Button) findViewById(R.id.bt_battery_Level_button);
        mGetBatteryLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayPalHereSDK.getCardReaderManager().getBatteryLevel();
            }
        });
        mGetBatteryLevelButton.setEnabled(false);

        mGetEmvDeviceDataButton = (Button) findViewById(R.id.bt_update_emv_info_button);
        mGetEmvDeviceDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEMVDeviceData();
            }
        });

        mLastKnowBatteryLevel = (TextView) findViewById(R.id.bt_last_battery_level);

        mDeviceName = (TextView) findViewById(R.id.bt_device_name);
        mFirmwareVersion = (TextView) findViewById(R.id.bt_firmare_version);
        mSerialNumber = (TextView) findViewById(R.id.bt_serial_number);
        mOSVersion = (TextView) findViewById(R.id.bt_os_version);
        mConnectionStatus = (TextView) findViewById(R.id.bt_connection_status);

    }

    private void getEMVDeviceData() {
        fetchTerminalInfo();
    }

    private void scanForBTDevices() {
        Intent i = new Intent(EMVOnlyActivity.this, BluetoothDeviceFinderActivity.class);
        startActivityForResult(i, BT_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == BT_REQ_CODE) {
            String deviceAddress = data.getExtras().getString(BluetoothDeviceFinderActivity
                    .EXTRA_DEVICE_ADDRESS);
            connectDevice(deviceAddress);
        }
    }

    private void connectDevice(String deviceInfo) {

        String address = deviceInfo.substring(deviceInfo.length() - 17);
        mEMVDevice = mBTAdapter.getRemoteDevice(address);
        setBTDevice(mEMVDevice);
        mPeripheralsManager.connectToDevice(mEMVDevice, mConnectionHandler);
    }

    private synchronized void showEMVStatusDialog(String statusMsg) {
        // Clear/dismiss the progress dialog having the earlier message if any.
        dismissProgressDialog();
        // Set a new progress dialog message and show that.
        Log.d(LOG, statusMsg + " : " + new Date());
        mProgressDialog.setMessage(statusMsg);
        mProgressDialog.show();
    }

    private void showEMVActionDialog(String statusMsg, boolean isMandatory) {
        // Clear/dismiss the progress dialog having the earlier message if any.
        dismissProgressDialog();
        // Set a new progress dialog message.
        mAlertDialog.setMessage(statusMsg);
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performSoftwareUpdate();
            }
        });
        // Do not show the cancel button in case of a mandatory update.
        if (!isMandatory) {
            mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dismissProgressDialog();
                    dismissAlertDialog();
                }
            });
        }

        mAlertDialog.show();
    }

    private void fetchTerminalInfo() {

        EMVDeviceData data = PayPalHereSDK.getCardReaderManager().getEMVDeviceData();

        mDeviceName.setText(CommonUtils.isNullOrEmpty(data.getDeviceName()) ? "" : data.getDeviceName());

        mFirmwareVersion.setText(CommonUtils.isNullOrEmpty(data.getFirmwareVersion()) ? "" : data.getFirmwareVersion
                ());

        mSerialNumber.setText(CommonUtils.isNullOrEmpty(data.getSerialNumber()) ? "" : data.getSerialNumber());

        mOSVersion.setText(CommonUtils.isNullOrEmpty(data.getOSVersion()) ? "" : data.getOSVersion());

        mLastKnowBatteryLevel.setText(data.getLastKnownBatteryLevel() + "");

        mConnectionStatus.setText(data.getConnectionStatus() == null ? "" : data
                .getConnectionStatus().toString());
    }

    private void clearUI() {
        mDeviceName.setText("");
        mFirmwareVersion.setText("");
        mSerialNumber.setText("");
        mOSVersion.setText("");
    }

    private void dismissAlertDialog() {
        if (mAlertDialog.isShowing())
            mAlertDialog.dismiss();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    private void performSoftwareUpdate() {
        mPeripheralsManager.initiateSoftwareUpdate(mEMVDevice, mUpdateHandler);
    }

    private void proceedToBillingActivity() {
        Intent intent = new Intent(EMVOnlyActivity.this, BillingTypeTabActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerTypes, ReaderConnectionTypes readerConnectionTypes) {

    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerTypes) {
        clearUI();
    }

    @Override
    public void onCardReadSuccess(SecureCreditCard secureCreditCard) {

    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> cardErrorsPPError) {

    }

    public void onCardReaderEvent(PPError<CardReaderEvents> e) {

        CardReaderEvents type = e.getErrorCode();

        switch (type) {
            case LowBattery:
            case ChargedBattery:
            case ChargingBattery:
            case OnBattery:
                showBatteryLevelPopup(type, e.getDetailedMessage());
                break;
        }

    }

    @Override
    public void onSelectPaymentDecision(List<ChipAndPinDecisionEvent> eventList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onInvalidListeningPort() {
        CommonUtils.createToastMessage(this, "No valid listening port.");
    }

    private void showBatteryLevelPopup(CardReaderEvents status, String batteryLevel) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        switch (status) {
            case LowBattery:
                dialog.setTitle("Battery Low!");
                break;
        }
        dialog.setMessage("Battery Level! : " + batteryLevel);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void showSWUpdateFailedDialog(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Software Update Failed!");
        dialog.setMessage(msg);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPeripheralsManager.registerCardReaderListener(this);

        if (getBTDevice() == null) {
            mDisconnectBTDeviceButton.setEnabled(false);
            mGetBatteryLevelButton.setEnabled(false);
        } else {
            mDisconnectBTDeviceButton.setEnabled(true);
            mGetBatteryLevelButton.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPeripheralsManager.unregisterCardReaderListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

}