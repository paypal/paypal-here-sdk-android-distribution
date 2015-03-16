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
import android.view.Menu;
import android.widget.TabHost;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.emv.EMVTransactionActivity;
import com.paypal.sampleapp.swipe.SwipeTransactionActivity;

/**
 * A tab-activity that shows different methods of payment options.
 * <p/>
 * Currently supported ones include:
 * <p/>
 * 1. Peripheral Card Reader.
 * <p/>
 * 2. Key in the Credit Card Number.
 * <p/>
 * 3. PayPal Check-in.
 */
public class PaymentTypeTabActivity extends OptionsMenuBaseActivity {

    public static final String PAYMENT = "payment";

    /**
     * Called when the activity is first created.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_type);
        createTabActivities();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);

    }

    private void createTabActivities() {

        @SuppressWarnings("deprecation")
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        Intent intent = null;
        Boolean emvPayment = false;

        if(PayPalHereSDK.getMerchantManager().getActiveMerchant().getMerchantCurrency().getCurrencyCode().equalsIgnoreCase("GBP")
            || PayPalHereSDK.getMerchantManager().getActiveMerchant().getMerchantCurrency().getCurrencyCode().equalsIgnoreCase("AUD")){

            emvPayment = true;
        }else{
            emvPayment = false;
        }

        if(emvPayment){
            intent = new Intent().setClass(this, EMVTransactionActivity.class);
            spec = tabHost.newTabSpec(getString(R.string.payment_type_tab_emv_title)).setIndicator(getString(R.string.payment_type_tab_emv_title), getResources().getDrawable(R.drawable.emv_device)).setContent(intent);
            tabHost.addTab(spec);
        }else {
            intent = new Intent().setClass(this, SwipeTransactionActivity.class);
            spec = tabHost.newTabSpec(getString(R.string.payment_type_tab_swipe_title)).setIndicator(getString(R.string.payment_type_tab_swipe_title), getResources().getDrawable(R.drawable.ic_launcher)).setContent(intent);
            tabHost.addTab(spec);
        }


        intent = new Intent().setClass(this, CreditCardManualActivity.class);
        spec = tabHost.newTabSpec("Key-in").setIndicator("Key-in", getResources().getDrawable(R.drawable
                .ic_launcher)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, PayPalMerchantCheckinActivity.class);
        spec = tabHost.newTabSpec("Check-in").setIndicator("Check-in", getResources().getDrawable(R.drawable
                .ic_launcher)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CashActivity.class);
        spec = tabHost.newTabSpec("Cash").setIndicator("Cash", getResources().getDrawable(R.drawable
                .ic_launcher)).setContent(intent);
        tabHost.addTab(spec);


        tabHost.setCurrentTab(0);
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
