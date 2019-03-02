package com.paypal.heresdk.sampleapp.activities.vaultAndPayTransaction;

import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.TransactionManager;

class VaultAndPayTransactionModel
{

  String getBraintreeLoginURL()
  {
    return RetailSDK.getBraintreeManager().getBtLoginUrl();
  }


  boolean isBraintreeReturnUrlValid(String redirectUrl)
  {
    return RetailSDK.getBraintreeManager().isBtReturnUrlValid(redirectUrl);
  }


  Invoice createInvoice()
  {
    return new Invoice(RetailSDK.getMerchant().getCurrency());
  }


  void createTransaction(Invoice invoice, TransactionManager.TransactionCallback transactionCallback)
  {
    RetailSDK.getTransactionManager().createTransaction(invoice, transactionCallback);
  }
}
