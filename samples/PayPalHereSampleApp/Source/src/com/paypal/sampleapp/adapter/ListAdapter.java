/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.paypal.sampleapp.helper.ImageLoader;

/**
 * A generic adaptor class to display text and image in a view.
 *
 * @param <T> : InstagramApiResponse.
 */
public abstract class ListAdapter<T> extends BaseAdapter {

    public static ImageLoader sImageLoader;
    protected static LayoutInflater sInflater = null;
    protected Activity mActivity;
    protected T mData;

    /**
     * @param a : current activity
     * @param d : data
     */
    public ListAdapter(Activity a, T d) {
        mActivity = a;
        mData = d;
        sInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sImageLoader = new ImageLoader(mActivity.getApplicationContext());
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public ImageLoader getImageLoader() {
        return sImageLoader;
    }

    /**
     * A method to get the total number of elements in the view.
     */
    public abstract int getCount();

    /**
     * A method to display a view.
     */
    public abstract View getView(int position, View convertView, ViewGroup parent);

}
