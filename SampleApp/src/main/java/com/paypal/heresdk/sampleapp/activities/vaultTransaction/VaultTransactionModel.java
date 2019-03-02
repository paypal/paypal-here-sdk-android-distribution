package com.paypal.heresdk.sampleapp.activities.vaultTransaction;

import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.TransactionManager;

class VaultTransactionModel
{

  String getBraintreeLoginURL()
  {
    return RetailSDK.getBraintreeManager().getBtLoginUrl();
  }


  boolean isBraintreeReturnUrlValid(String returnUrl)
  {
    return RetailSDK.getBraintreeManager().isBtReturnUrlValid(returnUrl);
  }


  void createTransaction(TransactionManager.TransactionCallback transactionCallback)
  {
    RetailSDK.getTransactionManager().createVaultTransaction(transactionCallback);
  }
}
