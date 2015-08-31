/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.emv.sampleapp.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.emv.sampleapp.login.OAuthLoginActivity;
import com.paypal.merchant.sdk.PayPalHereSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class CommonUtils {
    private static final String LOG_TAG = CommonUtils.class.getSimpleName();
    public static final String PREFS_NAME = "MerchantPrefs";
    public static final String REFRESH_URL = "refreshUrl";
    public static final String PREFS_USERNAME = "PREFS_LAST_GOOD_USERNAME";
    public static final String PREFS_SERVER = "PREFS_LAST_GOOD_SERVER";
    public static final String PREFS_EMV_CONFIG_REPO = "PREFS_LAST_GOOD_EMV_CONFIG_REPO";
    public static final String PREFS_SERVER_COUNTRY = "PREFS_SERVER_COUNTRY";
    private static SharedPreferences mSharedPrefs;

    public static boolean isNullOrEmpty(String s) {
        return s == null ? true : (s.trim().length() <= 0);
    }

    public static void createToastMessage(final Activity activity, final String msg) {
        if (!isNullOrEmpty(msg))
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT);
                    toast.show();

                }
            });
    }

    public static String getString(Object t) {
        String s = "";

        if (t instanceof EditText) {
            s = ((EditText) t).getText().toString();
        } else {
            s = ((TextView) t).getText().toString();
        }
        return s;
    }

    public static String getStringFromId(final Activity activity, int resId) {
        if (activity == null)
            return "";

        return activity.getString(resId);
    }

    public static double getDoubleValue(String s) {
        return (isNullOrEmpty(s) ? 0.0 : Double.valueOf(s));
    }

    public static String getMonthString(int month) {
        switch (month) {
            case Calendar.JANUARY:
                return "Jan";
            case Calendar.FEBRUARY:
                return "Feb";
            case Calendar.MARCH:
                return "Mar";
            case Calendar.APRIL:
                return "Apr";
            case Calendar.MAY:
                return "May";
            case Calendar.JUNE:
                return "Jun";
            case Calendar.JULY:
                return "Jul";
            case Calendar.AUGUST:
                return "Aug";
            case Calendar.SEPTEMBER:
                return "Sep";
            case Calendar.OCTOBER:
                return "Oct";
            case Calendar.NOVEMBER:
                return "Nov";
            case Calendar.DECEMBER:
                return "Dec";
            default:
                return "";
        }
    }

    public static int getBuildNumber(Activity activity) {
        int buildNumber = 0;
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            buildNumber = pInfo.versionCode;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to fetch the build number information.");
        }

        return buildNumber;

    }

    public static String getVersionNumber(Activity activity) {
        String version = "";
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to fetch the version number information.");
        }
        return version;

    }

    public static void setStage(Context context, String name){
        Log.d(LOG_TAG,"setStage setting the stage as: "+name);
        if(isNullOrEmpty(name)){
            Log.e(LOG_TAG,"setStage incoming stage name is null or empty. Name: "+name);
        }

        if(name.equals(PayPalHereSDK.Live) || name.equals(PayPalHereSDK.Sandbox) || name.equals(PayPalHereSDK.MockServer)){
            PayPalHereSDK.setServerName(name);
        }else {
            String url = "https://www." + name + ".stage.paypal.com";
            JSONObject object = new JSONObject();
            try {
                JSONArray array = new JSONArray();
                JSONObject urlObject = new JSONObject();
                urlObject.put("name", name);
                urlObject.put("url", url);
                array.put(urlObject);
                object.put("servers", array);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSONException whle setting the stage. StageName: " + name);
                e.printStackTrace();
                return;
            }
            PayPalHereSDK.setOptionalServerList(object.toString());
            PayPalHereSDK.setServerName(name);
        }
        CommonUtils.saveServer(context, name);
    }

    public static boolean isMockServer(Context context){
        String serverName = getStoredServer(context);
        if(isNullOrEmpty(serverName)){
            return false;
        }

        if(serverName.equals(PayPalHereSDK.MockServer)){
            return true;
        }

        return false;
    }

    public static void saveServer(Context context, String server) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_SERVER, server);
        editor.commit();
        Log.d(LOG_TAG, "Saving the server: " + server);
    }

    public static void saveEMVConfigStage(Context context, String swRepo) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_EMV_CONFIG_REPO, swRepo);
        editor.commit();
        Log.d(LOG_TAG, "Saving the EMV Config Repo: " + swRepo);
    }

    public static void saveMockCountryCode(Context context, PayPalHereSDK.MerchantCountryForMock country) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_SERVER_COUNTRY, country.toString());
        editor.commit();
        Log.d(LOG_TAG, "Saving the country code for Mock: " + country);
    }

    public static void saveRefreshUrl(Context context, String refreshUrl) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(REFRESH_URL, refreshUrl);
        editor.commit();
        Log.d(LOG_TAG, "Saving the refresh url: " + refreshUrl);
    }

    public static void saveUsername(Context context, String userName) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(PREFS_USERNAME, userName);
        editor.commit();
        Log.d(LOG_TAG, "Saving the username: " + userName);
    }

    public static String getRefreshUrl(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(REFRESH_URL, null);
    }

    public static String getStoredServer(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(PREFS_SERVER, null);
    }

    public static String getStoredEMVConfigRepo(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(PREFS_EMV_CONFIG_REPO, null);
    }

    public static String getStoredUsername(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        return mSharedPrefs.getString(PREFS_USERNAME, null);
    }

    public static PayPalHereSDK.MerchantCountryForMock getStoredMerchantCountry(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        return PayPalHereSDK.MerchantCountryForMock.toMerchantCountryForMock(mSharedPrefs.getString(PREFS_SERVER_COUNTRY, null));
    }

    public static  void removedSavedRefreshUrl(Context context) {
        mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        if (mSharedPrefs != null) {
            mSharedPrefs.edit().remove(REFRESH_URL).commit();
        }
    }
}
