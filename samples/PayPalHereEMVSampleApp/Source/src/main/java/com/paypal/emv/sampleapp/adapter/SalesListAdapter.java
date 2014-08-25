package com.paypal.emv.sampleapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.paypal.emv.sampleapp.R;
import com.paypal.emv.sampleapp.listeners.AdapterListener;
import com.paypal.emv.sampleapp.utils.CommonUtils;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.TransactionRecord;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SalesListAdapter extends BaseAdapter {
    private static final String LOG_TAG = SalesListAdapter.class.getSimpleName();

    public static enum TransactionListType{
        AUTHORIZED_TRANSACTION_LIST,
        COMPLETED_TRANSACTION_LIST
    };

    private class ViewHolder {
        private TextView mInvoiceView;
        private TextView mDateView;
        private TextView mPriceView;
        private RelativeLayout mLayout;
    }

    private ArrayList<TransactionRecord> mItems;
    private LayoutInflater mLayoutInflator = null;
    private Context mContext = null;
    private TransactionListType mType;
    private AdapterListener mListner;

    public SalesListAdapter(Context ctx, TransactionListType type, AdapterListener listener) {
        mContext = ctx;
        mType = type;
        mListner = listener;
        mLayoutInflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems = new ArrayList<TransactionRecord>();
    }

    public void addItems(ArrayList<TransactionRecord> list) {
        for (TransactionRecord record : list) {
            mItems.add(record);
        }
        notifyDataSetChanged();
    }

    public void addItem(TransactionRecord item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void clearAllItems() {
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
        final TransactionRecord record = mItems.get(i);
        if (null == view) {
            view = mLayoutInflator.inflate(R.layout.transaction_list_item, null);
            holder = new ViewHolder();
            holder.mInvoiceView = (TextView) view.findViewById(R.id.id_invoice_view);
            holder.mDateView = (TextView) view.findViewById(R.id.id_date_view);
            holder.mPriceView = (TextView) view.findViewById(R.id.id_total_view);
            holder.mLayout = (RelativeLayout)view.findViewById(R.id.id_authorized_item_layout);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (null != record && null != record.getInvoice()) {
            Invoice invoice = record.getInvoice();

            if (null != invoice.getId()) {
                holder.mInvoiceView.setText(record.getInvoice().getId());
            }

            Date date = record.getTransactionDate();
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            String dateStr = calendar.get(Calendar.DAY_OF_MONTH)+" "+ CommonUtils.getMonthString(calendar.get(Calendar.MONTH)) +" "+calendar.get(Calendar.YEAR);
            holder.mDateView.setText(dateStr);

            holder.mPriceView.setText(PayPalHereSDK.getMerchantManager().getActiveMerchant().getMerchantCurrency().getCurrencyCode() + " " + String.format("%.2f", record.getInvoice().getGrandTotal().doubleValue()));

            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null != mListner){
                        mListner.onItemClicked(record);
                    }
                }
            });
        }

        return view;
    }
}
