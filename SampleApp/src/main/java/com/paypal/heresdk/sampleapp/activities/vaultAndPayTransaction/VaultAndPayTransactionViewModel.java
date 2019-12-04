package com.paypal.heresdk.sampleapp.activities.vaultAndPayTransaction;

import java.math.BigDecimal;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import android.os.Handler;
import android.os.Looper;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.InvoiceItem;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionBeginOptions;
import com.paypal.paypalretailsdk.TransactionBeginOptionsPaymentTypes;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultProvider;
import com.paypal.paypalretailsdk.TransactionBeginOptionsVaultType;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;
import com.paypal.paypalretailsdk.VaultRecord;

public class VaultAndPayTransactionViewModel extends ViewModel
{
  private VaultAndPayTransactionModel _vaultAndPayTransactionModel;
  private Invoice _invoice;
  private final String LOG_TAG = "VaultNPayTransactionVM";
  private final String BRAINTREE_CUSTOMER_ID = "4085815786";
  private MutableLiveData<TransactionRecord> _transactionRecordLiveData;
  private MutableLiveData<VaultRecord> _vaultRecordLiveData;
  private MutableLiveData<RetailSDKException> _retailSDKExceptionMutableLiveData;


  public MutableLiveData<RetailSDKException> getRetailSDKExceptionMutableLiveData()
  {
    if (_retailSDKExceptionMutableLiveData == null)
    {
      _retailSDKExceptionMutableLiveData = new MutableLiveData<>();
    }
    return _retailSDKExceptionMutableLiveData;
  }


  public MutableLiveData<TransactionRecord> getTransactionRecordLiveData()
  {
    if (_transactionRecordLiveData == null)
    {
      _transactionRecordLiveData = new MutableLiveData<>();
    }
    return _transactionRecordLiveData;
  }


  public MutableLiveData<VaultRecord> getVaultRecordLiveData()
  {
    if (_vaultRecordLiveData == null)
    {
      _vaultRecordLiveData = new MutableLiveData<>();
    }
    return _vaultRecordLiveData;
  }


  public VaultAndPayTransactionViewModel()
  {
    _vaultAndPayTransactionModel = new VaultAndPayTransactionModel();
  }


  String getBraintreeLoginUrl()
  {
    return _vaultAndPayTransactionModel.getBraintreeLoginURL();
  }


  boolean isBraintreeReturnUrlValid(String returnUrl)
  {
    return _vaultAndPayTransactionModel.isBraintreeReturnUrlValid(returnUrl);
  }


  boolean createInvoice()
  {
    this._invoice = _vaultAndPayTransactionModel.createInvoice();
    return this._invoice != null;
  }


  boolean addInvoiceItem(String itemName, String quantity, String unitPrice, String itemId, String detailId)
  {
    if (this._invoice != null)
    {
      InvoiceItem _invoiceItem = this._invoice.addItem(
          itemName,
          new BigDecimal(quantity),
          new BigDecimal(unitPrice),
          Integer.valueOf(itemId),
          detailId
      );
      return _invoiceItem != null;
    }
    return false;
  }


  void beginPayment()
  {
    _vaultAndPayTransactionModel.createTransaction(
        VaultAndPayTransactionViewModel.this._invoice,
        new TransactionManager.TransactionCallback()
        {
          @Override
          public void transaction(RetailSDKException error, TransactionContext transactionContext)
          {
            if (error != null)
            {
              _retailSDKExceptionMutableLiveData.setValue(error);
            }
            else
            {
              transactionContext.setVaultCompletedHandler(new TransactionContext.VaultCompletedCallback()
              {
                @Override
                public void vaultCompleted(final RetailSDKException error, final VaultRecord vaultRecord)
                {
                  if (error != null)
                  {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _retailSDKExceptionMutableLiveData.setValue(error);
                      }
                    });
                  }
                  else
                  {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _vaultRecordLiveData.setValue(vaultRecord);
                      }
                    });
                  }
                }
              });
              transactionContext.setCompletedHandler(new TransactionContext.TransactionCompletedCallback()
              {
                @Override
                public void transactionCompleted(final RetailSDKException error, final TransactionRecord transactionRecord)
                {
                  if (error != null)
                  {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _retailSDKExceptionMutableLiveData.setValue(error);
                      }
                    });
                  }
                  else
                  {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _transactionRecordLiveData.setValue(transactionRecord);
                      }
                    });
                  }
                }
              });

              TransactionBeginOptions transactionBeginOptions = new TransactionBeginOptions();
              transactionBeginOptions.setPaymentType(TransactionBeginOptionsPaymentTypes.card);
              transactionBeginOptions.setVaultProvider(TransactionBeginOptionsVaultProvider.Braintree);
              transactionBeginOptions.setVaultType(TransactionBeginOptionsVaultType.PayAndVault);
              transactionBeginOptions.setVaultCustomerId(BRAINTREE_CUSTOMER_ID);
              transactionContext.beginPayment(transactionBeginOptions);
            }
          }
        }
    );
  }


  @Override
  protected void onCleared()
  {
    super.onCleared();
  }
}
