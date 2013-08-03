/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.TabActivity;
import android.content.SharedPreferences;
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
 * The most important feature of this class lies in the ability to save the checked in merchant's location ID and not
 * having him/her checkin multiple times, which results in a error thrown by the HERE API service.
 * <p/>
 * Currently, I am storing the location id through SharedPreferences.
 * <p/>
 * We might need to find an alternate approach to do this.
 */
public class OptionsMenuBaseActivity extends TabActivity {

    public static final String PREFS_NAME = "MerchantLocationPrefs";
    public static final String LOCATION_ID = "locationId";
    private static final String LOG = "OptionsMenuBaseActivity";
    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();
    private static SharedPreferences settings;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.locations, menu);
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
                if (((MyActivity) getCurrentActivity()).isMerchantLocationSaved()) {

                    CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                            "Merchant already checked in!");
                    Log.e(LOG, "Merchant already checked in");

                } else {
                    sMerchantManager.checkinMerchant("Joe's Pizza",
                            new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {

                                @Override
                                public void onSuccess(Merchant merchant) {
                                    CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                                            "Merchant checkin successful!");
                                    Log.e(LOG, "Checkin successful");

                                    ((MyActivity) getCurrentActivity()).checkAndSaveLocationId();

                                }

                                @Override
                                public void onError(PPError<MerchantManager.MerchantErrors> error) {
                                    CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                                            "Checkin unsuccessful. " + error.getDetailedMessage());

                                    Log.e(LOG, "Checkin unsuccessful. " + error.getDetailedMessage());
                                }
                            });

                }

                returnValue = true;

                break;

            case R.id.checkout:
                if (((MyActivity) getCurrentActivity()).isMerchantLocationSaved()) {

                    // clear the saved preference
                    ((MyActivity) getCurrentActivity()).removeSavedMerchantLocation();

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

                } else {
                    CommonUtils.createToastMessage(OptionsMenuBaseActivity.this,
                            "Merchant already checked out!");
                    Log.e(LOG, "Merchant already checked out");
                }

                returnValue = true;

                break;

            default:
                returnValue = super.onOptionsItemSelected(item);

        }
        return returnValue;

    }
}
