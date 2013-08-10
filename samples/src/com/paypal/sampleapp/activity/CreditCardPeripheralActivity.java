/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */

package com.paypal.sampleapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.PeripheralsListener;
import com.paypal.merchant.sdk.TransactionListener;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.TransactionManager.CancelPaymentErrors;
import com.paypal.merchant.sdk.TransactionManager.PaymentErrors;
import com.paypal.merchant.sdk.TransactionManager.PaymentResponse;
import com.paypal.merchant.sdk.TransactionManager.PaymentType;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SecureCreditCard;
import com.paypal.merchant.sdk.domain.TransactionRecord;
import com.paypal.merchant.sdk.domain.shopping.ShoppingCart;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.adapter.CreditCardListViewAdapter;
import com.paypal.sampleapp.util.CommonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * This activity is meant to complete the transaction either by connecting to a
 * PayPalHere card reader or the PayPalHere Bond device to take in the credit
 * card information.
 * <p/>
 * We would need to implement the TransactionListener to listen to payment/transaction related actions such as
 * payment complete, error in transaction etc.
 * We would also need to implement the PeripheralsListener to listen to card swipes within this activity.
 */
public class CreditCardPeripheralActivity extends MyActivity implements TransactionListener,
        PeripheralsListener {

    private static final String LOG = "PayPalHere.CreditCardPeripheral";
    private static final int SIGNATURE_ACTIVITY_REQ_CODE = 654;
    /**
     * Implementing a PaymentResponseHandler to handle the response status of a
     * transaction.
     */
    DefaultResponseHandler<PaymentResponse, PPError<TransactionManager.PaymentErrors>> mPaymentResponseHandler = new
            DefaultResponseHandler<PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                // If the transaction went through successfully.
                public void onSuccess(PaymentResponse response) {

                    paymentCompleted(true);

                    // Displaying a success message on the UI.
                    updateUIForPurchaseSuccess(response);
                    // We can now enable the refund option as well.
                    mRefundButton.setVisibility(View.VISIBLE);
                    // We can now take another payment.
                    mAnotherTransButton.setVisibility(View.VISIBLE);
                    // Hide the cancel button.
                    mCancelButton.setVisibility(View.GONE);

                }

                // If an error occurred while completing the transaction.
                @Override
                public void onError(PPError<PaymentErrors> e) {

                    paymentCompleted(false);

                    // Display the error message onto the UI.
                    updateUIForPurchaseError(e);
                    mAnotherTransButton.setVisibility(View.VISIBLE);
                    mReuseShoppingCartButton.setVisibility(View.VISIBLE);

                }
            };
    /**
     * Implementing a DefaultResponseHandler to handle the response status of a refund operation.
     */
    DefaultResponseHandler<TransactionRecord, PPError<PPError.BasicErrors>> mDefaultResponseHandler = new
            DefaultResponseHandler<TransactionRecord, PPError<PPError.BasicErrors>>() {
                // If the refund went through successfully.
                @Override
                public void onSuccess(TransactionRecord record) {
                    displayPaymentState("Refund Successful! Transaction Id: " + record.getTransactionId());
                    // Disabling the refund button once the refund was complete.
                    mRefundButton.setVisibility(View.GONE);
                }

                // If there any issue during the refund operation.
                @Override
                public void onError(PPError<PPError.BasicErrors> e) {
                    displayPaymentState("Refund Failed! " + e.getDetailedMessage());
                }
            };
    private Button mAnotherTransButton;
    private Button mReuseShoppingCartButton;
    private Button mRefundButton;
    private Button mSignButton;
    private Button mCancelButton;
    private Button mAddTipButton;
    private Button mPurchaseButton;
    private TransactionRecord mTransactionRecord;
    private BigDecimal mAmount;
    private CreditCardListViewAdapter mCreditCardAdapter;
    private List<SecureCreditCard> mCreditCardList;
    private ListView mCreditCardListView;
    private LinearLayout mCreditCardLayout;

    /**
     * Initialize the elements in the layout.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_credit_card_peripheral);
        // List view that shows a list of credit cards that have been swiped within this activity so that the
        // customer/merchant can choose any one of them to take a payment.
        mCreditCardListView = (ListView) findViewById(R.id.credit_card_list);
        // List of swiped credit cards.
        mCreditCardList = new ArrayList<SecureCreditCard>();
        // A list view adapter to show the list of swiped credit cards on the UI.
        mCreditCardAdapter = new CreditCardListViewAdapter(CreditCardPeripheralActivity.this, mCreditCardList);
        mCreditCardListView.setAdapter(mCreditCardAdapter);
        mCreditCardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Take payment from the credit card that was selected.
                takePaymentViaSelectedCard((SecureCreditCard) mCreditCardList.get(position));
            }
        });

        // Initially, hiding the view that shows the list of swiped in order to save real estate on the screen.
        mCreditCardLayout = (LinearLayout) findViewById(R.id.credit_cards_layout);
        mCreditCardLayout.setVisibility(View.GONE);

        // Find and set the button when the user clicks on the purchase button.
        mPurchaseButton = (Button) findViewById(R.id.purchase_button);
        mPurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peripheralPurchaseClicked();
            }
        });

        // Find and set the button when the user has provided the signature.
        mSignButton = (Button) findViewById(R.id.sign_button);
        mSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignClicked();
            }
        });

        // Find and set the button when the user tries to cancel a payment.
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelPaymentClicked();
            }
        });

        mAddTipButton = (Button) findViewById(R.id.add_tip_button);
        mAddTipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openTipDialog();
            }
        });

        // Find and set the button when the current transaction is complete and
        // the user wants to perform another.
        mAnotherTransButton = (Button) findViewById(R.id.another_transaction_button);
        mAnotherTransButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                performAnotherTransaction();
            }
        });

        // Find and set the button when the transaction is complete and a refund is asked for.
        mRefundButton = (Button) findViewById(R.id.refund_button);
        mRefundButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doRefund();
            }
        });

        mReuseShoppingCartButton = (Button) findViewById(R.id.reuse_cart_button);
        mReuseShoppingCartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reInstateShoppingCart();
                showButtons(true);
            }
        });
        mReuseShoppingCartButton.setVisibility(View.GONE);

        // Setting the "another transaction" button to invisible initially.
        mAnotherTransButton.setVisibility(View.GONE);

        // Hide the refund button at the start. Show the same once the transaction is completed.
        mRefundButton.setVisibility(View.GONE);

        // Disable the take signature button until we record a card swipe
        mSignButton.setEnabled(false);

        // Disable the purchase button until we record a card swipe
        mPurchaseButton.setEnabled(false);

        // Check if the SDK has some card swipe data that could've been obtained from the previous screen/activity.
        if (PayPalHereSDK.getTransactionManager().hasCardData()) {
            // If there is, then, inform the merchant to simply complete the transaction by clicking on the
            // "purchase" button.
            displayPaymentState("Click purchase to complete the transaction.");

            // Now since we have the card data, show the signature button and the purchase button
            mSignButton.setEnabled(true);
            mPurchaseButton.setEnabled(true);
        } else {
            // Check if any card reader device is connected.
            checkForPeripheralDevices();
        }


        // Display the amount such as the grand total, tax and tip on the screen.
        updateUIAmount();

        paymentCompleted(false);

    }

    private void openTipDialog() {
        AlertDialog.Builder tipDialog = new AlertDialog.Builder(this);
        tipDialog.setTitle("Tip amount");
        final EditText vi = new EditText(this);
        vi.setHint("$0");
        vi.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tipDialog.setView(vi);
        tipDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tip = vi.getText().toString();
                if (CommonUtils.isNullOrEmpty(tip)) {
                    sendInvalidTipMessage();
                    return;
                }
                BigDecimal tipVal = BigDecimal.ZERO;
                try {
                    tipVal = new BigDecimal(tip);

                } catch(Exception e) {
                    sendInvalidTipMessage();
                    return;
                }

                if (tipVal.doubleValue() <= BigDecimal.ZERO.doubleValue()) {
                    sendInvalidTipMessage();
                    return;
                }
                addTip(tipVal);
            }
        });

        tipDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Just dismiss the dialog
            }
        });

        tipDialog.show();
    }

    private void sendInvalidTipMessage() {
        CommonUtils.createToastMessage(CreditCardPeripheralActivity.this, "Invalid tip!");
    }

    private void showButtons(boolean show) {
        if (show) {
            mAddTipButton.setVisibility(View.VISIBLE);
            mCancelButton.setVisibility(View.VISIBLE);
            mPurchaseButton.setVisibility(View.VISIBLE);
            mSignButton.setVisibility(View.VISIBLE);
            mCreditCardLayout.setVisibility(View.VISIBLE);
        } else {
            mAddTipButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);
            mPurchaseButton.setVisibility(View.GONE);
            mSignButton.setVisibility(View.GONE);
            mCreditCardLayout.setVisibility(View.GONE);
        }

    }


    /**
     * This method updates the UI screen in case of any errors during the
     * transaction.
     *
     * @param e
     */
    private void updateUIForPurchaseError(PPError<PaymentErrors> e) {
        PaymentErrors error_type = e.getErrorCode();

        if (PaymentErrors.PaymentDeclined == error_type) {
            displayPaymentState("Payment declined!  Payment cycle complete.  Please start again");
        } else if (PaymentErrors.NetworkTimeout == error_type) {
            displayPaymentState("Payment timed out at network level.");
        } else if (PaymentErrors.NoDeviceForCardPresentPayment == error_type) {
            displayPaymentState("No Device connected.  Connect your device.");
        } else if (PaymentErrors.NoCardDataPresent == error_type) {
            displayPaymentState("We can't take card payment ... no card has been scanned.");
        } else if (PaymentErrors.TransactionCanceled == error_type) {
            displayPaymentState("Payment Canceled after takePayment.  No more payment");
        } else if (PaymentErrors.TimeoutWaitingForSwipe == error_type) {
            displayPaymentState("Payment Canceled.  Expecting card swipe but no swipe ever happened");
        } else if (PaymentErrors.BadConfiguration == error_type) {
            displayPaymentState("Payment Canceled.  Incorrect Usage / Bad Configuration " + e.getDetailedMessage());
        } else if (PaymentErrors.EmptyShoppingCart == error_type) {
            displayPaymentState("You've got an empty shopping cart, or a cart with zero value.  Can't process payment");
        } else {
            displayPaymentState("Unhandled error: " + e.getDetailedMessage());
        }
    }

    /**
     * This method updates the UI screen in case of a successful transaction.
     * We display transaction Id on the UI.
     *
     * @param response
     */
    private void updateUIForPurchaseSuccess(PaymentResponse response) {
        // Setting these values for refund related operations.
        mTransactionRecord = response.getTransactionRecord();
        mAmount = response.getTransactionRecord().getShoppingCart().getGrandTotal();
        displayPaymentState("Payment completed successfully!  TransactionId: " + response.getTransactionRecord()
                .getTransactionId());
    }

    /**
     * Method to do the refund.
     */
    private void doRefund() {
        // Check for a previous transaction (for which a refund is needed).
        // If there is one, then, make sure the transaction record and the amount are recorded.
        if (mAmount == null || mTransactionRecord == null) {
            CommonUtils.createToastMessage(CreditCardPeripheralActivity.this, "Invalid refund operation!");
            return;
        }
        displayPaymentState("Performing Refund...");
        // Call the refund API within the SDK to perform the refund.
        PayPalHereSDK.getTransactionManager().doRefund(mTransactionRecord, mAmount, mDefaultResponseHandler);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGNATURE_ACTIVITY_REQ_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    // provide the signature bitmap to the transaction manager to
                    // complete the transaction.
                    PayPalHereSDK.getTransactionManager().provideSignature(MyActivity.getBitmap());
                    Log.d(LOG, "signature received");
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for payment and peripheral (Bond, triangle, etc) events.
        registerTransactionAndPeripheralsListener(true);

        if(PayPalHereSDK.getTransactionManager().isProcessingAPayment() || isPaymentCompleted())
            showButtons(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister for payment and peripheral (Bond, triangle, etc) events.
        registerTransactionAndPeripheralsListener(false);
    }

    /**
     * This method registers or unregisters the PayPalHereSDK's
     * TransactionManager and the PeripheralManager to the app implemented
     * TransactionListerer and the PeripheralListers.
     */
    private void registerTransactionAndPeripheralsListener(boolean isRegister) {
        if (PayPalHereSDK.getTransactionManager() == null || PayPalHereSDK.getPeripheralsManager() == null)
            return;

        if (isRegister) {
            Log.d(LOG, "registered");

            PayPalHereSDK.getTransactionManager().registerListener(this);
            PayPalHereSDK.getPeripheralsManager().registerPeripheralsListener(this);

        } else {
            Log.d(LOG, "un-registered");

            PayPalHereSDK.getTransactionManager().unregisterListener(this);
            PayPalHereSDK.getPeripheralsManager().unregisterPeripheralsListener(this);

        }

    }

    /**
     * This method is meant to call the SDK to take a payment with a selected credit card.
     *
     * @param selectedCard
     */
    private void takePaymentViaSelectedCard(SecureCreditCard selectedCard) {

        // Check if a transaction or payment is currently under process. If so,
        // do not allow for another transaction. Simply return.
        if (PayPalHereSDK.getTransactionManager().isProcessingAPayment()) {
            CommonUtils.createToastMessage(CreditCardPeripheralActivity.this,
                    "Please wait until the current purchase" +
                            " is completed.");
            return;
        }
        // Hide the transaction related buttons.
        showButtons(false);
        mAnotherTransButton.setVisibility(View.GONE);
        mReuseShoppingCartButton.setVisibility(View.GONE);

        displayPaymentState("Taking payment with Card ... ");


        // Call the takePayment method to charge the credit card.
        // The SDK would automatically decide whether to make a fixed price
        // transaction or an itemized transaction based on the
        // "BeginPayment" type selected by the app.

        // **NOTE**: The transaction state i.e., the shopping cart, the transaction extras,
        // previously read credit cards etc would be kept intact only between the begin - finalize payments. If the
        // payment goes through successfully or if it returns back with a failure,
        // all the above mentioned objects are removed and the app would need to call beginPayment once again to
        // re-init, set the shopping cart back (which they would be holding onto) and try again.

        PayPalHereSDK.getTransactionManager().finalizePayment(selectedCard, mPaymentResponseHandler);
    }

    /**
     * This method is meant to take a payment.
     * The card data that is used to take the payment is the last card that was swiped and is held within the SDK.
     */
    private void peripheralPurchaseClicked() {

        // Check if a transaction or payment is currently under process. If so,
        // do not allow for another transaction. Simply return.
        if (PayPalHereSDK.getTransactionManager().isProcessingAPayment()) {
            CommonUtils.createToastMessage(CreditCardPeripheralActivity.this,
                    "Please wait until the current purchase" +
                            " is completed.");
            return;
        }
        showButtons(false);
        mAnotherTransButton.setVisibility(View.GONE);
        mReuseShoppingCartButton.setVisibility(View.GONE);


        displayPaymentState("Taking payment with Card ... ");

        // Call the takePayment method to charge the credit card.
        // The SDK would automatically decide whether to make a fixed price
        // transaction or an itemized transaction based on the
        // "BeginTransaction" type selected by the app.

        // **NOTE**: The transaction state i.e., the shopping cart, the transaction extras,
        // previously read credit cards etc would be kept intact only between the begin - finalize payments. If the
        // payment goes through successfully or if it returns back with a failure,
        // all the above mentioned objects are removed and the app would need to call beginPayment once again to
        // re-init, set the shopping cart back (which they would be holding onto) and try again.
        PayPalHereSDK.getTransactionManager().finalizePayment(PaymentType.CardReader, mPaymentResponseHandler);

    }

    /**
     * This method is called when the merchant would like to take the signature of the customer.
     */
    private void onSignClicked() {
        // We display a new activity that takes the customer's signature and the bitmap is stored within the
        // "MyActivity" class for future reference within this activity to complete the transaction.
        Intent intent = new Intent(CreditCardPeripheralActivity.this, SignatureActivity.class);
        startActivityForResult(intent, SIGNATURE_ACTIVITY_REQ_CODE);

        Log.d(LOG, "signature required");
    }

    /**
     * This method is invoked to cancel an ongoing payment, which would essentially clear out/remove the shopping cart
     * and any card data from previous swipes within the SDK and would also stop scanning for any cards.
     * <p/>
     * In order to take another/fresh payment, the app would need to invoke "beginPayment" once again to create a new
     * and empty shopping cart.
     */
    private void onCancelPaymentClicked() {
        Log.d(LOG, "cancelling payment");

        displayPaymentState("Cancelling Payment...");

        // Meant to cancel any on-going transaction.
        // NOTE: This method should NOT be called once the finalizePayment has been invoked as there isnt a way to
        // cancel an on-going payment with the back end service. This finalizePayment should be invoked once we know
        // the customer is willing to pay the said amount and if, for some reason,
        // after the finalize payment is called, if teh customer wants to cancel the payment,
        // they would need to ask for a refund.
        PPError<CancelPaymentErrors> error = PayPalHereSDK.getTransactionManager().cancelPayment();

        if (CancelPaymentErrors.Success == error.getErrorCode()) {
            CommonUtils.createToastMessage(CreditCardPeripheralActivity.this, "Payment cancelled. Clearing shopping " +
                    "cart.");
            // Go back to the billing activity and perform another transaction.
            performAnotherTransaction();
        } else
            displayPaymentState("Invalid operation. Cannot cancel payment now. ");
    }

    /**
     * This method is called in order to perform another transaction after the completion of the current one.
     */
    private void performAnotherTransaction() {
        Log.d(LOG, "Performing another transaction");
        Intent intent = new Intent(CreditCardPeripheralActivity.this, BillingTypeTabActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * TransactionListener
     * <p/>
     * The TransactionListener interface. It lets you know when the transaction
     * manager moves through the steps of taking a payment. The phases are
     * NOT_ACTING_ON_PAYMENT ITEMIZING AUTHORIZING PROCESSING_PAYMENT
     * <p/>
     * When not doing an itemized payment (for example, when taking a payment
     * for a fixed amount) we'll move to AUTHORIZING (working with the card
     * reader) then to the PROCESSING_PAYMENT phase (card/pin info gathering is
     * done and we're now communicating with the backend to actually collect
     * your money.
     *
     */

    /**
     * This method is invoked by the SDK when the state of the transaction changes.
     *
     * @param e a PaymentEvent object the contains information about the event.
     */
    public void onPaymentEvent(PaymentEvent e) {
        PaymentEventType type = e.getEventType();
        Log.d(LOG, type.name());

        if (type == PaymentEventType.ProcessingPayment) {
            displayPaymentState("Processing Payment...");
        } else if (type == PaymentEventType.GettingAuthorization) {
            displayPaymentState("Scanning for any card data...");
        }
    }

    /**
     * PeripheralsListener
     * <p/>
     * The PeripheralsListener interface methods.
     * <p/>
     * onPaymentReaderConnected Used to detect when a reader is connected
     * onPaymentReaderDisconnected Used to detect when a reader is disconnected
     * onPaymentCardDetected Used when a swipe (or card plug in) happens
     * onCardReadFailed Used to indicate bad swipes or other errors with reading
     * the card onPeripheralEvent Signals when a Swipe, Pin, or a Signature is
     * now required, etc.
     */

    /**
     * This method is invoked by the SDK when it detects a reader being connected to the device.
     *
     * @param readerType; indicates whether this is a magnetic stripe reading device or a more advanced device
     *                    capable of reading cards with a Chip & PIN.
     * @param transport;  indicates how the reader is connected to this device (bluetooth, audio jack etc).
     */
    public void onPaymentReaderConnected(ReaderTypes readerType, ReaderConnectionTypes transport) {
        displayPaymentState("Reader is connected! Please swipe your card.");
        Log.d(LOG, "reader connected");
    }

    /**
     * This method is invoked by the SDK when it detects a reader is disconnected.
     *
     * @param readerType
     */
    public void onPaymentReaderDisconnected(ReaderTypes readerType) {
        displayPaymentState("Reader is disconnected!");
        Log.d(LOG, "reader disconnected");
    }

    /**
     * This method is invoked by the SDK when a detects a card swipe.
     *
     * @param paymentCard
     */
    public void onCardReadSuccess(SecureCreditCard paymentCard) {
        Log.d(LOG, "card read success");
        // The SDK passes the card data back to the app, which could be stored by the application in case of multiple
        // card swipes.
        displayPaymentState("Card Read Successful! Click Purchase to complete transaction.");
        // adding the swiped card data into a list.
        mCreditCardList.add(paymentCard);
        // Update the UI showing the list of swiped cards.
        mCreditCardAdapter.notifyDataSetChanged();
        // Make this list visible on the UI screen.
        mCreditCardLayout.setVisibility(View.VISIBLE);
        // Show the signature button to take the customer's signature.
        mSignButton.setEnabled(true);
        // Show the purchase button as well.
        mPurchaseButton.setEnabled(true);
    }

    /**
     * This method is invoked by the SDK in case of failure while trying to read the card data.
     *
     * @param error
     */
    public void onCardReadFailed(PPError<CardErrors> error) {
        CommonUtils.createToastMessage(CreditCardPeripheralActivity.this, "Bad swipe! Try Again.");
        Log.d(LOG, "card read failed");
    }

    /**
     * This method is invoked by the SDK when certain to indicate certain card read related events.
     *
     * @param e
     */
    @Override
    public void onPeripheralEvent(PeripheralEvent e) {

        PeripheralEvents type = e.getEventType();
        Log.d(LOG, type.name());

        if (type == PeripheralEvents.BadSwipe)
            CommonUtils.createToastMessage(CreditCardPeripheralActivity.this, "Bad swipe! Try Again.");
        else if (type == PeripheralEvents.CardBlocked)
            displayPaymentState("Card blocked!  Please use another card.");
        else if (type == PeripheralEvents.CardInvalid)
            displayPaymentState("Invalid Card!  Please use another.");

    }

    /**
     * This method is meant to display the given text on the UI screen.
     *
     * @param state
     */
    private void displayPaymentState(String state) {
        Log.d(LOG, "state: " + state);
        TextView tv = (TextView) findViewById(R.id.purchase_status);
        tv.setText(state);
    }

    /**
     * This method is meant to check whether any card reading device is connected to the device.
     *
     * @return
     */
    private void checkForPeripheralDevices() {
        // Get the list of devices/readers that the device is connected to.
        ArrayList<ReaderTypes> readerList = PayPalHereSDK.getPeripheralsManager().availableReaders();
        if (readerList == null) {
            displayPaymentState("Please connect a card reader!.");
            Log.e(LOG, "no devices connected!");

        } else if (readerList.contains(ReaderTypes.MagneticCardReader)) {
            displayPaymentState("Reader is connected! Please swipe your card.");
            Log.d(LOG, "Card reader connected!");

        }

    }

    /**
     * This method is invoked to set a tip amount on the shopping cart.
     */
    private void addTip(BigDecimal tip) {
        ShoppingCart cart = PayPalHereSDK.getTransactionManager().getShoppingCart();
        if (cart == null) {
            Log.d(LOG, "shopping cart is null/empty");
            CommonUtils.createToastMessage(CreditCardPeripheralActivity.this, "No shopping cart found, " +
                    "please begin a new transaction.");
            return;
        }
        // We are setting a constant $4 amount as tip into the shopping cart.
        cart.setTipAmount(tip);
        PayPalHereSDK.getTransactionManager().setShoppingCart(cart);
        // Update the UI to reflect the recently added tip amount.
        updateUIAmount();
    }

    /**
     * This method is meant to display the various amounts associated with the shopping cart.
     */
    private void updateUIAmount() {
        ShoppingCart sc = PayPalHereSDK.getTransactionManager().getShoppingCart();
        if (sc == null)
            return;

        ((TextView) findViewById(R.id.subTotal)).setText("$ " + sc
                .getSubTotal().setScale(2, RoundingMode.CEILING).toString());
        ((TextView) findViewById(R.id.taxAmount)).setText("$ " + sc
                .getTaxAmount().setScale(2, RoundingMode.CEILING).toString());
        ((TextView) findViewById(R.id.tipAmount)).setText("$ " + sc
                .getTipAmount().setScale(2, RoundingMode.CEILING).toString());
        ((TextView) findViewById(R.id.totalAmount)).setText("$ " + sc
                .getGrandTotal().setScale(2, RoundingMode.CEILING).toString());
    }

    /**
     * This method is needed to make sure nothing is invoked/called when the
     * orientation of the phone is changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

}
