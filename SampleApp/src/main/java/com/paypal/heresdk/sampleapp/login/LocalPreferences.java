package com.paypal.heresdk.sampleapp.login;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalPreferences {
    private static final String LOG_TAG = LocalPreferences.class.getSimpleName();

    private static String SHARED_PREFS_SANDBOX_MID_TIER_TOKEN = "SHARED_PREFS_SANDBOX_MID_TIER_TOKEN";
    private static String SHARED_PREFS_LIVE_MID_TIER_TOKEN = "SHARED_PREFS_LIVE_MID_TIER_TOKEN";

    private static String SHARED_PREFS_LIVE_MID_TIER_ACCESS_TOKEN = "SHARED_PREFS_LIVE_MID_TIER_ACCESS_TOKEN";
    private static String SHARED_PREFS_LIVE_MID_TIER_REFRESH_URL = "SHARED_PREFS_LIVE_MID_TIER_REFRESH_URL";
    private static String SHARED_PREFS_LIVE_MID_TIER_ENV = "SHARED_PREFS_LIVE_MID_TIER_ENV";

    private static String SHARED_PREFS_SANDBOX_MID_TIER_ACCESS_TOKEN = "SHARED_PREFS_SANDBOX_MID_TIER_ACCESS_TOKEN";
    private static String SHARED_PREFS_SANDBOX_MID_TIER_REFRESH_URL = "SHARED_PREFS_SANDBOX_MID_TIER_REFRESH_URL";
    private static String SHARED_PREFS_SANDBOX_MID_TIER_ENV = "SHARED_PREFS_SANDBOX_MID_TIER_ENV";

    private static String SHARED_PREFS_NAME = "MyTokens";

    public static void storeSandboxMidTierToken(Context context, String token){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_PREFS_SANDBOX_MID_TIER_TOKEN,token);
        editor.commit();
    }

    public static void storeSandboxMidTierCredentials(Context context, String access_token, String refresh_url, String env){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_PREFS_SANDBOX_MID_TIER_ACCESS_TOKEN,access_token);
        editor.putString(SHARED_PREFS_SANDBOX_MID_TIER_REFRESH_URL,refresh_url);
        editor.putString(SHARED_PREFS_SANDBOX_MID_TIER_ENV,env);
        editor.commit();
    }

    public static void storeLiveMidTierToken(Context context, String token){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_PREFS_LIVE_MID_TIER_TOKEN,token);
        editor.commit();
    }

    public static void storeLiveMidTierCredentials(Context context, String access_token, String refresh_url, String env){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_PREFS_LIVE_MID_TIER_ACCESS_TOKEN,access_token);
        editor.putString(SHARED_PREFS_LIVE_MID_TIER_REFRESH_URL,refresh_url);
        editor.putString(SHARED_PREFS_LIVE_MID_TIER_ENV,env);
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

    public static String getLiveMidtierAccessToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SHARED_PREFS_LIVE_MID_TIER_ACCESS_TOKEN, null);
    }

    public static String getLiveMidtierRefreshUrl(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SHARED_PREFS_LIVE_MID_TIER_REFRESH_URL, null);
    }

    public static String getLiveMidtierEnv(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SHARED_PREFS_LIVE_MID_TIER_ENV, null);
    }

    public static String getSandboxMidtierAccessToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SHARED_PREFS_SANDBOX_MID_TIER_ACCESS_TOKEN, null);
    }

    public static String getSandboxMidtierRefreshUrl(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SHARED_PREFS_SANDBOX_MID_TIER_REFRESH_URL, null);
    }

    public static String getSandboxMidtierEnv(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SHARED_PREFS_SANDBOX_MID_TIER_ENV, null);
    }



}
