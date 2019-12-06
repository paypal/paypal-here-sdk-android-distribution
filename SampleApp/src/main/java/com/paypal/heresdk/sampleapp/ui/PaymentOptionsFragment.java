package com.paypal.heresdk.sampleapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import android.util.Log;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.RetailSDK;

public class PaymentOptionsFragment extends PreferenceFragmentCompat
{
  final String logComponent = "PaymentOptionsFragment";


  SwitchPreferenceCompat authCapturePreference;
  SwitchPreferenceCompat vaultPreference;
  SwitchPreferenceCompat promptAppPreference;
  SwitchPreferenceCompat promptReaderPreference;
  SwitchPreferenceCompat amountTippingPreference;
  SwitchPreferenceCompat enableQuickChipPreference;
  SwitchPreferenceCompat readerTipPreference;
  SwitchPreferenceCompat chipPreference;
  SwitchPreferenceCompat contactlessPreference;
  SwitchPreferenceCompat magneticSwipePreference;
  SwitchPreferenceCompat manualCardPreference;
  SwitchPreferenceCompat secureManualPreference;
  SwitchPreferenceCompat BTloginPref;

  EditTextPreference tag;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
  {
    setPreferencesFromResource(R.xml.preferences,rootKey);
    authCapturePreference =  (SwitchPreferenceCompat) findPreference(getString(R.string.auth_capture));
    vaultPreference =  (SwitchPreferenceCompat) findPreference(getString(R.string.vault_only));

    promptAppPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.show_prompt_in_app));
    promptReaderPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.show_prompt_in_card_reader));
    amountTippingPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.amount_based_tipping));
    enableQuickChipPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.enable_quick_chip));
    readerTipPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.tipping_on_reader));
    chipPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.chip));
    contactlessPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.contactless));
    magneticSwipePreference = (SwitchPreferenceCompat) findPreference(getString(R.string.magnetic_swipe));
    manualCardPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.manual_card));
    secureManualPreference = (SwitchPreferenceCompat) findPreference(getString(R.string.secure_manual));
    tag = (EditTextPreference)findPreference(getString(R.string.edit_text_pref));

    BTloginPref = (SwitchPreferenceCompat) findPreference(getString(R.string.login_bt));
    BTloginPref.setChecked(false);

    //WidgetContainer v = (WidgetContainer) BTloginPref.getWidgetLayoutResource();


    BTloginPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        int Rid = preference.getLayoutResource();

        String btLoginURL = RetailSDK.getBraintreeManager().getBtLoginUrl();

        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra(WebViewActivity.INTENT_URL_WEBVIEW, btLoginURL);
        startActivityForResult(intent, 1);

        return true;
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    getActivity();

    if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
      //some code
      if (data.hasExtra(WebViewActivity.INTENT_URL_RESULT))
      {
        String url = (String) data.getSerializableExtra(WebViewActivity.INTENT_URL_RESULT);
        Log.d(logComponent, "this is the overloaded url " + url);
        if (RetailSDK.getBraintreeManager().isBtReturnUrlValid(url)) {
          Log.d(logComponent, "YES, it does contain auth code! ");
        } else {
          Log.d(logComponent, "NO, it does not contain auth code! ");
        }
      }
    }

  }


  // getters

  public boolean getAuthCapturePreference()
  {
    return authCapturePreference.isChecked();
  }

  public boolean getVaultPreference()
  {
    return vaultPreference.isChecked();
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

  public boolean getQuickChipEnabledPreference()
  {
    return enableQuickChipPreference.isChecked();
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

  public void setVaultPreference(boolean isChecked)
  {
    vaultPreference.setChecked(isChecked);
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

  public void setQuickChipEnabledPreference(boolean isChecked){
    enableQuickChipPreference.setChecked(isChecked);
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
