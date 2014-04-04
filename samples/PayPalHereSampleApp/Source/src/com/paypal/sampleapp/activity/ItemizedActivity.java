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

import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.domain.ChipAndPinDecisionEvent;
import com.paypal.merchant.sdk.domain.DomainFactory;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.InvoiceItem;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.adapter.InvoiceItemListViewAdapter;
import com.paypal.sampleapp.util.CommonUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity that is used to create an itemized list.
 * <p/>
 * In this activity, we also implement the peripheral listener so that we can listen to card swipes.
 */
public class ItemizedActivity extends MyActivity implements CardReaderListener {

    private static final String LOG = "PayPalHere.ItemizedActivity";
    private static final int REQ_CODE = 4567;
    private Invoice mInvoice = null;
    private ListView mListView;
    private InvoiceItemListViewAdapter mAdapter;
    private Map<String, InvoiceItem> mStoreItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting the layout for this activity.
        setContentView(R.layout.activity_itemized_tab_type);

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


        Button b = (Button) findViewById(R.id.itemized_checkout_button);
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mInvoice == null || mInvoice.getItems().size() <= 0) {
                    CommonUtils.createToastMessage(ItemizedActivity.this, "Cannot proceed with an empty items list.");
                    return;
                }

                // Set the cashier id (if present) within the invoice to indicate which cashier within the store
                // created this invoice.
                mInvoice.setCashierId(getCashierId());
                // Set the partner attribution (BN) code (if present) also within the invoice to indicate which
                // partner it came from.
                mInvoice.setReferrerCode(getBNCode());
                // Insert the invoice containing the items back the SDK.
                // We need to do this coz the invoice object that we get while calling the beginPayment would return
                // us a copy of the invoice that is created in the SDK.
                // Hence, after we have filled in our invoice with some items,
                // we need to manually set it back into the SDK.
                PayPalHereSDK.getTransactionManager().setActiveInvoice(mInvoice);

                Intent intent = new Intent(ItemizedActivity.this, PaymentTypeTabActivity.class);
                startActivityForResult(intent, REQ_CODE);
            }
        });

        // Creating a new empty invoice.
        mInvoice = DomainFactory.newEmptyInvoice();

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

    /**
     * This method is meant to init the SDK after the customer tries to add the first item.
     */
    private void initInvoice() {
        // For the itemized payment, call the beginPayment() method to
        // indicate the SDK of an itemized payment.
        // NOTE: Once the beginPayment method is invoked, the SDK would listen for any card swipes and hence, the
        // customer is allowed to swipe in their card at point in time.
        // This card data would be held by the SDK and would be used during the payment.
        // This feature is mainly aimed to offer flexibility as to when the card could be swiped.
        // When a card swipe is detected, a SecureCreditCard object is also returned back by the SDK,
        // which the apps can choose to store.

        // Once the itemized payment is initialized in the SDK.

        mInvoice = PayPalHereSDK.getTransactionManager().beginPayment();
    }

    private void initListAdapter() {
        // Setting up the adapter to display all the items in our invoice.
        mAdapter = new InvoiceItemListViewAdapter(ItemizedActivity.this, mInvoice);
        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mAdapter);
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

        if (isInvoiceEmpty()) {
            initInvoice();
            initListAdapter();
        }

        // Add the item into the invoice, with the quantity 1.
        // NOTE: the quantity would get updated as we keep adding or removing items.
        // In order to remove an item, use : mInvoice.addItem(i, new Long(-1));
        mInvoice.addItem(i, BigDecimal.ONE);
        // To update the UI with the new list of items in the invoice.
        mAdapter.notifyDataSetChanged();
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    /**
     * Below are the implementation for the CardReaderListener.
     */

    @Override
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {

    }

    @Override
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {

    }

    @Override
    public void onCardReadSuccess(SecureCreditCard paymentCard) {
        // Display a message stating that the card swipe was successful.
        // NOTE: Once the card has been successfully swiped or read, the SDK holds on to it as well as returns the
        // same in this method (paymentCard) for the app to hold the same if they want or need to.
        // So, when we do a processPayment in the next screen to take the payment and complete the transaction,
        // this card data (that is held by the SDK) is used and charged against.
        CommonUtils.createToastMessage(ItemizedActivity.this, "Card read successful!!!");
    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> error) {
        // Display a message stating that the card swipe had a failure.
        CommonUtils.createToastMessage(ItemizedActivity.this, "Card read failed");
    }

    @Override
    public void onCardReaderEvent(PPError<CardReaderEvents> peripheralEventsPPError) {

    }

    @Override
    public void onSelectPaymentDecision(List<ChipAndPinDecisionEvent> eventList) {

    }

    @Override
    public void onInvalidListeningPort() {
        CommonUtils.createToastMessage(this, "No valid listening port.");
    }

    @Override
    public void onResume() {
        Log.d(LOG, "on Resume");
        super.onResume();
        // Register for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getCardReaderManager().registerCardReaderListener(this);

        // If the back button is pressed, we are handling in 2 scenarios:
        // 1. If the invoice has some items added to it and then, if we head to the payment page and for some
        // reason, if we decide to come back to this screen to add more items, we get back the already available
        // invoice.
        // 2. If the transaction is complete for a invoice and then, if the user hits the back button and we
        // land on this screen again, since the transaction is complete, no invoice would be available in the
        // transaction manager and hence, we create a new invoice.
        mInvoice = PayPalHereSDK.getTransactionManager().getActiveInvoice();
        if (isInvoiceEmpty()) {
            mInvoice = DomainFactory.newEmptyInvoice();
        }
        initListAdapter();

    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getCardReaderManager().unregisterCardReaderListener(this);
    }

    private boolean isInvoiceEmpty() {
        return (mInvoice == null || mInvoice.getItems().size() <= 0);
    }

    private enum FruitTypeEnum {
        APPLE, BANANA, ORANGE, STRAWBERRY
    }
}
