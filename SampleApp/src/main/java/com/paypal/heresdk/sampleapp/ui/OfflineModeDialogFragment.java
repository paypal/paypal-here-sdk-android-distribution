package com.paypal.heresdk.sampleapp.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.OfflinePaymentStatus;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionManager;
import org.w3c.dom.Text;


public class OfflineModeDialogFragment extends DialogFragment implements View.OnClickListener
{

  private OfflineModeDialogListener offlineModeDialogListener;
  private Switch offlineModeSwitch;
  private TextView getOfflineStatusCode;
  private TextView replayOfflineStatusCode;
  private TextView stopReplayCode;
  private TextView getGetOfflineStatusClickText;
  private TextView replayClickText;
  private TextView stopReplayClickText;
  private ImageView replayArrowImage;
  private ImageView stopReplayArrowImage;
  private TextView viewCodeOfflineStatus;
  private TextView viewCodeReplay;
  private TextView viewCodeStopReplay;


  private ProgressBar progressBar;


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      offlineModeDialogListener = (OfflineModeDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OfflineModeDialogListener");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_offline_mode_dialog, container, false);

    getOfflineStatusCode = (TextView) view.findViewById(R.id.get_offline_status_code);
    replayOfflineStatusCode = (TextView) view.findViewById(R.id.replay_offline_transaction_code);
    stopReplayCode = (TextView) view.findViewById(R.id.stop_replay_code);
    getGetOfflineStatusClickText = (TextView) view.findViewById(R.id.txt_get_offline_status);
    getGetOfflineStatusClickText.setOnClickListener(this);
    replayClickText = (TextView) view.findViewById(R.id.txt_replay_offline_txn);
    replayClickText.setOnClickListener(this);
    stopReplayClickText = (TextView) view.findViewById(R.id.txt_stop_replay);
    stopReplayClickText.setOnClickListener(this);
    offlineModeSwitch = (Switch) view.findViewById(R.id.offline_mode_switch);
    replayArrowImage = (ImageView) view.findViewById(R.id.replay_arrow_image);
    stopReplayArrowImage = (ImageView) view.findViewById(R.id.stop_replay_image);
    progressBar = (ProgressBar) view.findViewById(R.id.progress);

    viewCodeOfflineStatus = (TextView) view.findViewById(R.id.view_code_get_offline_status);
    viewCodeOfflineStatus.setOnClickListener(this);

    viewCodeReplay = (TextView)view.findViewById(R.id.view_code_replay_offline_txn);
    viewCodeReplay.setOnClickListener(this);

    viewCodeStopReplay = (TextView) view.findViewById(R.id.view_code_stop_replay);
    viewCodeStopReplay.setOnClickListener(this);
    offlineModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        offlineModeDialogListener.onOfflineModeSwitchToggled(isChecked);
        if (isChecked)
        {
          RetailSDK.getTransactionManager().startOfflinePayment(null);
          startReplayOptionDisabledState();
          stopReplayOptionDisabledState();
        }
        else
        {
          RetailSDK.getTransactionManager().stopOfflinePayment();
          startReplayOptionEnabledState();
        }
      }
    });

    setCancelable(false);


    return view;

  }


  public boolean isGetOfflineStatusCodeVisible()
  {
    return getOfflineStatusCode.getVisibility() == View.VISIBLE;
  }


  public boolean isReplayOfflineTxnVisible()
  {
    return replayOfflineStatusCode.getVisibility() == View.VISIBLE;
  }


  public boolean isStopReplayCodeVisible()
  {
    return stopReplayCode.getVisibility() == View.VISIBLE;
  }


  public void showOfflineStatusCode()
  {
    getOfflineStatusCode.setVisibility(View.VISIBLE);
  }

  public void hideOfflineStatusCode()
  {
    getOfflineStatusCode.setVisibility(View.GONE);
  }


  public void showReplayCode()
  {
    replayOfflineStatusCode.setVisibility(View.VISIBLE);
  }
  public void hideReplayCode()
  {
    replayOfflineStatusCode.setVisibility(View.GONE);
  }


  public void showStopReplayCode()
  {
    stopReplayCode.setVisibility(View.VISIBLE);
  }
  public void hideStopReplayCode()
  {
    stopReplayCode.setVisibility(View.GONE);
  }



  public void startReplayOptionEnabledState()
  {
    progressBar.setVisibility(View.INVISIBLE);
    replayArrowImage.setVisibility(View.VISIBLE);
    replayArrowImage.setImageResource(R.drawable.small_bluearrow);
    replayClickText.setTextColor(getResources().getColor(R.color.sdk_blue));
    replayClickText.setClickable(true);
  }

  public void startReplayOptionDisabledState()
  {
    progressBar.setVisibility(View.INVISIBLE);
    replayArrowImage.setVisibility(View.VISIBLE);
    replayArrowImage.setImageResource(R.drawable.small_greenarrow);
    replayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    replayClickText.setClickable(false);
  }

  public void startReplayOptionProgressState()
  {
    replayArrowImage.setVisibility(View.INVISIBLE);
    progressBar.setVisibility(View.VISIBLE);
    replayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    replayClickText.setClickable(false);
  }

  public void stopReplayOptionEnabledState()
  {

    stopReplayArrowImage.setImageResource(R.drawable.small_bluearrow);
    stopReplayClickText.setTextColor(getResources().getColor(R.color.sdk_blue));
    stopReplayClickText.setClickable(true);
  }

  public void stopReplayOptionDisabledState()
  {

    stopReplayArrowImage.setImageResource(R.drawable.small_greenarrow);
    stopReplayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    stopReplayClickText.setClickable(false);
  }




  @Override
  public void onClick(View v)
  {
    int id = v.getId();
    switch (id){
      case R.id.txt_get_offline_status:
        RetailSDK.getTransactionManager().getOfflinePaymentStatus(new TransactionManager.OfflinePaymentStatusCallback()
        {
          @Override
          public void offlinePaymentStatus(RetailSDKException e, List<OfflinePaymentStatus> list)
          {
            Log.d("hg","gh");
            getActivity().runOnUiThread(new Runnable()
            {
              @Override
              public void run()
              {
                updateStatus();
              }
            });
          }
        });
        break;
      case R.id.txt_replay_offline_txn:
        RetailSDK.getTransactionManager().startReplayOfflineTxns(new TransactionManager.OfflinePaymentStatusCallback()
        {
          @Override
          public void offlinePaymentStatus(RetailSDKException e, List<OfflinePaymentStatus> list)
          {
            getActivity().runOnUiThread(new Runnable()
            {
              @Override
              public void run()
              {
                startReplayOptionEnabledState();
                stopReplayOptionDisabledState();
                enableSwitch();

              }
            });

          }
        });
        startReplayOptionProgressState();
        stopReplayOptionEnabledState();
        disableSwitch();


        break;
      case R.id.txt_stop_replay:
        RetailSDK.getTransactionManager().stopReplayOfflineTxns(null);
        startReplayOptionEnabledState();
        stopReplayOptionDisabledState();
        enableSwitch();
        break;
      case R.id.view_code_get_offline_status:
        if (isGetOfflineStatusCodeVisible())
        {
          hideOfflineStatusCode();
        }
        else
        {
          showOfflineStatusCode();
        }
        break;
      case R.id.view_code_replay_offline_txn:
        if (isReplayOfflineTxnVisible())
        {
          hideReplayCode();
        }
        else
        {
          showReplayCode();
        }
        break;
      case R.id.view_code_stop_replay:
        if (isStopReplayCodeVisible())
        {
          hideStopReplayCode();
        }
        else
        {
          showStopReplayCode();
        }
    }
  }


  private void updateStatus()
  {

  }


  private void enableSwitch()
  {
    offlineModeSwitch.setEnabled(true);
  }

  private void disableSwitch()
  {
    offlineModeSwitch.setEnabled(false);
  }


  public interface OfflineModeDialogListener{
    void onOfflineModeSwitchToggled(boolean isChecked);
    void closeOfflineModeDialog(View view);
  }



}
