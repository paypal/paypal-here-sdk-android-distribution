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
  private ImageView getOfflineArrowImage;

  private Button getOfflineStatusButton;
  private Button replayOfflineTxnButton;
  private Button stopReplayButton;


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
    getGetOfflineStatusClickText = (TextView)view.findViewById(R.id.txt_get_offline_status);
    replayClickText = (TextView)view.findViewById(R.id.txt_replay_offline_txn);
    stopReplayClickText = (TextView) view.findViewById(R.id.txt_stop_replay);
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
          onStartReplayClicked();
          onStopReplayClicked();
        }
        else
        {
          RetailSDK.getTransactionManager().stopOfflinePayment();
          enableStartReplayOption();
        }
      }
    });

    setCancelable(false);


    return view;

  }


  private void enableStartReplayOption()
  {
    replayArrowImage.setImageResource(R.drawable.small_bluearrow);
    replayClickText.setTextColor(getResources().getColor(R.color.sdk_blue));
    replayClickText.setClickable(true);
  }

  private void enableStopReplayOption()
  {
    stopReplayArrowImage.setImageResource(R.drawable.small_bluearrow);
    stopReplayClickText.setTextColor(getResources().getColor(R.color.sdk_blue));
    stopReplayClickText.setClickable(true);
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



  public void onStartReplayClicked()
  {
    replayArrowImage.setImageResource(R.drawable.small_greenarrow);
    replayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    replayClickText.setClickable(false);

  }

  public void onStopReplayClicked()
  {
    stopReplayArrowImage.setImageResource(R.drawable.small_greenarrow);
    stopReplayClickText.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
    stopReplayClickText.setClickable(false);

  }


  public interface OfflineModeDialogListener{
    void onGetOfflineStatusClicked(View view);
    void onReplayOfflineTransactionClicked(View view);
    void onStopReplayClicked(View view);
    void closeOfflineModeDialog(View view);
  }



}
