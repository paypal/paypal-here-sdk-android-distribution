package com.paypal.sampleapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.paypal.merchant.sdk.domain.InvoiceItem;
import com.paypal.sampleapp.R;

import java.util.ArrayList;

public class InvoiceItemAdapter extends BaseAdapter {
    public class MyInvoiceItem{
        InvoiceItem mInvoiceItem;
        int mCount;

        public MyInvoiceItem(InvoiceItem item){
            mInvoiceItem = item;
            mCount = 1;
        }
    }

    private class ViewHolder {
        private TextView mNameView;
        private TextView mPriceView;
        private TextView mQuantityView;
    }

    private ArrayList<MyInvoiceItem> mItems;
    private LayoutInflater mLayoutInflator = null;
    private Context mContext = null;

    public InvoiceItemAdapter(Context ctx){
        mContext = ctx;
        mLayoutInflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems = new ArrayList<MyInvoiceItem>();
    }

    public void addItem(InvoiceItem item){
        for(MyInvoiceItem myItem: mItems){
            if(myItem.mInvoiceItem.getName().equalsIgnoreCase(item.getName())){
                myItem.mCount++;
                notifyDataSetChanged();
                return;
            }
        }
        MyInvoiceItem myItem = new MyInvoiceItem(item);
        mItems.add(myItem);
        notifyDataSetChanged();
    }

    public void clearAllItems(){
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        MyInvoiceItem myItem = mItems.get(i);
        if(null == view){
            view = mLayoutInflator.inflate(R.layout.itemized_list,null);
            holder = new ViewHolder();
            holder.mNameView = (TextView)view.findViewById(R.id.item_name);
            holder.mPriceView = (TextView)view.findViewById(R.id.item_price);
            holder.mQuantityView = (TextView)view.findViewById(R.id.item_quantity);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        if(null != myItem.mInvoiceItem.getName()){
            holder.mNameView.setText(myItem.mInvoiceItem.getName());
        }

        if(null != myItem.mInvoiceItem.getPrice()){
            holder.mPriceView.setText(myItem.mInvoiceItem.getPrice().toString());
        }

        holder.mQuantityView.setText(String.valueOf(myItem.mCount));
        return view;
    }
}
