package com.paypal.heresdk.sampleapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.paypal.heresdk.sampleapp.R;

public class OfflinePaySuccessActivity extends ToolbarActivity
{

  @Override
  public int getLayoutResId()
  {
    return R.layout.activity_offline_pay_success;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  }

  public void onNewSaleClicked(View view){
    goToChargeActivity();
  }
  public void goToChargeActivity(){
    Intent intent = new Intent(OfflinePaySuccessActivity.this, ChargeActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if(item.getItemId() == android.R.id.home){
      goToChargeActivity();
    }
    return true;
  }
}
