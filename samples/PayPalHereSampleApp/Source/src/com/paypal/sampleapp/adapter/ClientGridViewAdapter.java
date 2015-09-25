/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paypal.merchant.sdk.domain.CheckedInClient;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.util.CommonUtils;

import java.util.List;

/**
 * A list view adapter class that displays the items in list for the itemized
 * transaction type.
 */

public class ClientGridViewAdapter extends ListAdapter<List<CheckedInClient>> {

    public ClientGridViewAdapter(Activity a, List<CheckedInClient> itemList) {
        super(a, itemList);

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Display the items in a list.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = sInflater.inflate(R.layout.images_list, null);

        ImageView thumb_image = (ImageView) vi.findViewById(R.id.clientImage);
        TextView clientName = (TextView) vi.findViewById(R.id.clientName);

        if (mData != null) {
            CheckedInClient client = mData.get(position);
            String url = client.getPhotoUrl();
            clientName.setText(client.getClientsName());
            if (CommonUtils.isNullOrEmpty(url)) {
                thumb_image.setImageResource(R.drawable.ic_launcher);
            } else {
                sImageLoader.DisplayImage(url, thumb_image);

            }
        }
        return vi;
    }

}
