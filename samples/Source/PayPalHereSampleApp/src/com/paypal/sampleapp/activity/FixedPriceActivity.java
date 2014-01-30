/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.*;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * An activity that takes in a fixed price amount.
 * * <p/>
 * In this activity, we also implement the peripheral listener so that we can listen to card swipes.
 */
public class FixedPriceActivity extends MyActivity implements CardReaderListener {

    private static final String LOG = "PayPalHere.FixedPrice";
    private static final int REQ_CODE = 7654;
    private EditText mFixedPrice;
    private BigDecimal mFixedPriceVal;

    /**
     * Initialize the elements in the layout.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting the layout for this activity.
        setContentView(R.layout.activity_fixed_price_tab_type);

        // Find and set the edit text to read the entered amount.
        mFixedPrice = (EditText) findViewById(R.id.fixed_price);

        // Find and setup the button for fixed price purchase button.
        Button b = (Button) findViewById(R.id.fixed_price_checkout_button);
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Validate the amount entered.
                if (!isValidAmount()) {
                    CommonUtils.createToastMessage(FixedPriceActivity.this,
                            CommonUtils.getStringFromId(FixedPriceActivity.this, R.string.invalid_amount_entered));
                    return;
                }

                Log.d(LOG, "Fixed price: " + mFixedPriceVal);
                // Inform the SDK of the fixed price transaction and provide the amount.
                // This would create an invoice with one item having the given amount.
                // NOTE: Once the beginPayment method is invoked, the customer is allowed to swipe in their card at
                // point in time.
                // This card data would be held by the SDK and would be used during the payment.
                // This feature is mainly aimed to offer flexibility as to when the card could be swiped.
                // When a card swipe is detected, a SecureCreditCard object is also returned back by the SDK,
                // which the apps can choose to store.
                Invoice i = PayPalHereSDK.getTransactionManager().beginPayment(mFixedPriceVal);
                // Set the cashier id (if present) within the invoice to indicate which cashier within the store
                // created this invoice.
                i.setCashierId(getCashierId());
                // Set the partner attribution (BN) code (if present) also within the invoice to indicate which
                // partner it came from.
                i.setReferrerCode(getBNCode());
                PayPalHereSDK.getTransactionManager().setActiveInvoice(i);

                // Send the amount to the next activity i.e., payment activity.
                Intent intent = new Intent(FixedPriceActivity.this, PaymentTypeTabActivity.class);
                startActivityForResult(intent, REQ_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE) {
            mFixedPrice.setText("");
            mFixedPriceVal = BigDecimal.ZERO;
        }

    }

    /**
     * Method to validate the amount entered.
     *
     * @return
     */
    private boolean isValidAmount() {
        String amount = CommonUtils.getString(mFixedPrice);
        if (CommonUtils.isNullOrEmpty(amount) || (this.mFixedPriceVal = new BigDecimal(amount)).doubleValue() <=
                BigDecimal.ZERO.doubleValue()) {

            return false;
        }
        return true;
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    /**
     * Below are the implementation for the CardReaderListener.
     */

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {

    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {

    }

    @Override
    public void onCardReadSuccess(SecureCreditCard paymentCard) {
        // Display a message stating that the card swipe was successful.
        // NOTE: Once the card has been successfully swiped or read, the SDK holds on to it as well as returns the
        // same in this method (paymentCard) for the app to hold the same if they want or need to.
        // So, when we do a finalizePayment in the next screen to take the payment and complete the transaction,
        // this card data (that is held by the SDK) is used and charged against.
        CommonUtils.createToastMessage(FixedPriceActivity.this, "Card read successful!!!");
    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> error) {
        // Display a message stating that the card swipe had a failure.
        CommonUtils.createToastMessage(FixedPriceActivity.this, "Card read failed");
    }

    @Override
    public void onCardReaderEvent(PPError<CardReaderEvents> peripheralEventsPPError) {

    }

    @Override
    public void onSelectPaymentDecision(List<ChipAndPinDecisionEvent> eventList) {

    }

    @Override
    public void onInvalidListeningPort() {
        CommonUtils.createToastMessage(this, "No valid listening port.");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getCardReaderManager().registerCardReaderListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getCardReaderManager().unregisterCardReaderListener(this);
    }

}
