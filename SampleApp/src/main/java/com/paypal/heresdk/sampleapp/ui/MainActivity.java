package com.paypal.heresdk.sampleapp.ui;

/**
 * Created by muozdemir on 12/5/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.paypal.heresdk.sampleapp.login.LoginActivity;

import com.paypal.heresdk.sampleapp.R;


public class MainActivity extends Activity
{
  private static final String LOG_TAG = MainActivity.class.getSimpleName();


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_activity);
  }


  public void onStartClicked(View view)
  {
    Log.d(LOG_TAG, "goToLoginActivity");
    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }

}