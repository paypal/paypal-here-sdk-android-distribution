/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.helper;

import android.content.Context;

import java.io.File;

/**
 * This class is responsible for maintaining a file cache to store the images.
 */

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        // Find the dir to save cached images
        /*
         * if (android.os.Environment.getExternalStorageState().equals(
		 * android.os.Environment.MEDIA_MOUNTED)) cacheDir = new File(
		 * android.os.Environment.getExternalStorageDirectory(), "LazyList");
		 * else
		 */
        cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}