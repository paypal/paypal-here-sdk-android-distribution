package com.paypal.emv.sampleapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.paypal.emv.sampleapp.R;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;

public class SettingsActivity extends ActionBarActivity {
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    private EditText mCCNameTxt;
    private EditText mVATIDTxt;
    private Button mSavePrefsButton;

    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mCCNameTxt = (EditText)findViewById(R.id.id_cc_name);
        mVATIDTxt = (EditText)findViewById(R.id.id_vat_id);

        mSavePrefsButton = (Button)findViewById(R.id.id_save_prefs_button);
        mSavePrefsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"Save Prefs Button onClick");
                hideKeyboard();
                if((null == mCCNameTxt.getText().toString() || mCCNameTxt.getText().toString().length() <=0) &&
                        (null == mVATIDTxt.getText().toString() || mVATIDTxt.getText().toString().length() <= 0)){
                    showAlertDialog(R.string.merchant_error_title,R.string.err_invalid_cc_name_or_vat_id);
                    return;
                }
                savePreferences();
            }
        });

        showProgressDialog(null,getString(R.string.progress_getting_prefs));

        PayPalHereSDK.getMerchantManager().getActiveMerchant().getMerchantPreferences(new DefaultResponseHandler<Merchant.MerchantPreferences, PPError<PPError.BasicErrors>>() {
            @Override
            public void onSuccess(Merchant.MerchantPreferences responseObject) {
                cancelProgressDialog();
                Log.d(LOG_TAG,"getMerchantPreferences onSuccess. CCStatementName: "+responseObject.getCCStatementName()+" VATID: "+responseObject.getVATID());
                mCCNameTxt.setText(responseObject.getCCStatementName());
                mVATIDTxt.setText(responseObject.getVATID());
            }

            @Override
            public void onError(PPError<PPError.BasicErrors> error) {
                cancelProgressDialog();
                showAlertDialog(R.string.merchant_error_title,R.string.err_get_prefs);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG,"onConfigurationChanged");
    }

    private void showAlertDialog(int titleResID, int msgResID){
        Log.d(LOG_TAG, "showTransactionCompleteAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResID);
        builder.setMessage(msgResID);
        builder.setCancelable(false);
        builder.setNeutralButton(com.paypal.merchant.sdk.R.string.sdk_OK,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    private void showProgressDialog(String title, String message) {
        Log.d(LOG_TAG, "showProgressDialog IN");
        mProgressDialog = new ProgressDialog(this);
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

    private void cancelProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void savePreferences(){
        String ccName = mCCNameTxt.getText().toString();
        String vatId = mVATIDTxt.getText().toString();

        Merchant merchant = PayPalHereSDK.getMerchantManager().getActiveMerchant();
        if(null == merchant){
            showAlertDialog(R.string.merchant_error_title,R.string.err_active_merchant);
            return;
        }
        showProgressDialog(null,getString(R.string.progress_saving_prefs));
        Preferences preferences = new Preferences();
        preferences.setCCStatementName(ccName);
        preferences.setVATID(vatId);

        merchant.saveMerchantPreferences(preferences,new DefaultResponseHandler<Boolean, PPError<PPError.BasicErrors>>() {
            @Override
            public void onSuccess(Boolean responseObject) {
                cancelProgressDialog();
                showAlertDialog(R.string.merchant_success_title,R.string.save_prefs_success);
            }

            @Override
            public void onError(PPError<PPError.BasicErrors> error) {
                cancelProgressDialog();
                showAlertDialog(R.string.merchant_error_title,R.string.err_save_prefs);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mCCNameTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(mVATIDTxt.getWindowToken(), 0);
    }

    private class Preferences implements Merchant.MerchantPreferences{

        private String mCCStatementName;
        private String mVATID;

        public void setCCStatementName(String ccStatementName){
            mCCStatementName = ccStatementName;
        }

        public void setVATID(String vatid){
            mVATID = vatid;
        }

        @Override
        public String getCCStatementName() {
            return mCCStatementName;
        }

        @Override
        public String getVATID() {
            return mVATID;
        }
    }
}
