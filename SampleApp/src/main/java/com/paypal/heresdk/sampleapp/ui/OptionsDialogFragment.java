package com.paypal.heresdk.sampleapp.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.FormFactor;


public class OptionsDialogFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener
{

  private boolean isAuthCaptureEnabled = false;
  private boolean isCardReaderPromptEnabled = true;
  private boolean isAppPromptEnabled = true;
  private boolean isTippingOnReaderEnabled = false;
  private boolean isAmountBasedTippingEnabled = false;

  private boolean isMagneticSwipeEnabled = true;
  private boolean isChipEnabled = true;
  private boolean isContactlessEnabled = true;
  private boolean isManualCardEnabled = true;
  private boolean isSecureManualEnabled = true;

  private Switch authCaptureSwitch;
  private Switch cardReaderPromptSwitch;
  private Switch appPromptSwitch;
  private Switch tippingOnReaderSwitch;
  private Switch amountBasedTippingSwitch;
  private CheckBox magneticSwipe;
  private CheckBox chip;
  private CheckBox contactless;
  private CheckBox manualCard;
  private CheckBox secureManual;
  private EditText tag;


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.fragment_options_dialog, container, false);
    authCaptureSwitch = (Switch) view.findViewById(R.id.auth_capture_switch);
    authCaptureSwitch.setOnCheckedChangeListener(this);
    cardReaderPromptSwitch = (Switch) view.findViewById(R.id.show_prompt_card_reader_switch);
    cardReaderPromptSwitch.setOnCheckedChangeListener(this);
    appPromptSwitch = (Switch) view.findViewById(R.id.show_prompt_app_switch);
    appPromptSwitch.setOnCheckedChangeListener(this);
    tippingOnReaderSwitch = (Switch) view.findViewById(R.id.tipping_reader_switch);
    tippingOnReaderSwitch.setOnCheckedChangeListener(this);
    amountBasedTippingSwitch = (Switch) view.findViewById(R.id.amount_tipping_switch);
    amountBasedTippingSwitch.setOnCheckedChangeListener(this);
    tag = (EditText) view.findViewById(R.id.tag);
    magneticSwipe = (CheckBox) view.findViewById(R.id.magnetic_swipe);
    magneticSwipe.setOnCheckedChangeListener(this);
    chip = (CheckBox) view.findViewById(R.id.chip);
    chip.setOnCheckedChangeListener(this);
    contactless = (CheckBox) view.findViewById(R.id.contactless);
    contactless.setOnCheckedChangeListener(this);
    manualCard = (CheckBox) view.findViewById(R.id.manual_card);
    manualCard.setOnCheckedChangeListener(this);
    secureManual = (CheckBox) view.findViewById(R.id.secure_manual);
    secureManual.setOnCheckedChangeListener(this);

    initDefaults();

    setCancelable(false);


    return view;

  }


  private void initDefaults()
  {
    authCaptureSwitch.setChecked(isAuthCaptureEnabled);
    cardReaderPromptSwitch.setChecked(isCardReaderPromptEnabled);
    appPromptSwitch.setChecked(isAppPromptEnabled);
    tippingOnReaderSwitch.setChecked(isTippingOnReaderEnabled);
    amountBasedTippingSwitch.setChecked(isAmountBasedTippingEnabled);
    magneticSwipe.setChecked(isMagneticSwipeEnabled);
    chip.setChecked(isChipEnabled);
    contactless.setChecked(isContactlessEnabled);
    manualCard.setChecked(isManualCardEnabled);
    secureManual.setChecked(isSecureManualEnabled);
  }


  public boolean isAuthCaptureEnabled()
  {
    return isAuthCaptureEnabled;
  }


  public boolean isCardPreaderPromptEnabled()
  {
    return isCardReaderPromptEnabled;
  }


  public boolean isAppPromptSwitchEnabled()
  {
    return isAppPromptEnabled;
  }


  public boolean isTippingOnReaderEnabled()
  {
    return isTippingOnReaderEnabled;
  }


  public boolean isAmountBasedTippingEnabled()
  {
    return isAmountBasedTippingEnabled;
  }


  public String getTagValue()
  {
    if (tag == null){
      return "";
    }
    return tag.getText().toString();
  }


  public List<FormFactor> getPreferredFormFactors()
  {
    List<FormFactor> formFactors = new ArrayList<>();
    if (isMagneticSwipeEnabled)
    {
      formFactors.add(FormFactor.MagneticCardSwipe);
    }
    if (isChipEnabled)
    {
      formFactors.add(FormFactor.Chip);
    }
    if (isContactlessEnabled)
    {
      formFactors.add(FormFactor.EmvCertifiedContactless);
    }
    if (isSecureManualEnabled)
    {
      formFactors.add(FormFactor.SecureManualEntry);
    }
    if (isManualCardEnabled)
    {
      formFactors.add(FormFactor.ManualCardEntry);
    }

    if (formFactors.size() == 0)
    {
      formFactors.add(FormFactor.None);
    }
    return formFactors;

  }


  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
  {
    int id = buttonView.getId();
    switch (id){
      case R.id.auth_capture_switch:
        isAuthCaptureEnabled = isChecked;
        break;
      case R.id.show_prompt_card_reader_switch:
        isCardReaderPromptEnabled = isChecked;
        break;
      case R.id.show_prompt_app_switch:
        isAppPromptEnabled = isChecked;
        break;
      case R.id.tipping_reader_switch:
        isTippingOnReaderEnabled = isChecked;
        break;
      case R.id.amount_tipping_switch:
        isAmountBasedTippingEnabled = isChecked;
        break;
      case R.id.magnetic_swipe:
        isMagneticSwipeEnabled = isChecked;
        break;
      case R.id.chip:
        isChipEnabled = isChecked;
        break;
      case R.id.contactless:
        isContactlessEnabled = isChecked;
        break;
      case R.id.manual_card:
        isManualCardEnabled = isChecked;
        break;
      case R.id.secure_manual:
        isSecureManualEnabled = isChecked;
    }

  }
}
