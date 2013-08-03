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
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.PeripheralsListener;
import com.paypal.merchant.sdk.domain.DomainFactory;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.merchant.sdk.domain.shopping.CartItem;
import com.paypal.merchant.sdk.domain.shopping.ShoppingCart;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.adapter.ShoppingCartListViewAdapter;
import com.paypal.sampleapp.util.CommonUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * An activity that is used to create an itemized list.
 * <p/>
 * In this activity, we also implement the peripheral listener so that we can listen to card swipes.
 */
public class ItemizedActivity extends MyActivity implements PeripheralsListener {

    private static final String LOG = "PayPalHere.ItemizedActivity";
    private ShoppingCart mShoppingCart = null;
    private ListView mListView;
    private ShoppingCartListViewAdapter mAdapter;
    private Map<String, CartItem> mStoreItems;

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

                if (mShoppingCart == null || mShoppingCart.getCartItems().size() <= 0) {
                    CommonUtils.createToastMessage(ItemizedActivity.this, "Cannot proceed with an empty shopping cart" +
                            ".");
                    return;
                }
                // Insert the shopping cart containing the items back the SDK.
                // We need to do this coz the shopping cart that we get while calling the beginPayment would return
                // us a copy of the shopping cart that is created in the SDK.
                // Hence, after we have filled in our shopping cart with some items,
                // we need to manually set the shopping cart back into the SDK.
                PayPalHereSDK.getTransactionManager().setShoppingCart(mShoppingCart);

                // Saving the shopping cart at another place as well in case of a transaction failure,
                // where the shopping cart from the transaction manager is removed.
                // NOTE: the reasoning for this could be found during the finalizePayment method call.
                // Please refer to those comments as well.
                saveShoppingCart(mShoppingCart);


                Intent intent = new Intent(ItemizedActivity.this, PaymentTypeTabActivity.class);
                startActivity(intent);
            }
        });

        // Registering the peripherals listener with the SDK that would help us listen to card swipes.
        PayPalHereSDK.getPeripheralsManager().registerPeripheralsListener(this);

    }

    /**
     * The sample app creates a hash map of all the items in the store (in this case, we only have 4).
     * We need this because the shopping cart updates the quantity of items in case of addition or removal of items
     * and it requires the SAME item object to perform this action.
     * <p/>
     * For example:
     * Let say we have an item CartItem ci_1 = new CartItem("apple",1) and put this in the shopping cart:
     * shoppingCart.add(ci_2);
     * Now if the merchant adds another apple item, theoretically speaking, the shopping cart should contain only one
     * item (apple) with the quantity updated to 2.
     * But, if we try to create another apple item, lets say, CartItem ci_2 = new CartItem("apple", 1);
     * even though its the sample "apple" item, the shopping cart would treat it as a separate item and would now
     * have 2 items, both apple and both with quantity 1.
     * <p/>
     * In this sample app, we implemented the former and keep track of the same item object to update the quantity in
     * the shoppping cart.
     */
    private void initStoreItems() {
        // Put all the store items in a hash map for easy access later.
        // App can implement this in any way of their choosing.
        mStoreItems = new HashMap<String, CartItem>(4);

        mStoreItems.put("Apple", DomainFactory.newCartItem(("Apple"), new BigDecimal(4.12)));
        mStoreItems.put("Banana", DomainFactory.newCartItem(("Banana"), new BigDecimal(1.32)));
        mStoreItems.put("Orange", DomainFactory.newCartItem(("Orange"), new BigDecimal(5)));
        mStoreItems.put("Strawberry", DomainFactory.newCartItem(("Strawberry"), new BigDecimal(3.98)));
    }

    /**
     * This method is meant to init the SDK after the customer tries to add the first item.
     */
    private void initShoppingCart() {
        // For the itemized payment, call the beginPayment() method to
        // indicate the SDK of an itemized payment.
        // NOTE: Once the beginPayment method is invoked, the SDK would listen for any card swipes and hence, the
        // customer is allowed to swipe in their card at point in time.
        // This card data would be held by the SDK and would be used during the payment.
        // This feature is mainly aimed to offer flexibility as to when the card could be swiped.
        // When a card swipe is detected, a SecureCreditCard object is also returned back by the SDK,
        // which the apps can choose to store.

        // Once the itemized payment is initialized in the SDK, get the Shopping
        // cart obj created by the SDK and add items to the same.

        mShoppingCart = PayPalHereSDK.getTransactionManager().beginPayment();

        // Setting up the adapter to display all the items in our shopping cart.
        mAdapter = new ShoppingCartListViewAdapter(ItemizedActivity.this, mShoppingCart);
        mListView = (ListView) findViewById(R.id.item_list);
        mListView.setAdapter(mAdapter);
    }

    /**
     * Method to add items into the shopping cart based on the fruit type.
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
        // Get the CartItem object for the fruit.
        CartItem ci = mStoreItems.get(name);

        if (mShoppingCart == null)
            initShoppingCart();

        // Add the item into the shopping cart, with the quantity 1.
        // NOTE: the quantity would keep getting updated as we keep addding or removing items.
        // In order to remove an item, use : mShoppingCart.addItem(ci, new Long(-1));
        mShoppingCart.addItem(ci, new Long(1));
        // To update the UI with the new list of items in the shopping cart.
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
     * Below are the implementation for the PeripheralsListener.
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
        // So, when we do a finalizePayment in the next screen to take the payment and complete the transaction,
        // this card data (that is held by the SDK) is used and charged against.
        CommonUtils.createToastMessage(ItemizedActivity.this, "Card read successful!!!");
    }

    @Override
    public void onCardReadFailed(PPError<CardErrors> error) {
        // Display a message stating that the card swipe had a failure.
        CommonUtils.createToastMessage(ItemizedActivity.this, "Card read failed");
    }

    @Override
    public void onPeripheralEvent(PeripheralEvent e) {

    }

    @Override
    public void onResume() {
        Log.d(LOG, "on Resume");
        super.onResume();
        // Register for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getPeripheralsManager().registerPeripheralsListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister for payment and peripheral (Bond, triangle, etc) events.
        PayPalHereSDK.getPeripheralsManager().unregisterPeripheralsListener(this);
    }

    private enum FruitTypeEnum {
        APPLE, BANANA, ORANGE, STRAWBERRY
    }
}
