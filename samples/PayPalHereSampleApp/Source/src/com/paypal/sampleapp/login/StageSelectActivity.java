/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.paypal.sampleapp.R;
import com.paypal.merchant.sdk.PayPalHereSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StageSelectActivity extends Activity {
    private static final String LOG_TAG = StageSelectActivity.class.getSimpleName();

    private ArrayAdapter<String> mSoftwareRepoAdapter;
    private String[] mSoftwareRepos = {"dev-stage-1","dev-stage-2","dev-stage-3","qa-stage-1","qa-stage-2","qa-stage-3"};
    private EditText mStageSelection;
    private Button mStageSelectButton;
    private Button mSoftwareRepoButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"onCreate IN");
        setContentView(R.layout.stage_selection);
        mSoftwareRepoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,mSoftwareRepos);

        mStageSelection = (EditText)findViewById(R.id.id_stage_selection);

        mStageSelectButton = (Button)findViewById(R.id.id_stage_selection_button);
        mStageSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Stage Select Button onClick");
                hideKeyboard();
                String stageName = mStageSelection.getText().toString();
                if(null == stageName || stageName.length() <= 0){
                    Toast.makeText(StageSelectActivity.this,"Please enter the valid stage name",Toast.LENGTH_SHORT).show();
                    return;
                }
                setStage(stageName);
            }
        });
        hideKeyboard();

        Button sandbox = (Button)findViewById(R.id.id_select_sandbox);
        sandbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Sandbox environment Button onClick");
                PayPalHereSDK.setServerName(PayPalHereSDK.Sandbox);
                Toast.makeText(StageSelectActivity.this,"Selected Sandbox as environment to use",Toast.LENGTH_SHORT).show();
            }
        });

        Button live = (Button)findViewById(R.id.id_select_live);
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Live environment Button onClick");
                PayPalHereSDK.setServerName(PayPalHereSDK.Live);
                Toast.makeText(StageSelectActivity.this,"Selected Live as environment to use",Toast.LENGTH_SHORT).show();
            }
        });
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
        Log.d(LOG_TAG,"showSoftwareRepoSelectDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(StageSelectActivity.this);
        builder.setTitle("Select Software Repo");
        builder.setAdapter(mSoftwareRepoAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String swRepo = mSoftwareRepoAdapter.getItem(which);
                if(null != swRepo){
                    PayPalHereSDK.setEMVConfigRepo(swRepo);
                    Toast.makeText(StageSelectActivity.this,"Changed the EMV SW Repo to "+swRepo,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    public void setStage(String name){
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
            Log.e(LOG_TAG,"JSONException");
            e.printStackTrace();
            Toast.makeText(StageSelectActivity.this,"Failed to set the server",Toast.LENGTH_SHORT).show();
            return;
        }
        PayPalHereSDK.setOptionalServerList(object.toString());
        PayPalHereSDK.setServerName(name);
        Toast.makeText(StageSelectActivity.this,"Selected the stage: "+name,Toast.LENGTH_SHORT).show();
    }
}