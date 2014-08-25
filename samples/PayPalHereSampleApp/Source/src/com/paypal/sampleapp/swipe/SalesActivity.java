package com.paypal.sampleapp.swipe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.paypal.sampleapp.R;
import com.paypal.sampleapp.adapter.TransactionListAdapter;
import com.paypal.sampleapp.util.LocalPreferences;

public class SalesActivity extends Activity {
    private static final String LOG_TAG = SalesActivity.class.getSimpleName();
    public static final int SEND_RECEIPT_ACTIVITY_REQ_CODE = 8001;

    private ListView mListView = null;
    private TransactionListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate IN");
    }

    @Override
    protected void onResume(){
        super.onResume();
        mAdapter = new TransactionListAdapter(this, TransactionListAdapter.TransactionListType.COMPLETED_TRANSACTION_LIST);
        mAdapter.addItems(LocalPreferences.getCompletedTransactionRecordList());
        if(0 == mAdapter.getCount()){
            setContentView(R.layout.no_items_view);
        }else{
            setContentView(R.layout.list_view);
            mListView = (ListView)findViewById(R.id.list_view);
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG,"onActivityResult requestCode: "+requestCode);
        if(SEND_RECEIPT_ACTIVITY_REQ_CODE == requestCode){
            finish();
        }
    }
}
