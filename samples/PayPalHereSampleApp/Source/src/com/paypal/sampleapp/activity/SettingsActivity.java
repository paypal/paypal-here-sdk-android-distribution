/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;
import com.paypal.sampleapp.util.LocalPreferences;

public class SettingsActivity extends MyActivity {
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private EditText mBNCode;
    private EditText mCashierId;
    private Button mUpdateBNCodesButton;
    private Button mUpdateCashierIDButton;
    private ToggleButton mAuthorizeButton;
    private TextView mCaptureTolerance;
    private RelativeLayout mCaptureToleranceLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
        setContentView(R.layout.activity_sdk_settings);

        mBNCode = (EditText) findViewById(R.id.bn_code);
        mCashierId = (EditText) findViewById(R.id.cashier_id);
        mCaptureTolerance = (TextView) findViewById(R.id.capture_tolerance);
        mCaptureToleranceLayout = (RelativeLayout) findViewById(R.id.capture_tolerance_layout);

        displayCaptureTolerance(LocalPreferences.getAuthorizeOption());

        String bnCode = LocalPreferences.getBNCode();
        Log.d(LOG_TAG, "Setting the BNCode text as: " + bnCode);
        if (null != bnCode) {
            mBNCode.setText(bnCode);
        }

        String cashierID = LocalPreferences.getCashierID();
        Log.d(LOG_TAG, "Setting the CashierID as: " + cashierID);
        if (null != cashierID) {
            mCashierId.setText(cashierID);
        }

        mUpdateBNCodesButton = (Button) findViewById(R.id.update_bncode_button);
        mUpdateBNCodesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bnCode = CommonUtils.getString((EditText) mBNCode);
                Log.d(LOG_TAG, "Storing the BNCode: " + bnCode + " to the LocalPreferences");
                LocalPreferences.setBNCode(bnCode);
            }
        });

        mUpdateCashierIDButton = (Button) findViewById(R.id.update_casierid_button);
        mUpdateCashierIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cashierID = CommonUtils.getString((EditText) mCashierId);
                Log.d(LOG_TAG, "Storing the CashierID: " + cashierID + " to the LocalPreferences");
                LocalPreferences.setCashierID(cashierID);
            }
        });

        mAuthorizeButton = (ToggleButton) findViewById(R.id.id_authorize_button);
        mAuthorizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = ((ToggleButton) view).isChecked();
                Log.d(LOG_TAG, "AuthorizeButton onClick Button Stage: " + on);
                LocalPreferences.setAuthorizeOption(on);
                displayCaptureTolerance(on);
            }
        });
    }

    private void displayCaptureTolerance(boolean display) {
        if (display) {
            mCaptureToleranceLayout.setVisibility(View.VISIBLE);
            mCaptureTolerance.setText(PayPalHereSDK
                    .getMerchantManager()
                    .getActiveMerchant()
                    .getPaymentLimits()
                    .getCaptureTolerancePercentage()
                    .toString());
        } else {
            mCaptureToleranceLayout.setVisibility(View.INVISIBLE);
        }
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