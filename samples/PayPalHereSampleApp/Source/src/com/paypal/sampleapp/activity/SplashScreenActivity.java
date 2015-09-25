/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.login.LoginScreenActivity;

/**
 * This class displays the splash screen of the application.
 */
public class SplashScreenActivity extends Activity {

    private int SPLASH_DISPLAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_splash_screen);

		/*
         * Fades out the splash screen and fades in the next screen after the
		 * specified interval.
		 */
        new Handler().postDelayed(new Runnable() {
            public void run() {

                Intent intent = new Intent();
                intent.setClass(SplashScreenActivity.this, LoginScreenActivity.class);

                SplashScreenActivity.this.startActivity(intent);
                SplashScreenActivity.this.finish();

                overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            }
        }, SPLASH_DISPLAY_TIME);

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
