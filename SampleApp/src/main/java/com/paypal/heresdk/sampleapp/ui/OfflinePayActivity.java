package com.paypal.heresdk.sampleapp.ui;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.OfflinePaymentStatus;
import com.paypal.paypalretailsdk.OfflineTransactionState;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionManager;

public class OfflinePayActivity extends ToolbarActivity implements View.OnClickListener
{

  public static final String PREF_NAME = "SampleAppPrefs";
  public static final String OFFLINE_MODE ="offlineMode";
  public static final String OFFLINE_INIT ="offlineInit";
  private static final String REPLAY_IN_PROGRESS = "replayInProgress";
  private static final String LOG_TAG = OfflinePayActivity.class.getSimpleName();

  private Switch offlineModeSwitch;
  private TextView statusText;
  private StepView offlineStatusStep;
  private StepView replayStep;
  private StepView stopReplayStep;
  private SharedPreferences sharedPrefs;
  private boolean activityVisible = true;


  @Override
  public int getLayoutResId()
  {
    return R.layout.activity_offline_pay;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    sharedPrefs = getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
    statusText = (TextView)findViewById(R.id.status_text);
    offlineModeSwitch = (Switch)findViewById(R.id.offline_mode_switch);
    offlineStatusStep = (StepView) findViewById(R.id.offline_status);
    offlineStatusStep.setOnButtonClickListener(this);
    replayStep = (StepView) findViewById(R.id.replay_offline);
    replayStep.setOnButtonClickListener(this);
    stopReplayStep = (StepView) findViewById(R.id.stop_replay);
    stopReplayStep.setOnButtonClickListener(this);
    getOfflineStatus();
    replayStep.setStepEnabled();
    offlineModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        sharedPrefs.edit().putBoolean(OFFLINE_MODE,isChecked).apply();
        if (isChecked){
          // Before we start offline Payment, we want to check if the merchant is eligible
          // to take offline Payments. Then we call the startOfflinePayment() API.
          // This API will have a callback which will return an error if there was one and a list
          // of offline transactions.
          if (RetailSDK.getTransactionManager().getOfflinePaymentEligibility()){
            RetailSDK.getTransactionManager().startOfflinePayment(new TransactionManager.OfflinePaymentStatusCallback() {
              @Override
              public void offlinePaymentStatus(RetailSDKException error, List<OfflinePaymentStatus> statusList) {
                if (error != null) {
                  Toast.makeText(getApplicationContext(), error.getDeveloperMessage(), Toast.LENGTH_LONG).show();
                }
              }
            });
          } else {
            Toast.makeText(getApplicationContext(), "Merchant not whitelisted for offline payments", Toast.LENGTH_LONG).show();
          }
        }else{
          RetailSDK.getTransactionManager().stopOfflinePayment(new TransactionManager.OfflinePaymentStatusCallback()
          {
            @Override
            public void offlinePaymentStatus(RetailSDKException error, List<OfflinePaymentStatus> list)
            {
              if (error != null) {
                Toast.makeText(getApplicationContext(), error.getDeveloperMessage(), Toast.LENGTH_LONG).show();
              }
            }
          });
        }
      }
    });

    intiViewState();
  }

  private void intiViewState()
  {
    if (sharedPrefs.getBoolean(OFFLINE_MODE,false)){
      offlineModeSwitch.setChecked(true);
      replayStep.setStepEnabled();
      stopReplayStep.setStepDisabled();
    }else{
      offlineModeSwitch.setChecked(false);
      if (sharedPrefs.getBoolean(REPLAY_IN_PROGRESS,false)){
        replayStep.showProgressBar();
        stopReplayStep.setStepEnabled();
        offlineModeSwitch.setEnabled(false);
      }else{
        replayStep.setStepEnabled();
        stopReplayStep.setStepDisabled();
      }

    }
  }

  private void showSwitchingToOnlineDialog(String title, String message, final Boolean setChecked){
    Log.d(LOG_TAG, "showSwitchingToOnlineDialog");
    AlertDialog.Builder builder = new AlertDialog.Builder(OfflinePayActivity.this);
    builder.setTitle(title);
    builder.setMessage(message);
    builder.setCancelable(false);
    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_TAG, "replaying while in offline mode alert dialog onClick");
        offlineModeSwitch.setChecked(setChecked);
        dialog.dismiss();
      }
    });
    builder.create().show();
  }

  private void replayOfflineTxns(){
    if (sharedPrefs.getBoolean(OFFLINE_INIT, false))
    {
      showSwitchingToOnlineDialog(getString(R.string.replay_dialog_offline_init_title), getString(R.string.replay_dialog_offline_init_message), true);
      return;
    } else if (sharedPrefs.getBoolean(OFFLINE_MODE, false)) {
      showSwitchingToOnlineDialog(getString(R.string.replay_dialog_offline_mode_title), getString(R.string.replay_dialog_offline_mode_message), false);
    }

    replayStep.showProgressBar();
    stopReplayStep.setStepEnabled();
    offlineModeSwitch.setEnabled(false);
    sharedPrefs.edit().putBoolean(REPLAY_IN_PROGRESS,true).apply();

    RetailSDK.getTransactionManager().startReplayOfflineTxns(new TransactionManager.OfflinePaymentStatusCallback()
    {
      @Override
      public void offlinePaymentStatus(RetailSDKException e, List<OfflinePaymentStatus> list)
      {
        sharedPrefs.edit().putBoolean(REPLAY_IN_PROGRESS,false).apply();
        if(activityVisible){
          final String toPrint = getStringToPrint(list);
          runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              statusText.setText(toPrint);
              stopReplayStep.setStepDisabled();
              offlineModeSwitch.setEnabled(true);
              replayStep.hideProgressBarShowButton();
            }
          });

        }

      }
    });
  }

  private void stopReplay()
  {
    sharedPrefs.edit().putBoolean(REPLAY_IN_PROGRESS,false).apply();
    RetailSDK.getTransactionManager().stopReplayOfflineTxns(new TransactionManager.OfflinePaymentStatusCallback() {
      @Override
      public void offlinePaymentStatus(RetailSDKException error, List<OfflinePaymentStatus> statusList) {
        if (error != null) {
          Toast.makeText(getApplicationContext(), error.getDeveloperMessage(), Toast.LENGTH_LONG).show();
        }
      }
    });
    stopReplayStep.setStepDisabled();
    offlineModeSwitch.setEnabled(true);
    replayStep.hideProgressBarShowButton();


  }


  private void getOfflineStatus()
  {
    RetailSDK.getTransactionManager().getOfflinePaymentStatus(new TransactionManager.OfflinePaymentStatusCallback()
    {
      @Override
      public void offlinePaymentStatus(RetailSDKException e, List<OfflinePaymentStatus> list)
      {


        final String toPrint = getStringToPrint(list);
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            statusText.setText(toPrint);
          }
        });
      }
    });
  }
  private String getStringToPrint(List<OfflinePaymentStatus> list)
  {
    String toPrint = "No pending offline transactions";
    int activeTransactionsCount = 0;
    int completedTransactionsCount = 0;
    int declinedTransactionsCount = 0;
    int deletedTransactionsCount = 0;
    int failedTransactionsCount = 0;
    if(list!=null && list.size()>0){
      toPrint = "";
      for (OfflinePaymentStatus status:list){
        OfflineTransactionState state = status.getState();
        switch (state){
          case Active:
            activeTransactionsCount += 1;
            break;
          case Completed:
            completedTransactionsCount += 1;
            break;
          case Declined:
            declinedTransactionsCount += 1;
            break;
          case Deleted:
            deletedTransactionsCount += 1;
            break;
          case Failed:
            failedTransactionsCount += 1;
            break;

        }
      }

      toPrint += "Active : " + activeTransactionsCount + "\n"
          + "Completed : " + completedTransactionsCount + "\n"
          + "Declined : " + declinedTransactionsCount + "\n"
          + "Deleted : " + deletedTransactionsCount + "\n"
          + "Failed : " + failedTransactionsCount;



    }
    return toPrint;
  }


  @Override
  protected void onPause()
  {
    super.onPause();
    activityVisible = false;
  }


  @Override
  public void onClick(View v)
  {
    if (v == offlineStatusStep.getButton()){
      getOfflineStatus();
    }else if(v == replayStep.getButton()){
      replayOfflineTxns();
    }else if(v == stopReplayStep.getButton()){
      stopReplay();
    }
  }



}
