/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.emv.sampleapp.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.utils.CommonUtils;
import com.paypal.merchant.sdk.PayPalHereSDK;

public class StageSelectActivity extends Activity {
    private static final String LOG_TAG = StageSelectActivity.class.getSimpleName();

    private ArrayAdapter<String> mSoftwareRepoAdapter;
    private String[] mSoftwareRepos = null;
    private EditText mStageSelection;
    private Button mStageSelectButton;
    private Button mSoftwareRepoButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
        setContentView(R.layout.stage_selection);

        mStageSelection = (EditText)findViewById(R.id.id_stage_selection);
        mSoftwareRepoButton = (Button)findViewById(R.id.id_update_sw_repo_button);
        mSoftwareRepoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Software Repo Button onClick");
                updateSoftwareRepoSelection();
                showSoftwareRepoSelectDialog();
            }
        });

        mStageSelectButton = (Button)findViewById(R.id.id_stage_selection_button);
        mStageSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Stage Select Button onClick");
                hideKeyboard();
                String stageName = mStageSelection.getText().toString();
                if (null == stageName || stageName.length() <= 0) {
                    Toast.makeText(StageSelectActivity.this, "Please enter the valid stage name", Toast.LENGTH_SHORT).show();
                    return;
                }
                CommonUtils.setStage(StageSelectActivity.this, stageName);
                setStageSoftwareRepos();
                enableSoftwareRepoSelection();
            }
        });
        hideKeyboard();

        Button sandbox = (Button)findViewById(R.id.id_select_sandbox);
        sandbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Sandbox environment Button onClick");
                CommonUtils.setStage(StageSelectActivity.this, PayPalHereSDK.Sandbox);
                setLiveOrSandboxSoftwareRepos();
                enableSoftwareRepoSelection();
                Toast.makeText(StageSelectActivity.this,"Selected Sandbox as environment to use",Toast.LENGTH_SHORT).show();
            }
        });

        Button live = (Button)findViewById(R.id.id_select_live);
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Live environment Button onClick");
                CommonUtils.setStage(StageSelectActivity.this, PayPalHereSDK.Live);
                setLiveOrSandboxSoftwareRepos();
                enableSoftwareRepoSelection();
                Toast.makeText(StageSelectActivity.this,"Selected Live as environment to use",Toast.LENGTH_SHORT).show();
            }
        });

        Button mockServer = (Button)findViewById(R.id.id_select_controlled_sandbox);
        mockServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Mockserver Button onClick");
                CommonUtils.setStage(StageSelectActivity.this, PayPalHereSDK.MockServer);
                displayCountryCodeOptionAlertDialog();
                disableSoftwareRepoSelection();
                Toast.makeText(StageSelectActivity.this, "Selected Mockserver as environment to use", Toast.LENGTH_SHORT).show();
            }
        });

        updateSoftwareRepoSelection();
    }

    private void updateSoftwareRepoSelection(){
        if(CommonUtils.isMockServer(this)){
            disableSoftwareRepoSelection();
        }else{
            enableSoftwareRepoSelection();
        }

        String stageName = CommonUtils.getStoredServer(this);
        if(stageName.equals(PayPalHereSDK.Live) || stageName.equals(PayPalHereSDK.Sandbox)){
            setLiveOrSandboxSoftwareRepos();
        }else{
            setStageSoftwareRepos();
        }
    }

    private void disableSoftwareRepoSelection(){
        mSoftwareRepoButton.setEnabled(false);
    }

    private void enableSoftwareRepoSelection(){
        mSoftwareRepoButton.setEnabled(true);
    }

    private void setStageSoftwareRepos(){
        mSoftwareRepos = new String[7];

        mSoftwareRepos[0] = "dev-stage-1";
        mSoftwareRepos[1] = "dev-stage-2";
        mSoftwareRepos[2] = "dev-stage-3";
        mSoftwareRepos[3] = "dev-stage-4";
        mSoftwareRepos[4] = "qa-stage-1";
        mSoftwareRepos[5] = "qa-stage-2";
        mSoftwareRepos[6] = "qa-stage-3";
    }

    private void setLiveOrSandboxSoftwareRepos(){
        mSoftwareRepos = new String[2];

        mSoftwareRepos[0] = "production";
        mSoftwareRepos[1] = "production-stage";
    }

    private void displayCountryCodeOptionAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a merchant country");
        builder.setItems(new CharSequence[]{"UK", "AU", "US"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, "Selected Country Code : " + which);
                setMerchantCountryCodeForMock(which);
            }
        });
        builder.setCancelable(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    private void setMerchantCountryCodeForMock(int which) {
        PayPalHereSDK.MerchantCountryForMock countryForMock;
        switch (which) {
            case 0:
                countryForMock = PayPalHereSDK.MerchantCountryForMock.UK;
                break;
            case 1:
                countryForMock = PayPalHereSDK.MerchantCountryForMock.AU;
                break;
            case 2:
            default:
                countryForMock = PayPalHereSDK.MerchantCountryForMock.US;
        }

        PayPalHereSDK.setMerchantCountryForMock(countryForMock);
        CommonUtils.saveMockCountryCode(this, countryForMock);
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(StageSelectActivity.this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mStageSelection.getWindowToken(), 0);
    }

    private void showSoftwareRepoSelectDialog(){
        Log.d(LOG_TAG, "showSoftwareRepoSelectDialog");
        mSoftwareRepoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,mSoftwareRepos);
        AlertDialog.Builder builder = new AlertDialog.Builder(StageSelectActivity.this);
        builder.setTitle("Select Software Repo");
        builder.setAdapter(mSoftwareRepoAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String swRepo = mSoftwareRepoAdapter.getItem(which);
                if (null != swRepo) {
                    PayPalHereSDK.setEMVConfigRepo(swRepo);
                    CommonUtils.saveEMVConfigStage(StageSelectActivity.this, swRepo);
                    Toast.makeText(StageSelectActivity.this, "Changed the EMV SW Repo to " + swRepo, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }
}