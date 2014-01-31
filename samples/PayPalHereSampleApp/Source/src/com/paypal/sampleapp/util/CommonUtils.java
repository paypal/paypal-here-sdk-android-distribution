/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.util;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CommonUtils {

    /**
     * A method to check whether a string is null or empty.
     *
     * @param String .
     * @return boolean true or false.
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null ? true : (s.trim().length() <= 0);
    }

    /**
     * A method to create a toast message on any screen.
     *
     * @param activity : screen on which the toast message should be shown.
     * @param msg      : the message to be shown.
     */
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

    /**
     * A method get the string from either the edit text or a text view.
     *
     * @param t
     * @return string
     */

    public static String getString(Object t) {
        String s = "";

        if (t instanceof EditText) {
            s = ((EditText) t).getText().toString();
        } else {
            s = ((TextView) t).getText().toString();
        }
        return s;
    }

    /**
     * A method to get the string values from the res/strings.xml file.
     *
     * @param activity
     * @param resId
     * @return string
     */
    public static String getStringFromId(final Activity activity, int resId) {
        if (activity == null)
            return "";

        return activity.getString(resId);
    }

    /**
     * A generic method to get the double value from a string type.
     *
     * @param s
     * @return double
     */
    public static double getDoubleValue(String s) {
        return (isNullOrEmpty(s) ? 0.0 : Double.valueOf(s));
    }

}
