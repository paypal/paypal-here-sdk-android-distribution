package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.login.LoginActivity;
import com.paypal.paypalretailsdk.DeviceManager;
import com.paypal.paypalretailsdk.PaymentDevice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;

public class ReaderConnectionActivity extends Activity
{

  private static final String LOG_TAG = ReaderConnectionActivity.class.getSimpleName();
  public static final String INTENT_STRING_EMV_READER = "EMV_READER";
  public static final String INTENT_STRING_AUDIO_JACK_READER = "AUDIO_JACK_READER";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "onCreate");
    setContentView(R.layout.reader_connection_activity);
  }


  @Override
  public void onBackPressed()
  {
    Log.d(LOG_TAG, "onBackPressed");
    goBackToLoginActivity(null);
  }


  public void onFindAndConnectViewCodeClicked(View view)
  {
    final TextView txtFindConnectCode = (TextView) findViewById(R.id.findConnectCode);

    if (txtFindConnectCode.getVisibility() == View.GONE)
    {
      txtFindConnectCode.setVisibility(View.VISIBLE);
    }
    else
    {
      txtFindConnectCode.setVisibility(View.GONE);
    }
  }


  public void onConnectLastViewCodeClicked(View view)
  {
    final TextView txtConnectLast = (TextView) findViewById(R.id.connectLastReaderCode);

    if (txtConnectLast.getVisibility() == View.GONE)
    {
      txtConnectLast.setVisibility(View.VISIBLE);
    }
    else
    {
      txtConnectLast.setVisibility(View.GONE);
    }
  }


  public void onFindAndConnectClicked(View view)
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
              Toast.makeText(getApplicationContext(), "Connected to card reader" + cardReader.getId(), Toast.LENGTH_SHORT).show();
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


  public void onConnectToLastClicked(View view)
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
              Toast.makeText(getApplicationContext(), "Connected to last active device " + cardReader.getId(), Toast.LENGTH_SHORT).show();
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
    final LinearLayout btmLayout = (LinearLayout) findViewById(R.id.bottomBanner);
    btmLayout.setVisibility(View.VISIBLE);
    final LinearLayout readerIdLayout = (LinearLayout) findViewById(R.id.readerIdLayout);
    readerIdLayout.setVisibility(View.VISIBLE);
    final TextView readerIdTxt = (TextView) findViewById(R.id.textReaderId);
    readerIdTxt.setText(cardReader.getId());
  }

  public void onRunTransactionClicked(View view)
  {
    Intent transactionIntent = new Intent(ReaderConnectionActivity.this, ChargeActivity.class);
    transactionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(transactionIntent);
  }


  public void goBackToLoginActivity(View view)
  {
    Log.d(LOG_TAG, "goBackToLoginActivity");
    RetailSDK.logout();
    Intent loginIntent = new Intent(ReaderConnectionActivity.this, LoginActivity.class);
    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(loginIntent);
  }
}
