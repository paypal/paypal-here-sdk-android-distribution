package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapper;
import com.paypal.heresdk.sampleapp.R;

public class PaymentOptionsActivity extends Activity {
    private static final String LOG_TAG = PaymentOptionsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_payment_options);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG,"onBackPresssed");
        showExitDialog();
    }

    private void showExitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.exit_dialog_title));
        builder.setMessage(this.getString(R.string.exit_dialog_msg));
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                PayPalHereSDKWrapper.getInstance().disConnectEMVReader(null);
                finish();
            }
        });
        builder.setCancelable(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });
    }

    public void onSwipePaymentOptionSelected(View view){
        Log.d(LOG_TAG, "onSwipePaymentOptionSelected");

        Intent readerConnectionIntent = new Intent(this,ReaderConnectionActivity.class);
        readerConnectionIntent.putExtra(ReaderConnectionActivity.INTENT_STRING_AUDIO_JACK_READER,true);
        startActivity(readerConnectionIntent);
    }

    public void onEMVPaymentOptionSelected(View view){
        Log.d(LOG_TAG,"onEMVPaymentOptionSelected");
        Intent readerConnectionIntent = new Intent(this,ReaderConnectionActivity.class);
        readerConnectionIntent.putExtra(ReaderConnectionActivity.INTENT_STRING_EMV_READER,true);
        startActivity(readerConnectionIntent);
    }

    public void onBothSwipeAndEMVPaymentOptionSelected(View view){
        Log.d(LOG_TAG, "onBothSwipeAndEMVPaymentOptionSelected");
        Intent readerConnectionIntent = new Intent(this,MultiReaderConnectionActivity.class);
        startActivity(readerConnectionIntent);
    }
}
