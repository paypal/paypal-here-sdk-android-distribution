package com.paypal.emv.sampleapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.paypal.emv.sampleapp.R;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.EMVDeviceData;

public class DeviceDetailsActivity extends Activity {
    private static final String LOG_TAG = DeviceDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"onCreate IN");
        setContentView(R.layout.device_details);

        EMVDeviceData deviceData = PayPalHereSDK.getCardReaderManager().getEMVDeviceData();
        if(null == deviceData){
            Log.d(LOG_TAG,"deviceData is null. Hence can't do much...");
        }else {
            updateUIWithDeviceData(deviceData);
        }
    }

    private void updateUIWithDeviceData(EMVDeviceData deviceData){
        TextView deviceName = (TextView)findViewById(R.id.id_device_name_val);
        if(null != deviceName) {
            deviceName.setText(deviceData.getDeviceName());
        }

        TextView osVersion = (TextView)findViewById(R.id.id_os_version_val);
        if(null != osVersion) {
            osVersion.setText(deviceData.getOSVersion());
        }

        TextView firmwareVersion = (TextView)findViewById(R.id.id_firmware_version_val);
        if(null != firmwareVersion) {
            firmwareVersion.setText(deviceData.getFirmwareVersion());
        }

        TextView serialNumber = (TextView)findViewById(R.id.id_serial_number_val);
        if(null != serialNumber) {
            serialNumber.setText(deviceData.getSerialNumber());
        }

        TextView batteryLevel = (TextView)findViewById(R.id.id_battery_level_val);
        if(null != batteryLevel) {
            batteryLevel.setText(String.valueOf(deviceData.getLastKnownBatteryLevel()));
        }
    }
}
