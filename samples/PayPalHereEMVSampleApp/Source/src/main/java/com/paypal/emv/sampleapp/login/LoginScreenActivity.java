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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.emv.sampleapp.R;
import com.paypal.merchant.sdk.PayPalHereSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This activity acts as the login screen that enables users to log in to the app. Steps to login include:
 * 1. Selecting a suitable server to connect to.
 * 2. Performing an OAuth login (handled in the activity: OAuthLoginActivity.java).
 */
public class LoginScreenActivity extends Activity {
    private static final String LOG_TAG = LoginScreenActivity.class.getSimpleName();
    private String mUsername;
    private String mPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private TextView mEnv;
    private TextView mEMVSoftwareRepo;
    private String mServerName;
    private static SharedPreferences mSharedPrefs;

    /**
     * initialize the various layout elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_screen);

        mServerName = getLastGoodServer();
        if(mServerName == null) {
            mServerName = PayPalHereSDK.Sandbox;
        }

        String lastGoodUsername = getLastGoodUsername();
        if(lastGoodUsername != null) {
            ((EditText) findViewById(R.id.username)).setText(lastGoodUsername);
        }

        mEnv = (TextView) findViewById(R.id.env);
        mEnv.setText(mServerName);
        mEMVSoftwareRepo = (TextView) findViewById(R.id.env_emv_sw_repo);

        mProgressBar = (ProgressBar) findViewById(R.id.login_progress);
        mProgressBar.setVisibility(View.GONE);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg) {
                // Get the username and password from the input fields.
                mUsername = ((EditText) findViewById(R.id.username)).getText().toString();
                mPassword = ((EditText) findViewById(R.id.password)).getText().toString();
                // Validate the username and password for null or empty.
                if (!isValidInput()) {
                    Toast.makeText(LoginScreenActivity.this, R.string.invalid_user_credentials, Toast.LENGTH_SHORT).show();
                    return;
                }


                performOAuthLogin(arg);
                finish();

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
        setStage(mServerName);

        updateConnectedEnvUI();
    }

    private void updateConnectedEnvUI() {
        String connectedTo = PayPalHereSDK.getCurrentServer();
        if (null != connectedTo) {
            mEnv.setText(connectedTo);
        }
        String repo = PayPalHereSDK.getEMVConfigRepo();
        if(null != repo){
            mEMVSoftwareRepo.setText(repo);
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
        startActivity(intent);

    }

    /**
     * Method to valid the input for null or empty.
     *
     * @return
     */
    private boolean isValidInput() {
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
    private void setUserCredentials() {
        ((EditText) findViewById(R.id.username)).setText("harish");
        ((EditText) findViewById(R.id.password)).setText("11111111");
        PayPalHereSDK.setServerName("stage2pph10");
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

    private String getLastGoodServer() {
        mSharedPrefs = getSharedPreferences(OAuthLoginActivity.PREFS_NAME, 0);
        return mSharedPrefs.getString(OAuthLoginActivity.PREFS_LAST_GOOD_SERVER, null);
    }

    private String getLastGoodUsername() {
        mSharedPrefs = getSharedPreferences(OAuthLoginActivity.PREFS_NAME, 0);
        return mSharedPrefs.getString(OAuthLoginActivity.PREFS_LAST_GOOD_USERNAME, null);
    }

    public static void setStage(String name){
        String url = "https://www."+name+".stage.paypal.com";
        JSONObject object = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            JSONObject urlObject = new JSONObject();
            urlObject.put("name",name);
            urlObject.put("url",url);
            array.put(urlObject);
            object.put("servers",array);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException");
            e.printStackTrace();
            return;
        }
        PayPalHereSDK.setOptionalServerList(object.toString());
        PayPalHereSDK.setServerName(name);
    }
}
