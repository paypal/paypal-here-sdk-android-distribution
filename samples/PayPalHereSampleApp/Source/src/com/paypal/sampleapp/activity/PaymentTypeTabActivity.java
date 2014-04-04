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

import com.paypal.sampleapp.R;

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

        Intent intent = new Intent().setClass(this, CreditCardPeripheralActivity.class);
        spec = tabHost.newTabSpec("Swipe").setIndicator("Swipe", getResources().getDrawable(R.drawable
                .ic_launcher)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CreditCardManualActivity.class);
        spec = tabHost.newTabSpec("Key-in").setIndicator("Key-in", getResources().getDrawable(R.drawable
                .ic_launcher)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, PayPalMerchantCheckinActivity.class);
        spec = tabHost.newTabSpec("Check-in").setIndicator("Check-in", getResources().getDrawable(R.drawable
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
