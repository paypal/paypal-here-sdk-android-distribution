package com.paypal.heresdk.sampleapp.login;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalPreferences {
    private static final String LOG_TAG = LocalPreferences.class.getSimpleName();

    private static String SHARED_PREFS_SANDBOX_MID_TIER_TOKEN = "SHARED_PREFS_SANDBOX_MID_TIER_TOKEN";
    private static String SHARED_PREFS_LIVE_MID_TIER_TOKEN = "SHARED_PREFS_LIVE_MID_TIER_TOKEN";

    private static String SHARED_PREFS_NAME = "MyTokens";

    public static void storeSandboxMidTierToken(Context context, String token){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_PREFS_SANDBOX_MID_TIER_TOKEN,token);
        editor.commit();
    }

    public static void storeLiveMidTierToken(Context context, String token){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_PREFS_LIVE_MID_TIER_TOKEN,token);
        editor.commit();
    }

    public static String getSandboxMidtierToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(SHARED_PREFS_SANDBOX_MID_TIER_TOKEN, null);
        return token;
    }

    public static String getLiveMidtierToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(SHARED_PREFS_LIVE_MID_TIER_TOKEN, null);
        return token;
    }
}
