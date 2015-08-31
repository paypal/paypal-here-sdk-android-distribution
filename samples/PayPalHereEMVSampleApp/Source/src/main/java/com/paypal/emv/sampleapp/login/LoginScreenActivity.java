/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.emv.sampleapp.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.utils.CommonUtils;
import com.paypal.merchant.sdk.PayPalHereSDK;

/**
 * This activity acts as the login screen that enables users to log in to the app. Steps to login include:
 * 1. Selecting a suitable server to connect to.
 * 2. Performing an OAuth login (handled in the activity: OAuthLoginActivity.java).
 */
public class LoginScreenActivity extends Activity {
    private static final String LOG_TAG = LoginScreenActivity.class.getSimpleName();
    private String mUsername;
    private EditText mUserNameEditBox;
    private EditText mPasswordEditBox;
    private String mPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private TextView mEnv;
    private TextView mEMVSoftwareRepo;
    private TextView mBuildNumber;
    private TextView mMockCountryCodeTextView;
    private LinearLayout mMockCountryCodeLayout;
    private LinearLayout mEMVSoftwareRepoLayout;
    private String mServerName;
    private PayPalHereSDK.MerchantCountryForMock mMockCountryCode;
    private static SharedPreferences mSharedPrefs;

    /**
     * initialize the various layout elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_login_screen);

        mUserNameEditBox = (EditText) findViewById(R.id.username);
        mPasswordEditBox = (EditText) findViewById(R.id.password);
        mEnv = (TextView) findViewById(R.id.env);
        mEMVSoftwareRepo = (TextView) findViewById(R.id.env_emv_sw_repo);
        mMockCountryCodeTextView = (TextView) findViewById(R.id.env_mock_country_code);
        mMockCountryCodeLayout = (LinearLayout) findViewById(R.id.id_mock_country_code_layout);
        mEMVSoftwareRepoLayout = (LinearLayout) findViewById(R.id.id_emv_sw_repo_layout);
        mBuildNumber = (TextView) findViewById(R.id.app_build_number);

        mProgressBar = (ProgressBar) findViewById(R.id.login_progress);
        mProgressBar.setVisibility(View.GONE);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg) {
                mUsername = mUserNameEditBox.getText().toString();
                mPassword = mPasswordEditBox.getText().toString();
                if (!isValidInput()) {
                    Toast.makeText(LoginScreenActivity.this, R.string.invalid_user_credentials, Toast.LENGTH_SHORT).show();
                    return;
                }
                storeLoginInfo();
                performOAuthLogin(arg);
            }
        });

        mLoginButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(LoginScreenActivity.this, StageSelectActivity.class);
                startActivity(i);
                return true;
            }
        });

        PayPalHereSDK.init(getApplicationContext(), PayPalHereSDK.Sandbox);
        setLastKnownValues();
        updateConnectedEnvUI();
    }

    private void setLastKnownValues() {
        mServerName = CommonUtils.getStoredServer(this);
        if (null != mServerName && mServerName.length() > 0) {
            mServerName = PayPalHereSDK.Sandbox;
        }
        CommonUtils.setStage(this, mServerName);

        if (CommonUtils.isMockServer(this)) {
            mMockCountryCode = CommonUtils.getStoredMerchantCountry(this);
            Log.d(LOG_TAG, "setLastKnownValues setting the merchant country for Mock: " + mMockCountryCode);
            PayPalHereSDK.setMerchantCountryForMock(mMockCountryCode);
        } else {
            String lastGoodEMVConfigRepo = CommonUtils.getStoredEMVConfigRepo(this);
            if (null != lastGoodEMVConfigRepo) {
                PayPalHereSDK.setEMVConfigRepo(lastGoodEMVConfigRepo);
            }
        }
    }

    private void updateConnectedEnvUI() {
        mServerName = PayPalHereSDK.getCurrentServer();
        if (null != mServerName) {
            mEnv.setText(mServerName);
        }

        if (CommonUtils.isMockServer(this)) {
            mMockCountryCodeLayout.setVisibility(View.VISIBLE);
            mEMVSoftwareRepoLayout.setVisibility(View.GONE);
            mUserNameEditBox.setText(PayPalHereSDK.MockServer);
            mPasswordEditBox.setText(PayPalHereSDK.MockServer);
            mUserNameEditBox.setText("");
            mPasswordEditBox.setText("");
            mUserNameEditBox.setInputType(InputType.TYPE_NULL);
            mPasswordEditBox.setInputType(InputType.TYPE_NULL);

            mMockCountryCode = CommonUtils.getStoredMerchantCountry(this);
            mMockCountryCodeTextView.setText(mMockCountryCode.toString());
        } else {
            mMockCountryCodeLayout.setVisibility(View.GONE);
            mEMVSoftwareRepoLayout.setVisibility(View.VISIBLE);
            mUserNameEditBox.setHint(R.string.username_hint);
            mPasswordEditBox.setHint(R.string.password_hint);
            mUserNameEditBox.setInputType(InputType.TYPE_CLASS_TEXT);
            mPasswordEditBox.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

            String repo = PayPalHereSDK.getEMVConfigRepo();
            if (null != repo) {
                mEMVSoftwareRepo.setText(repo);
            }

            String lastGoodUsername = CommonUtils.getStoredUsername(this);
            if (lastGoodUsername != null) {
                mUserNameEditBox.setText(lastGoodUsername);
            }
            fillPassword();
        }

        mBuildNumber.setText("" + CommonUtils.getBuildNumber(LoginScreenActivity.this));
    }

    private void storeLoginInfo() {
        if (CommonUtils.isMockServer(this)) {
            Log.d(LOG_TAG, "storeLoginInfo: currently mock server is set. Hence no need to store anything");
        } else {
            String userName = mUserNameEditBox.getText().toString();
            CommonUtils.saveUsername(this, userName);
        }
    }

    /**
     * Method to move into the OAuth login activity.
     *
     * @param arg
     */
    private void performOAuthLogin(View arg) {
        hideKeyboard(arg);

        // Pass the username and password to the OAuth login activity for OAuth login.
        // Once the login is successful, we automatically check in the merchant in the OAuth activity.
        Intent intent = new Intent(LoginScreenActivity.this, OAuthLoginActivity.class);
        intent.putExtra("username", mUsername);
        intent.putExtra("password", mPassword);
        intent.putExtra("servername", mServerName);
        startActivity(intent);
        finish();
    }

    /**
     * Method to valid the input for null or empty.
     *
     * @return
     */
    private boolean isValidInput() {
        //For mock server we don't need to validate credentials
        if (CommonUtils.isMockServer(this)) {
            return true;
        }

        if (null == mUsername || mUsername.length() <= 0) {
            return false;
        } else if (null == mPassword || mPassword.length() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Method to hide the keyboard.
     *
     * @param v
     */
    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    /**
     * Method to set the username and password to a default value.
     */
    //TODO: need to remove this before shipping the app to public
    private void fillPassword() {
        ((EditText) findViewById(R.id.password)).setText("11111111");
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConnectedEnvUI();
    }
}
