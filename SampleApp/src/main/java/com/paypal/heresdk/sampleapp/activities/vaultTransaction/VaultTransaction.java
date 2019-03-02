package com.paypal.heresdk.sampleapp.activities.vaultTransaction;

import java.util.Objects;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.ui.StepView;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.VaultRecord;


public class VaultTransaction extends AppCompatActivity
{
  private String LOG_TAG = VaultTransaction.class.getSimpleName();
  private VaultTransactionViewModel _vaultTransactionViewModel;

  StepView _vaultStep;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_vault_transaction);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

    _vaultTransactionViewModel = ViewModelProviders.of(VaultTransaction.this).get(VaultTransactionViewModel.class);
    _vaultStep = findViewById(R.id.begin_vault_step);
    _vaultStep.setStepEnabled();
  }


  @Override
  protected void onStart()
  {
    super.onStart();

    _vaultStep.setOnButtonClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {

        _vaultTransactionViewModel.getVaultRecordLiveData().observe(VaultTransaction.this, new Observer<VaultRecord>()
        {
          @Override
          public void onChanged(@Nullable VaultRecord vaultRecord)
          {
            if (vaultRecord != null)
            {
              _vaultStep.setStepCompleted();
            }
          }
        });
        _vaultTransactionViewModel.getRetailSDKExceptionMutableLiveData().observe(VaultTransaction.this, new Observer<RetailSDKException>()
        {
          @Override
          public void onChanged(@Nullable RetailSDKException error)
          {
            if (error != null)
            {
              _vaultStep.setStepCrossed();
            }
          }
        });
        _vaultTransactionViewModel.beginPayment();
      }
    });
  }
}
