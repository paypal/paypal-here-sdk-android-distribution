/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

public class SDKSettingsActivity extends MyActivity {

    private EditText mBNCode;
    private EditText mCashierId;
    private Button mUpdateSettingsButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_settings);

        mBNCode = (EditText) findViewById(R.id.bn_code);
        mCashierId = (EditText) findViewById(R.id.cashier_id);
        mUpdateSettingsButton = (Button) findViewById(R.id.sdk_settings_button);
        mUpdateSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
        updateUI();
    }

    private void updateUI() {
        mBNCode.setText(CommonUtils.isNullOrEmpty(getBNCode()) ? "" : getBNCode());
        mCashierId.setText(CommonUtils.isNullOrEmpty(getCashierId()) ? "" : getCashierId());
    }

    private void updateSettings() {
        setBNCode(CommonUtils.getString((EditText) mBNCode));
        setCashierId(CommonUtils.getString((EditText) mCashierId));
        finish();
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