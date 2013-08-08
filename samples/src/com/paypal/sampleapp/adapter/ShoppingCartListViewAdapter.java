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
import com.paypal.merchant.sdk.domain.shopping.CartItem;
import com.paypal.merchant.sdk.domain.shopping.ShoppingCart;
import com.paypal.sampleapp.R;

import java.math.RoundingMode;

/**
 * A list view adapter class that displays the items in list for the itemized
 * transaction type.
 */

public class ShoppingCartListViewAdapter extends ListAdapter<ShoppingCart> {

    public ShoppingCartListViewAdapter(Activity a, ShoppingCart itemList) {
        super(a, itemList);

    }

    @Override
    public int getCount() {
        return mData.getCartItems().size();
    }

    @Override
    public Object getItem(int position) {
        return mData.getCartItems().get(position);
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
            vi = sInflater.inflate(R.layout.itemized_list, null);

        CartItem i = (CartItem) getItem(position);

        TextView name = (TextView) vi.findViewById(R.id.item_name);
        name.setText(i.getName());

        TextView price = (TextView) vi.findViewById(R.id.item_price);
        price.setText(String.valueOf(i.getPrice().setScale(2, RoundingMode.CEILING)));

        TextView quantity = (TextView) vi.findViewById(R.id.item_quantity);
        quantity.setText(String.valueOf(mData.getQuantityForItem(i)));

        return vi;
    }

}
