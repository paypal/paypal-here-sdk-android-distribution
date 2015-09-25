/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

/**
 * This class is basically meant to provide an option bar at the top, with the menu items for Merchant Checkin and
 * Checkout.
 * <p/>
 */
public class OptionsMenuBaseActivity extends TabActivity {

    private static final String LOG = "OptionsMenuBaseActivity";
    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean returnValue;

        switch (item.getItemId()) {
            case R.id.checkin:

                sMerchantManager.checkinMerchant("Joe's Pizza",
                        new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {

                            @Override
                            public void onSuccess(Merchant merchant) {
                                CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                                        "Merchant checkin successful!");
                                Log.e(LOG, "Checkin successful");

                            }

                            @Override
                            public void onError(PPError<MerchantManager.MerchantErrors> error) {
                                CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                                        "Checkin unsuccessful. " + error.getDetailedMessage());

                                Log.e(LOG, "Checkin unsuccessful. " + error.getDetailedMessage());
                            }
                        }
                );

                returnValue = true;

                break;

            case R.id.checkout:

                sMerchantManager.checkoutMerchant(new DefaultResponseHandler<Merchant,
                        PPError<MerchantManager.MerchantErrors>>() {
                    @Override
                    public void onSuccess(Merchant merchant) {
                        CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                                "Merchant checkout successful!");
                        Log.d(LOG, "Checkout successful");
                    }

                    @Override
                    public void onError(PPError<MerchantManager.MerchantErrors> error) {
                        CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                                "Merchant checkout unsuccessful!");
                        Log.e(LOG, "Checkout unsuccessful. " + error.getDetailedMessage());
                    }
                });

                returnValue = true;

                break;

            case R.id.bt_connect:
                returnValue = true;
                break;

            case R.id.sdk_settings:
                Intent in = new Intent(OptionsMenuBaseActivity.this, SettingsActivity.class);
                startActivity(in);

                returnValue = true;
                break;

            default:
                returnValue = super.onOptionsItemSelected(item);

        }
        return returnValue;

    }
}
