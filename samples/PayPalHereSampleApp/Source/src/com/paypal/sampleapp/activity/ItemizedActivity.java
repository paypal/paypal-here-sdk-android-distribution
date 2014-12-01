/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.DomainFactory;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.InvoiceItem;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.adapter.InvoiceItemAdapter;
import com.paypal.sampleapp.swipe.AuthorizedActivity;
import com.paypal.sampleapp.swipe.SalesActivity;
import com.paypal.sampleapp.util.CommonUtils;
import com.paypal.sampleapp.util.LocalPreferences;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * An activity that is used to create an itemized list.
 * <p/>
 * In this activity, we also implement the peripheral listener so that we can listen to card swipes.
 */
public class ItemizedActivity extends MyActivity {

    private static final String LOG = "PayPalHere.ItemizedActivity";
    private static final int REQ_CODE = 4567;
    private Invoice mInvoice = null;
    private ListView mListView;
    private InvoiceItemAdapter mAdapter;
    private Map<String, InvoiceItem> mStoreItems;
    private RelativeLayout mHeaderLayout;
    private Button mPurchaseButton;
    private Button mAuthorizedTransactionsButton;
    private Button mSalesHistoryButton;

    private enum FruitTypeEnum {
        APPLE, BANANA, ORANGE, STRAWBERRY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting the layout for this activity.
        setContentView(R.layout.activity_itemized_tab_type);
        mHeaderLayout = (RelativeLayout) findViewById(R.id.header);
        mHeaderLayout.setVisibility(View.GONE);
        // Here we init all the items that are present in the store.
        // This way, we can access those items and update the quantity.
        initStoreItems();

        ImageButton ib = (ImageButton) findViewById(R.id.add_apple);
        ib.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addFruit(FruitTypeEnum.APPLE);
            }
        });

        ib = (ImageButton) findViewById(R.id.add_orange);
        ib.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addFruit(FruitTypeEnum.ORANGE);
            }
        });

        ib = (ImageButton) findViewById(R.id.add_banana);
        ib.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addFruit(FruitTypeEnum.BANANA);
            }
        });

        ib = (ImageButton) findViewById(R.id.add_strawberry);
        ib.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addFruit(FruitTypeEnum.STRAWBERRY);
            }
        });


        mPurchaseButton = (Button) findViewById(R.id.id_purchase_button);
        mPurchaseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mInvoice == null || mInvoice.getItems().size() <= 0) {
                    CommonUtils.createToastMessage(ItemizedActivity.this, "Cannot proceed with an empty items list.");
                    return;
                }
                // Set the cashier id (if present) within the invoice to indicate which cashier within the store created this invoice.
                mInvoice.setCashierId(LocalPreferences.getCashierID());
                // Set the partner attribution (BN) code (if present) also within the invoice to indicate which partner it came from.
                mInvoice.setReferrerCode(LocalPreferences.getBNCode());

                Intent intent = new Intent(ItemizedActivity.this, PaymentTypeTabActivity.class);
                startActivityForResult(intent, REQ_CODE);
            }
        });
        mPurchaseButton.setVisibility(View.GONE);

        mAuthorizedTransactionsButton = (Button)findViewById(R.id.id_authorized_transactions_button);
        mAuthorizedTransactionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG,"Authorized Transaction Button onClick");
                Intent intent = new Intent(ItemizedActivity.this, AuthorizedActivity.class);
                ItemizedActivity.this.startActivity(intent);
            }
        });

        mSalesHistoryButton = (Button)findViewById(R.id.id_completed_transactions_button);
        mSalesHistoryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG,"Sales History Button onClick");
                Intent intent = new Intent(ItemizedActivity.this, SalesActivity.class);
                ItemizedActivity.this.startActivity(intent);
            }
        });

        CommonUtils.createToastMessage(ItemizedActivity.this, "Is device compatible : " + PayPalHereSDK.getCardReaderManager().getDeviceCompatibilityModel().isModelSupported());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQ_CODE == requestCode) {
            clearListAdapter();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * The sample app creates a hash map of all the items in the store (in this case, we only have 4).
     * We need this because the invoice updates the quantity of items in case of addition or removal of items
     * and it requires the SAME item object to perform this action.
     * <p/>
     * For example:
     * Let say we have an item InvoiceItem item_1 = new InvoiceItem("apple",1) and put this into our invoice:
     * invoice.add(item_1);
     * Now if the merchant adds another apple item, theoretically speaking, the invoice should contain only one
     * item (apple) with the quantity updated to 2.
     * But, if we try to create another apple item, lets say, InvoiceItem item_2 = new InvoiceItem("apple", 1);
     * even though its the sample "apple" item, the invoice would treat it as a separate item and would now
     * have 2 items, both apple and both with quantity 1.
     * <p/>
     * In this sample app, we implemented the former and keep track of the same item object to update the quantity in
     * the invoice.
     */
    private void initStoreItems() {
        // Put all the store items in a hash map for easy access later.
        // App can implement this in any way of their choosing.
        mStoreItems = new HashMap<String, InvoiceItem>(4);

        mStoreItems.put("Apple", DomainFactory.newInvoiceItem(("Apple"), "1", new BigDecimal("4.12")));
        mStoreItems.put("Banana", DomainFactory.newInvoiceItem(("Banana"), "2", new BigDecimal("1.32")));
        mStoreItems.put("Orange", DomainFactory.newInvoiceItem(("Orange"), "3", new BigDecimal("5")));
        mStoreItems.put("Strawberry", DomainFactory.newInvoiceItem(("Strawberry"), "4", new BigDecimal("3.98")));
    }

    private void initListAdapter() {
        // Setting up the adapter to display all the items in our invoice.
        mAdapter = new InvoiceItemAdapter(ItemizedActivity.this);
        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mAdapter);

        mHeaderLayout.setVisibility(View.VISIBLE);
        mPurchaseButton.setVisibility(View.VISIBLE);
        mInvoice = PayPalHereSDK.getTransactionManager().beginPayment();
    }

    private void clearListAdapter() {
        if (null != mAdapter && null != mListView) {
            mAdapter.clearAllItems();
        }
        mHeaderLayout.setVisibility(View.GONE);
        mPurchaseButton.setVisibility(View.GONE);
        mInvoice = null;
    }

    /**
     * Method to add items into the invoice based on the fruit type.
     *
     * @param fe
     */
    private void addFruit(FruitTypeEnum fe) {
        String name = "";
        switch (fe) {
            case APPLE:
                name = "Apple";
                break;
            case BANANA:
                name = "Banana";
                break;
            case STRAWBERRY:
                name = "Strawberry";
                break;
            case ORANGE:
                name = "Orange";
                break;
        }
        // Get the InvoiceItem object for the fruit.
        InvoiceItem i = mStoreItems.get(name);

        if (null == mInvoice) {
            initListAdapter();
        }

        mInvoice.addItem(i, BigDecimal.ONE);
        mAdapter.addItem(i);
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        Log.d(LOG, "on Resume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(LOG, "onPause");
        super.onPause();
    }
}
