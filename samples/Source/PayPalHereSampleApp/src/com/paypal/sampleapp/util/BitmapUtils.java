/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * This utility class is meant to peform certain bitmap manipulation operations.
 */
public class BitmapUtils {

    private static volatile Bitmap bitmap;

    /**
     * This method converts a bitmap image to a byte array.
     *
     * @param bm
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * This method converts a byte array to a bitmap image.
     *
     * @param byteArray
     * @return
     */
    public static Bitmap getBitmapFromBytes(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

    }

    public static Bitmap getBitMap() {
        return bitmap;
    }

    public static void setBitMap(Bitmap bm) {
        bitmap = bm;
    }


}
