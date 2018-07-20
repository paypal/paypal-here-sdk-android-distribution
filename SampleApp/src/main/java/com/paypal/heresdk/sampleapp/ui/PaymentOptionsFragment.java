package com.paypal.heresdk.sampleapp.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import com.paypal.heresdk.sampleapp.R;


public class PaymentOptionsFragment extends PreferenceFragmentCompat
{

  SwitchPreferenceCompat authCapturePreference;
  SwitchPreferenceCompat promptAppPreference;
  SwitchPreferenceCompat promptReaderPreference;
  SwitchPreferenceCompat amountTippingPreference;
  SwitchPreferenceCompat readerTipPreference;
  SwitchPreferenceCompat chipPreference;
  SwitchPreferenceCompat contactlessPreference;
  SwitchPreferenceCompat magneticSwipePreference;
  SwitchPreferenceCompat manualCardPreference;
  SwitchPreferenceCompat secureManualPreference;

  EditTextPreference tag;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
  {
    setPreferencesFromResource(R.xml.preferences,rootKey);
    authCapturePreference = (SwitchPreferenceCompat) findPreference(getString(R.string.auth_capture));
    promptAppPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.show_prompt_in_app));
    promptReaderPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.show_prompt_in_card_reader));
    amountTippingPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.amount_based_tipping));
    readerTipPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.tipping_on_reader));
    chipPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.chip));
    contactlessPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.contactless));
    magneticSwipePreference = (SwitchPreferenceCompat) findPreference(getString(R.string.magnetic_swipe));
    manualCardPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.manual_card));
    secureManualPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.secure_manual));
    tag = (EditTextPreference)findPreference(getString(R.string.edit_text_pref));

  }

  // getters

  public boolean getAuthCapturePreference()
  {
    return authCapturePreference.isChecked();
  }


  public boolean getPromptAppPreference()
  {
    return promptAppPreference.isChecked();
  }


  public boolean getPromptReaderPreference()
  {
    return promptReaderPreference.isChecked();
  }


  public boolean getAmountTippingPreference()
  {
    return amountTippingPreference.isChecked();
  }

  public boolean getTipOnReaderPreference()
  {
    return readerTipPreference.isChecked();
  }


  public boolean getChipPreference()
  {
    return chipPreference.isChecked();
  }


  public boolean getContactlessPreference()
  {
    return contactlessPreference.isChecked();
  }


  public boolean getMagneticSwipePreference()
  {
    return magneticSwipePreference.isChecked();
  }


  public boolean getManualCardPreference()
  {
    return manualCardPreference.isChecked();
  }


  public boolean getSecureManualPreference()
  {
    return secureManualPreference.isChecked();
  }


  public String getTagString()
  {
    return tag.getText();
  }

  // setters
  public void setAuthCapturePreference(boolean isChecked)
  {
    authCapturePreference.setChecked(isChecked);
  }


  public void setPromptAppPreference(boolean isChecked)
  {
    promptAppPreference.setChecked(isChecked);
  }


  public void setPromptReaderPreference(boolean isChecked)
  {
    promptReaderPreference.setChecked(isChecked);
  }


  public void setAmountTippingPreference(boolean isChecked)
  {
    amountTippingPreference.setChecked(isChecked);
  }

  public void setReaderTipPreference(boolean isChecked)
  {
    readerTipPreference.setChecked(isChecked);
  }


  public void setChipPreference(boolean isChecked)
  {
    chipPreference.setChecked(isChecked);
  }


  public void setContactlessPreference(boolean isChecked)
  {
    contactlessPreference.setChecked(isChecked);
  }


  public void setMagneticSwipePreference(boolean isChecked)
  {
    magneticSwipePreference.setChecked(isChecked);
  }


  public void setManualCardPreference(boolean isChecked)
  {
    manualCardPreference.setChecked(isChecked);
  }


  public void setSecureManualPreference(boolean isChecked)
  {
    secureManualPreference.setChecked(isChecked);
  }


  public void setTag(String tagText)
  {
    if(tag!=null)
    {
      tag.setText(tagText);
    }
  }






}
