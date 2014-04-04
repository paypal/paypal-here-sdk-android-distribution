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
import android.widget.TextView;

import com.paypal.merchant.sdk.domain.ChipAndPinDecisionEvent;
import com.paypal.sampleapp.R;

import java.util.List;

public class ChipAndPinDecisionListAdapter extends ListAdapter<List<ChipAndPinDecisionEvent>> {

    public ChipAndPinDecisionListAdapter(Activity a, List<ChipAndPinDecisionEvent> eventList) {
        super(a, eventList);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = sInflater.inflate(R.layout.device_name, null);

        ChipAndPinDecisionEvent e = (ChipAndPinDecisionEvent) getItem(position);

        TextView name = (TextView) vi.findViewById(R.id.name);
        name.setText(e.getApplicationLabel());

        return vi;
    }
}
