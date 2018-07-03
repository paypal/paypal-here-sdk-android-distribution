package com.paypal.heresdk.sampleapp.ui;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.OfflinePaymentStatus;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionManager;
import org.w3c.dom.Text;


public class OfflineModeDialogFragment extends DialogFragment
{

  private Switch offlineModeSwitch;
  private TextView getOfflineStatusCode;
  private TextView replayOfflineStatusCode;
  private TextView stopReplayCode;
  private TextView getGetOfflineStatusClickText;
  private TextView replayClickText;
  private TextView stopReplayClickText;
  private ImageView replayArrowImage;
  private ImageView stopReplayArrowImage;
  private ImageView getOfflineArrowImage;



  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_offline_mode_dialog, container, false);

    getOfflineStatusCode = (TextView) view.findViewById(R.id.get_offline_status_code);
    replayOfflineStatusCode = (TextView) view.findViewById(R.id.replay_offline_transaction_code);
    stopReplayCode = (TextView) view.findViewById(R.id.stop_replay_code);
    getGetOfflineStatusClickText = (TextView)view.findViewById(R.id.txt_get_offline_status);
    replayClickText = (TextView)view.findViewById(R.id.txt_replay_offline_txn);
    stopReplayClickText = (TextView) view.findViewById(R.id.txt_stop_replaty);
    offlineModeSwitch = (Switch) view.findViewById(R.id.offline_mode_switch);
    replayArrowImage = (ImageView) view.findViewById(R.id.replay_arrow_image);
    getOfflineArrowImage = (ImageView) view.findViewById(R.id.get_offline_status_image);
    stopReplayArrowImage = (ImageView) view.findViewById(R.id.stop_replay_image);

    offlineModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        if (isChecked)
        {
          RetailSDK.getTransactionManager().startOfflinePayment(null);
          startReplayClicked();
          stopReplayClicked();
        }
        else
        {
          RetailSDK.getTransactionManager().stopOfflinePayment();
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

  public void getOfflineStatusClicked()
  {
    getOfflineArrowImage.setImageResource(R.drawable.small_greenarrow);
    getGetOfflineStatusClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    getGetOfflineStatusClickText.setClickable(false);

  }

  public void startReplayClicked()
  {
    replayArrowImage.setImageResource(R.drawable.small_greenarrow);
    replayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    replayClickText.setClickable(false);

  }

  public void stopReplayClicked()
  {
    stopReplayArrowImage.setImageResource(R.drawable.small_greenarrow);
    stopReplayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    stopReplayClickText.setClickable(false);

  }



}
