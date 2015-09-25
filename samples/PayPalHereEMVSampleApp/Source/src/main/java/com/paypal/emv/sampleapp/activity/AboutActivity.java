package com.paypal.emv.sampleapp.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.utils.CommonUtils;

public class AboutActivity extends Activity {
    private static final String LOG_TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView buildNumber = (TextView) findViewById(R.id.about_app_build_number);
        buildNumber.setText("" + CommonUtils.getBuildNumber(AboutActivity.this));

        TextView versionNumber = (TextView) findViewById(R.id.about_app_version_number);
        versionNumber.setText(CommonUtils.getVersionNumber(AboutActivity.this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }
}
