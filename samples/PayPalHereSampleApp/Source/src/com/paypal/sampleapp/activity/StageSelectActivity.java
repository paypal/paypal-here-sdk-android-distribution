/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.sampleapp.R;

public class StageSelectActivity extends ListActivity {

    ArrayAdapter<String> mListViewAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_stage);
        mListViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,
                PayPalHereSDK.getAvailableServers());

        setListAdapter(mListViewAdapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String[] servers = PayPalHereSDK.getAvailableServers();
        if (position < servers.length) {
            PayPalHereSDK.setServerName(servers[position]);
            finish();
        }
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