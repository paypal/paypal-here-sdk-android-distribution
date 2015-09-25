/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.emv.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.R;
import com.paypal.merchant.sdk.domain.ChipAndPinStatusUpdateHandler;
import com.paypal.merchant.sdk.domain.PPError;

import java.util.ArrayList;
import java.util.Set;

public class DeviceConnectActivity extends Activity {
    private static final String LOG_TAG = DeviceConnectActivity.class.getSimpleName();
    public static BluetoothDevice mSelectedBluetoothDevice = null;
    private ContextThemeWrapper mAlertDialogTheme = null;
    private ProgressDialog mProgressDialog = null;
    private AlertDialog mCurrentDisplayDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
        setContentView(R.layout.sdk_empy_layout);
        LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
        mAlertDialogTheme = new ContextThemeWrapper(this, R.style.SDKTheme_AlertDialog);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart IN");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() <= 0) {
            showNoPairedDevicesAlertDialog();
        } else {
            showAlertDialogWithPairedDevices(pairedDevices);
        }
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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(LOG_TAG, "onConfigurationChanged IN");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null != mCurrentDisplayDialog){
            mCurrentDisplayDialog.dismiss();
            mCurrentDisplayDialog = null;
        }
        if(null != mProgressDialog){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        Log.d(LOG_TAG, "onStop IN");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy IN");
    }

    private void showAlertDialogWithPairedDevices(final Set<BluetoothDevice> deviceSet) {
        Log.d(LOG_TAG, "showAlertDialogWithPairedDevices IN");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeviceConnectActivity.this, R.layout.sdk_device_name);
        final ArrayList<BluetoothDevice> deviceArray = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : deviceSet) {
            String deviceName = device.getName();
            if (deviceName.contains("PayPal")) {
                Log.d(LOG_TAG, "Adding the device: " + deviceName + " to the adapter");
                adapter.add(deviceName);
                deviceArray.add(device);
            }
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mAlertDialogTheme);
        builder.setTitle(R.string.sdk_dlg_title_paired_devices);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
                final BluetoothDevice device = deviceArray.get(i);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectToBluetoothDevice(device);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(mAlertDialogTheme);
        builder.setTitle(R.string.sdk_dlg_title_paired_devices);
        builder.setMessage(R.string.sdk_dlg_msg_no_paired_devices);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.sdk_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "showNoPairedDevicesAlertDialog:run:onClick IN");
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
                setResult(MainActivity.ACTIVITY_RESULT_CODE_FAILURE);
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

    private void showSoftwareUpdateRequiredAlertDialog() {
        Log.d(LOG_TAG, "showSoftwareUpdateRequiredAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(mAlertDialogTheme);
        builder.setTitle(R.string.sdk_dlg_title_sw_upd_required);
        builder.setMessage(R.string.sdk_dlg_msg_sw_upd_required);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.sdk_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "showSoftwareUpdateRequiredAlertDialog:run:onClick IN");
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startSoftwareUpdate();
                    }
                }).run();
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

    private void showSoftwareUpdateSuggestedAlertDialog() {
        Log.d(LOG_TAG, "showSoftwareUpdateSuggestedAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(mAlertDialogTheme);
        builder.setTitle(R.string.sdk_dlg_title_sw_upd_recommended);
        builder.setMessage(R.string.sdk_dlg_msg_sw_upd_recommended);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.sdk_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "showSoftwareUpdateSuggestedAlertDialog:OK:onClick IN");
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startSoftwareUpdate();
                    }
                }).run();
            }
        });
        builder.setNegativeButton(R.string.sdk_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_TAG, "showSoftwareUpdateSuggestedAlertDialog:Cancel:onClick IN");
                dialogInterface.dismiss();
                mCurrentDisplayDialog = null;
                setResult(MainActivity.ACTIVITY_RESULT_CODE_SUCCESS);
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

    private void showProgressDialog(String title, String message) {
        Log.d(LOG_TAG, "showProgressDialog IN");
        mProgressDialog = new ProgressDialog(mAlertDialogTheme);
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

    private void updateProgressDialog(String title, String message) {
        if (null != mProgressDialog) {
            if (null != title) {
                mProgressDialog.setTitle(title);
            }
            if (null != message) {
                mProgressDialog.setMessage(message);
            }
        }
    }

    private void cancelProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void startSoftwareUpdate() {
        Log.d(LOG_TAG, "startSoftwareUpdate IN");
        if (null == mSelectedBluetoothDevice) {
            Log.e(LOG_TAG, "startSoftwareUpdate Error!. mSelectedBluetoothDevice is null");
            setResult(MainActivity.ACTIVITY_RESULT_CODE_FAILURE);
            finish();
            return;
        }
        String title = DeviceConnectActivity.this.getString(R.string.sdk_dlg_title_sw_upd_updating);//+" "+mSelectedBluetoothDevice.getName();
        String msg = DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_initiating);
        showProgressDialog(title, msg);
        PayPalHereSDK.getCardReaderManager().initiateSoftwareUpdate(mSelectedBluetoothDevice, new ReaderUpdateHandler());
    }

    private void connectToBluetoothDevice(BluetoothDevice device) {
        if (null == device) {
            Log.e(LOG_TAG, "connectToBluetoothDevice Error!. Incoming device is null");
            setResult(MainActivity.ACTIVITY_RESULT_CODE_FAILURE);
            finish();
            return;
        }
        mSelectedBluetoothDevice = device;
        Log.d(LOG_TAG, "connectToBluetoothDevice IN. Device: " + device.getName());
        String message = DeviceConnectActivity.this.getString(R.string.sdk_dlg_title_connecting_device) + " " + device.getName();
        showProgressDialog(message, this.getString(R.string.sdk_dlg_msg_detecting_device));
        PayPalHereSDK.getCardReaderManager().connectToDevice(device, new ReaderConnectHandler());
    }

    private class ReaderConnectHandler implements ChipAndPinStatusUpdateHandler<CardReaderManager.ChipAndPinStatusResponse,
            PPError<CardReaderManager.ChipAndPinConnectionStatus>> {

        @Override
        public void onInitiated(CardReaderManager.ChipAndPinStatusResponse object,
                                PPError<CardReaderManager.ChipAndPinConnectionStatus> updatedStatus) {
            Log.d(LOG_TAG, "ReaderConnectHandler onInitiated the Card reader connection");
        }

        @Override
        public void onStatusUpdated(CardReaderManager.ChipAndPinStatusResponse object,
                                    PPError<CardReaderManager.ChipAndPinConnectionStatus> updatedStatus) {
            CardReaderManager.ChipAndPinConnectionStatus info = updatedStatus.getErrorCode();
            Log.d(LOG_TAG, "ReaderConnectHandler:onStatusUpdated: " + info.name());
            switch (info) {
                case ConnectionInProgress:
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_connecting_device));
                    break;

                case ConnectedAndConfiguring:
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_configuring_device));
                    break;

                case TerminalInfoAvailable:
                    break;
                default:
            }
        }

        @Override
        public void onCompleted(CardReaderManager.ChipAndPinStatusResponse object,
                                PPError<CardReaderManager.ChipAndPinConnectionStatus> updatedStatus) {
            CardReaderManager.ChipAndPinConnectionStatus status = updatedStatus.getErrorCode();
            Log.d(LOG_TAG, "ReaderConnectHandler:onCompleted: " + status.name());
            cancelProgressDialog();
            switch (status) {
                case ErrorSoftwareUpdateRequired: {
                    showSoftwareUpdateRequiredAlertDialog();
                }
                break;
                case ErrorSoftwareUpdateRecommended: {
                    showSoftwareUpdateSuggestedAlertDialog();
                }
                break;
                case ConnectedAndReady: {
                    setResult(MainActivity.ACTIVITY_RESULT_CODE_SUCCESS);
                    finish();
                }
                break;
                case Disconnected: {
                    setResult(MainActivity.ACTIVITY_RESULT_CODE_FAILURE);
                    finish();
                }
                break;
                case ErrorBluetoothNotActiveListeningPort: {
                    setResult(MainActivity.ACTIVITY_RESULT_CODE_FAILURE);
                    finish();
                }
                default:

            }
        }
    }

    private class ReaderUpdateHandler implements ChipAndPinStatusUpdateHandler<CardReaderManager.ChipAndPinStatusResponse,
            PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus>> {

        @Override
        public void onInitiated(CardReaderManager.ChipAndPinStatusResponse object,
                                PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus> updatedStatus) {
            Log.d(LOG_TAG, "ReaderUpdateHandler:onInitiated IN");
            updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_initiating));
        }

        @Override
        public void onStatusUpdated(CardReaderManager.ChipAndPinStatusResponse object,
                                    PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus> updatedStatus) {
            CardReaderManager.ChipAndPinSoftwareUpdateStatus status = updatedStatus.getErrorCode();
            Log.d(LOG_TAG, "ReaderUpdateHandler:onStatusUpdated: status: " + status.name());
            switch (status) {
                case UpdatedAndReady: {
                    setResult(MainActivity.ACTIVITY_RESULT_CODE_SUCCESS);
                    finish();
                }
                break;
                case KeyInjectionInProgress: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_key_injection));
                }
                break;
                case FetchingTerminalKeys: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_fetching_keys));
                }
                break;
                case SoftwareUpdateInProgress: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_update_in_progress));
                }
                break;
                case DownloadInProgress: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_download_in_progress));
                }
                break;
                case ProgressOfCurrentFileDownload: {

                }
                break;
                case EndDownloadingFile: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_download_complete));
                }
                break;
                case InstallInProgress: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_install_in_progress));
                }
                break;
                case KeyInjectionFailedWithCause:
                case KeyInjectionFailedUnknownError:
                case SoftwareUpdateFailedWithCause:
                case SoftwareUpdateFailedUnknownError: {

                }
                break;
                case RestartingTerminal: {
                    updateProgressDialog(null, DeviceConnectActivity.this.getString(R.string.sdk_dlg_msg_sw_upd_restarting_terminal));
                }
                break;
            }
        }

        @Override
        public void onCompleted(CardReaderManager.ChipAndPinStatusResponse object,
                                PPError<CardReaderManager.ChipAndPinSoftwareUpdateStatus> updatedStatus) {
            CardReaderManager.ChipAndPinSoftwareUpdateStatus status = updatedStatus.getErrorCode();
            Log.d(LOG_TAG, "ReaderUpdateHandler:onCompleted: status: " + status.name());
            cancelProgressDialog();
            switch (status) {
                case UpdatedAndReady: {
                    setResult(MainActivity.ACTIVITY_RESULT_CODE_SUCCESS);
                }
                break;
                case KeyInjectionFailedWithCause:
                case KeyInjectionFailedUnknownError:
                case SoftwareUpdateFailedWithCause:
                case SoftwareUpdateFailedUnknownError: {
                    setResult(MainActivity.ACTIVITY_RESULT_CODE_FAILURE);
                }
                break;
            }
            finish();
        }
    }
}
