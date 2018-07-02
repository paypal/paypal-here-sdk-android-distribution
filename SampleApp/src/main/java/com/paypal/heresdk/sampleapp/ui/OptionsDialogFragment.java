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
import android.widget.EditText;
import android.widget.Switch;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.FormFactor;


public class OptionsDialogFragment extends DialogFragment
{

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
    authCaptureSwitch = (Switch)view.findViewById(R.id.auth_capture_switch);
    cardReaderPromptSwitch = (Switch)view.findViewById(R.id.show_prompt_card_reader_switch);
    appPromptSwitch = (Switch)view.findViewById(R.id.show_prompt_app_switch);
    tippingOnReaderSwitch = (Switch)view.findViewById(R.id.tipping_reader_switch);
    amountBasedTippingSwitch = (Switch)view.findViewById(R.id.amount_tipping_switch);
    tag = (EditText) view.findViewById(R.id.tag);
    magneticSwipe = (CheckBox) view.findViewById(R.id.magnetic_swipe);
    chip = (CheckBox)view.findViewById(R.id.chip);
    contactless = (CheckBox)view.findViewById(R.id.contactless);
    manualCard =(CheckBox) view.findViewById(R.id.manual_card);
    secureManual =(CheckBox) view.findViewById(R.id.secure_manual);

    setCancelable(false);



    return view;

  }


  public boolean isAuthCaptureChecked(){
    return authCaptureSwitch.isChecked();
  }
  public boolean isCardPreaderPromptChecked(){
    return cardReaderPromptSwitch.isChecked();
  }

  public boolean isAppPromptSwitchChecked(){
    return appPromptSwitch.isChecked();
  }

  public boolean isTippingOnReaderChecked(){
    return tippingOnReaderSwitch.isChecked();
  }

  public boolean isAmountBasedTippingChecked(){
    return amountBasedTippingSwitch.isChecked();
  }

  public String getTagValue(){
    return tag.getText().toString();
  }

  public List<FormFactor> getPreferredFormFactors(){
    List<FormFactor> formFactors= new ArrayList<>();
    if (magneticSwipe.isChecked()){
      formFactors.add(FormFactor.MagneticCardSwipe);
    }
    if (chip.isChecked()){
      formFactors.add(FormFactor.Chip);
    }
    if(contactless.isChecked()){
      formFactors.add(FormFactor.EmvCertifiedContactless);
    }
    if (secureManual.isChecked()){
      formFactors.add(FormFactor.SecureManualEntry);
    }
    if(manualCard.isChecked()){
      formFactors.add(FormFactor.ManualCardEntry);
    }

    if (formFactors.size() == 0){
      formFactors.add(FormFactor.None);
    }
    return formFactors;

  }


}
