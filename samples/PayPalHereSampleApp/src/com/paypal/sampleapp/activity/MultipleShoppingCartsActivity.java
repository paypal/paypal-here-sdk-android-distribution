/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.PeripheralsListener;
import com.paypal.merchant.sdk.TransactionListener;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

import java.math.BigDecimal;
import java.util.Random;

public class MultipleShoppingCartsActivity extends MyActivity implements TransactionListener, PeripheralsListener {

    private static final String LOG = "PayPalHere.MultipleShoppingCartsActivity";
    DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>
            mPaymentResponseHandler = new DefaultResponseHandler<TransactionManager.PaymentResponse,
            PPError<TransactionManager.PaymentErrors>>() {
        public void onSuccess(TransactionManager.PaymentResponse response) {
            updateUIForPurchaseSuccess(response);
            //mAnotherTransButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(PPError<TransactionManager.PaymentErrors> e) {
            updateUIForPurchaseError(e);

        }
    };
    private RadioGroup mShoppingCartGroup;
    private RadioGroup mCreditCardGroup;
    private SecureCreditCard mCreditCard;
    private BigDecimal mAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_shopping_carts);

        mShoppingCartGroup = (RadioGroup) findViewById(R.id.shopping_cart_list_radio_group);
        mCreditCardGroup = (RadioGroup) findViewById(R.id.credit_card_list_radio_group);

        Button b = (Button) findViewById(R.id.multiple_shopping_cart_purchase_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePayment();
            }
        });

        b = (Button) findViewById(R.id.add_shopping_cart_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateShoppingCart();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getPeripheralsManager().registerPeripheralsListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getPeripheralsManager().unregisterPeripheralsListener(this);
    }

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {
        displayStatusMsg("Card Reader Connected!. Ready for Swipe");

    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {
        displayStatusMsg("No readers found!");

    }

    @Override
    public void onCardReadSuccess(SecureCreditCard paymentCard) {
        displayStatusMsg("Card read success!");
        storeCardData(paymentCard);
    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> error) {
        displayStatusMsg("Bad Swipe.  Try Again!");
    }

    @Override
    public void onPeripheralEvent(PeripheralEvent e) {
        PeripheralEvents type = e.getEventType();

        if (type == PeripheralEvents.NeedPin) {
            displayStatusMsg("Ask use to enter their pin!");
        }
    }

    @Override
    public void onPaymentEvent(PaymentEvent e) {
        PaymentEventType type = e.getEventType();

        if (type == PaymentEventType.ProcessingPayment) {
            displayStatusMsg("Processing Payment");
        } else if (type == PaymentEventType.GettingAuthorization) {
            displayStatusMsg("Scanning for any card data");
        }
    }

    private void takePayment() {

        if (mCreditCard == null || mAmount == null) {
            CommonUtils.createToastMessage(MultipleShoppingCartsActivity.this, "Invalid!");
            return;
        }
        displayStatusMsg("Taking Payment...");
        PayPalHereSDK.getTransactionManager().beginPayment(mAmount);
        PayPalHereSDK.getTransactionManager().finalizePayment(mCreditCard, mPaymentResponseHandler);

    }

    private void generateShoppingCart() {
        int r = new Random().nextInt(15);
        final BigDecimal amnt = new BigDecimal(r);
        PayPalHereSDK.getTransactionManager().beginPayment(amnt);
        RadioButton b = new RadioButton(this);
        b.setText("$ " + r);
        mShoppingCartGroup.addView(b);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmount = amnt;
            }
        });

    }

    private void storeCardData(final SecureCreditCard scc) {

        RadioButton b = new RadioButton(this);
        b.setText(scc.getCardHoldersName() + " : " + scc.getLastFourDigits());
        mCreditCardGroup.addView(b);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreditCard = scc;

            }
        });

    }

    private void updateUIForPurchaseError(PPError<TransactionManager.PaymentErrors> e) {
        TransactionManager.PaymentErrors error_type = e.getErrorCode();

        if (TransactionManager.PaymentErrors.PaymentDeclined == error_type) {
            displayStatusMsg("Payment declined!  Payment cycle complete.  Please start again");
        } else if (TransactionManager.PaymentErrors.NetworkTimeout == error_type) {
            displayStatusMsg("Payment timed out at network level.");
        } else if (TransactionManager.PaymentErrors.NoDeviceForCardPresentPayment == error_type) {
            displayStatusMsg("No Device connected.  Connect your device.");
        } else if (TransactionManager.PaymentErrors.NoCardDataPresent == error_type) {
            displayStatusMsg("We can't take card payment ... no card has been scanned.");
        } else if (TransactionManager.PaymentErrors.TransactionCanceled == error_type) {
            displayStatusMsg("Payment Canceled after takePayment.  No more payment");
        } else if (TransactionManager.PaymentErrors.TimeoutWaitingForSwipe == error_type) {
            displayStatusMsg("Payment Canceled.  Expecting card swipe but no swipe ever happened");
        } else if (TransactionManager.PaymentErrors.BadConfiguration == error_type) {
            displayStatusMsg("Payment Canceled.  Incorrect Usage / Bad Configuration " + e.getDetailedMessage());
        } else if (TransactionManager.PaymentErrors.EmptyShoppingCart == error_type) {
            displayStatusMsg("You've got an empty shopping cart, or a cart with zero value.  Can't process payment");
        } else {
            displayStatusMsg("Unhandled error: " + e.getDetailedMessage());
        }
    }

    private void updateUIForPurchaseSuccess(TransactionManager.PaymentResponse response) {
        displayStatusMsg("Payment completed successfully!  TransactionId: " + response.getTransactionRecord()
                .getTransactionId());
    }

    private void displayStatusMsg(String msg) {
        TextView t = (TextView) findViewById(R.id.multiple_shopping_cart_purchase_status);
        t.setText(msg);
    }

}
