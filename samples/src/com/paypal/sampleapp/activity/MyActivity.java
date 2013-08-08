/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.CheckedInMerchantLocation;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.shopping.ShoppingCart;
import com.paypal.sampleapp.util.CommonUtils;


public class MyActivity extends Activity {

    public static final String PREFS_NAME = "MerchantLocationPrefs";
    public static final String LOCATION_ID = "locationId";
    private static final String LOG = "MyActivity";
    private static Bitmap sBitmap;
    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();
    private static SharedPreferences settings;
    private static ShoppingCart sShoppingCart;
    private static boolean sIsPaymentCompleted;

    public static Bitmap getBitmap() {
        return sBitmap;
    }

    public static void setBitmap(Bitmap bm) {
        sBitmap = bm;

    }

    public ShoppingCart getShoppingCart() {
        return sShoppingCart;
    }

    public void saveShoppingCart(ShoppingCart sc) {
        sShoppingCart = sc;

    }

    public void reInstateShoppingCart() {
        PayPalHereSDK.getTransactionManager().beginPayment();
        ShoppingCart sc = getShoppingCart();
        if (sc != null)
            PayPalHereSDK.getTransactionManager().setShoppingCart(sc);
    }

    /**
     * This method is used to save the merchant's location ID as a shared preference if the app goes into the
     * background or killed.
     */
    public void checkAndSaveLocationId() {

        // Check if the merchant has checked in their location
        if (isMerchantCheckedIdIn()) {
            //If checked in, get the current location id
            String locationId = sMerchantManager.getActiveMerchant().getCheckedInMerchant().getLocationId();

            settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(LOCATION_ID, locationId);

            // Commit the edits!
            editor.commit();

            Log.d("checkAndSaveLocationId", "Saving location id: " + locationId);

        }

    }

    /**
     * This method is used to retrieve the saved merchant location from the shared preference.
     *
     * @return
     */
    public boolean isMerchantLocationSaved() {
        // Check if the merchant has checked in their location
        if (isMerchantCheckedIdIn()) {
            Log.d(LOG, "merchant checked in and is the active merchant");
            return true;

        }
        //If checked in, get the stored location id from the shared prefs
        final String locationId = getLocationId();
        if (!CommonUtils.isNullOrEmpty(locationId)) {
            // Just a hack for the time being
            Log.d("retrieveSavedCheckedInMerchant", "Location id: " + locationId);
            //Get the active merchant object and set the location ID
            Merchant merchant = sMerchantManager.getActiveMerchant();
            merchant.setCheckedInMerchant(new CheckedInMerchantLocation() {
                @Override
                public String getStatus() {
                    return "active";
                }

                @Override
                public String getLocationId() {
                    return locationId;
                }

                @Override
                public String getUpdateDate() {
                    return null;
                }

                @Override
                public String getCreateDate() {
                    return null;
                }

                @Override
                public double getLatitude() {
                    return 0;
                }

                @Override
                public double getLongitude() {
                    return 0;
                }

                @Override
                public Bitmap getMerchantLogo() {
                    return null;
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Method to remove the location ID stored as a shared preference once the merchant has checked out.
     */
    public void removeSavedMerchantLocation() {
        // Check if the merchant has checked in their location
        if (isMerchantCheckedIdIn()) {

            settings = getSharedPreferences(PREFS_NAME, 0);
            if (settings != null)
                settings.edit().remove(LOCATION_ID).commit();

        }
    }

    /**
     * Method to check if the merchant has checked in.
     *
     * @return
     */
    public boolean isMerchantCheckedIdIn() {
        if (sMerchantManager != null && sMerchantManager.getActiveMerchant() != null && sMerchantManager
                .getActiveMerchant().getCheckedInMerchant() != null) {

            return true;
        }

        return false;
    }

    /**
     * Retrieves the stored location id from the shared preferences.
     *
     * @return
     */
    private String getLocationId() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(LOCATION_ID, null);
    }

    protected boolean isPaymentCompleted() {
        return sIsPaymentCompleted;
    }

    protected void paymentCompleted(boolean state) {
        sIsPaymentCompleted = state;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
