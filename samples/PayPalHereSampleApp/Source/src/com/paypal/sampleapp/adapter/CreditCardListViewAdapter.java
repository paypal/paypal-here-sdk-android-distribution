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

import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.sampleapp.R;

import java.util.List;


public class CreditCardListViewAdapter extends ListAdapter<List<SecureCreditCard>> {

    public CreditCardListViewAdapter(Activity a, List<SecureCreditCard> data) {
        super(a, data);

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
            vi = sInflater.inflate(R.layout.device_name, null);

        SecureCreditCard s = (SecureCreditCard) getItem(position);

        TextView name = (TextView) vi.findViewById(R.id.name);
        if (s.getDataSourceType() == SecureCreditCard.DataSourceType.MIURA_SWIPE ||
                s.getDataSourceType() == SecureCreditCard.DataSourceType.MIURA_FB_SWIPE ||
                s.getDataSourceType() == SecureCreditCard.DataSourceType.MIURA_CHIP)
            name.setText("Emv card data read : " + s.getLastFourDigits());
        else
            name.setText(s.getCardHoldersName() + " : " + s.getLastFourDigits());

        return vi;
    }

}

