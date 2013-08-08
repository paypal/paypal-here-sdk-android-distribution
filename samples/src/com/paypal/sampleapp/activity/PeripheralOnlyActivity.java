/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.PeripheralsListener;
import com.paypal.merchant.sdk.PeripheralsManager;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.sampleapp.R;


public class PeripheralOnlyActivity extends Activity implements PeripheralsListener {

    private static final int BT_REQ_CODE = 1234;
    private static final String LOG = "PeripheralOnlyActivity";
    private PeripheralsManager mPeripheralManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripherals_only);

        PayPalHereSDK.init(getApplicationContext(), "Sandbox");
        mPeripheralManager = PayPalHereSDK.getPeripheralsManager();
        registerTransactionAndPeripheralsListener(true);
        mPeripheralManager.waitForAuthorization();

    }

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {
        Log.d(LOG, "onPaymentReaderConnected");
        displayPaymentState("Connected to : " + readerType.name() + " via " + transport.name());

    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {
        Log.d(LOG, "onPaymentReaderDisconnected");
        displayPaymentState(readerType.name() + " disconnected!!!");
    }

    @Override
    public void onCardReadSuccess(SecureCreditCard paymentCard) {
        Log.d(LOG, "onCardReadSuccess");
        displayPaymentState("Card read success for : " + paymentCard.getCardHoldersName());

    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> error) {
        Log.d(LOG, "onCardReadFailed");
        displayPaymentState("Card read failure : " + error.getDetailedMessage());

    }

    @Override
    public void onPeripheralEvent(PeripheralEvent e) {
        Log.d(LOG, "onPeripheralEvent");
        displayPaymentState(e.getEventType().name());

    }

    private void registerTransactionAndPeripheralsListener(boolean isRegister) {
        if (mPeripheralManager == null)
            return;

        if (isRegister) {
            Log.d(LOG, "registered");
            mPeripheralManager.registerPeripheralsListener(this);
        } else {
            Log.d(LOG, "un-registered");

            mPeripheralManager.unregisterPeripheralsListener(this);

        }

    }

    private void displayPaymentState(String msg) {
        ((TextView) findViewById(R.id.status)).setText(msg);
    }
}
