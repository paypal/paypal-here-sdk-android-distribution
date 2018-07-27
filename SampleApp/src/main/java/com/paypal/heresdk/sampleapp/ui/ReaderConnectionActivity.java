package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.login.LoginActivity;
import com.paypal.paypalretailsdk.DeviceManager;
import com.paypal.paypalretailsdk.PaymentDevice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;

public class ReaderConnectionActivity extends ToolbarActivity implements View.OnClickListener
{

  private static final String LOG_TAG = ReaderConnectionActivity.class.getSimpleName();
  public static final String INTENT_STRING_EMV_READER = "EMV_READER";
  public static final String INTENT_STRING_AUDIO_JACK_READER = "AUDIO_JACK_READER";

  private StepView findConnectStep;
  private StepView connectLastStep;
  private StepView autoConnectStep;

  @Override
  public int getLayoutResId()
  {
    return R.layout.reader_connection_activity;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    findConnectStep = (StepView)findViewById(R.id.find_connect_step);
    findConnectStep.setOnButtonClickListener(this);
    connectLastStep = (StepView)findViewById(R.id.connect_last_step);
    connectLastStep.setOnButtonClickListener(this);
    autoConnectStep = (StepView)findViewById(R.id.auto_connect_step);
    autoConnectStep.setOnButtonClickListener(this);
  }




  public void onFindAndConnectClicked()
  {
    RetailSDK.getDeviceManager().searchAndConnect(new DeviceManager.ConnectionCallback()
    {
      @Override
      public void connection(final RetailSDKException error, final PaymentDevice cardReader)
      {
        ReaderConnectionActivity.this.runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            if (error == null)
            {
              //Toast.makeText(getApplicationContext(), "Connected to card reader" + cardReader.getId(), Toast.LENGTH_SHORT).show();
              onReaderConnected(cardReader);
            }
            else
            {
              Log.e(LOG_TAG, "Connection to a reader failed with error: " + error);
              Toast.makeText(getApplicationContext(), "Card reader connection error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    });

  }


  public void onConnectToLastClicked()
  {
    RetailSDK.getDeviceManager().connectToLastActiveReader(new DeviceManager.ConnectionCallback()
    {
      @Override
      public void connection(final RetailSDKException error, final PaymentDevice cardReader)
      {
        ReaderConnectionActivity.this.runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            if (error == null && cardReader != null)
            {
              //Toast.makeText(getApplicationContext(), "Connected to last active device " + cardReader.getId(), Toast.LENGTH_SHORT).show();
              onReaderConnected(cardReader);
            }
            else if (error != null)
            {
              Toast.makeText(getApplicationContext(), "Connection to a reader failed with error: " + error, Toast.LENGTH_SHORT).show();
              Log.e(LOG_TAG, "Connection to a reader failed with error: " + error);
            }
            else
            {
              Toast.makeText(getApplicationContext(), "Could not find the last card reader to connect to", Toast.LENGTH_SHORT).show();
              Log.d(LOG_TAG, "Could not find the last card reader to connect to");
            }
          }
        });

      }
    });
  }

  private void onReaderConnected(PaymentDevice cardReader)
  {
    Log.d(LOG_TAG, "Connected to device " + cardReader.getId());
    final TextView readerIdTxt = (TextView) findViewById(R.id.textReaderId);
    readerIdTxt.setText(getString(R.string.connected) + " " + cardReader.getId());

    final LinearLayout runTxnButtonContainer = (LinearLayout) findViewById(R.id.run_txn_btn_container);
    runTxnButtonContainer.setVisibility(View.VISIBLE);
  }

  public void onRunTransactionClicked(View view)
  {
    Intent transactionIntent = new Intent(ReaderConnectionActivity.this, ChargeActivity.class);
    startActivity(transactionIntent);
  }


  public void onAutoConnectClicked() {

    autoConnectStep.showProgressBar();
    String lastKnowReader = RetailSDK.getDeviceManager().getLastActiveBluetoothReader();
    RetailSDK.getDeviceManager().scanAndAutoConnectToBluetoothReader(lastKnowReader, new DeviceManager.ConnectionCallback() {
        @Override
        public void connection(final RetailSDKException error, final PaymentDevice cardReader) {
          ReaderConnectionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              autoConnectStep.hideProgressBarShowButton();
              if (error == null && cardReader != null) {
                //Toast.makeText(getApplicationContext(), "Connected to last active device " + cardReader.getId(), Toast.LENGTH_SHORT).show();
                onReaderConnected(cardReader);
              } else if (error != null) {
                Toast.makeText(getApplicationContext(), "Connection to a reader failed with error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, "Connection to a reader failed with error: " + error);
              } else {
                Toast.makeText(getApplicationContext(), "Could not find the last card reader to connect to", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Could not find the last card reader to connect to");
              }
            }
          });

        }
      });
  }


  @Override
  public void onClick(View v)
  {
    if (v == findConnectStep.getButton()){
      onFindAndConnectClicked();
    }else if(v == connectLastStep.getButton()){
      onConnectToLastClicked();
    }else if(v == autoConnectStep.getButton()){
      onAutoConnectClicked();
    }
  }
}
