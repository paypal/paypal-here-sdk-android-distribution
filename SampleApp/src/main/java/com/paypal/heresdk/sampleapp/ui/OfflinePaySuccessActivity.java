package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.paypal.heresdk.sampleapp.R;

public class OfflinePaySuccessActivity extends Activity
{

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_offline_pay_success);
  }

  public void  onNewSaleClicked(View view){
    Intent intent = new Intent(OfflinePaySuccessActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }
}
