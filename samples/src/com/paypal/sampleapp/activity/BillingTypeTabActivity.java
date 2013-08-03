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
 * This tab-activity enables the merchant to either create an itemized billing
 * or a fixed price billing.
 */
public class BillingTypeTabActivity extends OptionsMenuBaseActivity {

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

        Intent intent = new Intent().setClass(this, ItemizedActivity.class);
        spec = tabHost.newTabSpec("Itemized").setIndicator("Itemized", getResources().getDrawable(R.drawable
                .ic_launcher)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, FixedPriceActivity.class);
        spec = tabHost.newTabSpec("Fixed Price").setIndicator("Fixed Price", getResources().getDrawable(R.drawable
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
