package com.paypal.heresdk.sampleapp.sdk;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.paypal.merchant.sdk.AuthenticationListener;
import com.paypal.merchant.sdk.CardReaderConnectionListener;
import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionController;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Invoice;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.merchant.sdk.domain.SDKReceiptScreenOptions;
import com.paypal.merchant.sdk.domain.SDKSignatureScreenOptions;
import com.paypal.merchant.sdk.domain.TransactionRecord;

import java.math.BigDecimal;
import java.util.List;

public class PayPalHereSDKWrapper implements CardReaderConnectionListener,
                                             AuthenticationListener,
                                             TransactionController{
    private static String LOG_TAG = PayPalHereSDKWrapper.class.getSimpleName();
    private static PayPalHereSDKWrapper mInstance = null;
    private PayPalHereSDKWrapperCallbacks mListener = null;
    private Activity mCurrentActivity = null;

    public static PayPalHereSDKWrapper getInstance(){
        if(null == mInstance){
            mInstance = new PayPalHereSDKWrapper();
        }
        return mInstance;
    }

    private PayPalHereSDKWrapper(){

    }

    public void setListener(Activity currentActivity, PayPalHereSDKWrapperCallbacks listener){
        mCurrentActivity = currentActivity;
        mListener = listener;
    }

    public void removeListener(){
        mListener = null;
    }

    /**
     * Initializing the PayPalHere SDK. This will be first thing to do before interacting with PayPalHere SDK
     * @param context should pass the application context instead of activity context. This will be used by SDK where ever it is needed
     * @param serverName Name of the server to which SDK needs to connect. By default it will be connected live, In case for development
     *                   purpose we can connect it to Sandbox environment
     * @param compositeAccessToken The access token and refresh url which we will get from reail SDK node server.
     * @param listener listener to get notified about the result of initialization
     */
    public void initializeSDK(Context context, String serverName, String compositeAccessToken, final PayPalHereSDKWrapperCallbacks listener){
        PayPalHereSDK.init(context,serverName);
        PayPalHereSDK.registerAuthenticationListener(this);
        PayPalHereSDK.getCardReaderManager().registerCardReaderConnectionListener(this);

        if(null != compositeAccessToken) {
            setAccessTokenToSDK(compositeAccessToken, listener);
        }
    }

    /**
     * Setting the access token to SDK. One way to set is passing it during initialization. The other way is calling this api
     * @param compositeAccessToken The access token and refresh url which we will get from reail SDK node server.
     * @param listener listener to get notified about the result of setting the access token.
     */
    public void setAccessTokenToSDK(String compositeAccessToken, final PayPalHereSDKWrapperCallbacks listener){
        PayPalHereSDK.setCredentialsFromCompositeStrFromMidTierServer(compositeAccessToken, new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {
            @Override
            public void onSuccess(Merchant merchant) {
                Log.d(LOG_TAG, "initializeSDK setCredentialsFromCompositeStr onSuccess");
                if (null != listener) {
                    listener.onSuccessfulCompletionOfSettingAccessTokenToSDK();
                }
            }

            @Override
            public void onError(PPError<MerchantManager.MerchantErrors> merchantErrorsPPError) {
                Log.d(LOG_TAG, "initializeSDK setCredentialsFromCompositeStr onError error: " + merchantErrorsPPError);
                if (null != listener) {
                    listener.onErrorWhileSettingAccessTokenToSDK();
                }
            }
        });
    }

    public boolean isMagstripeReaderConnected(){
        Log.d(LOG_TAG, "isMagstripeReaderConnected");
        CardReaderManager cardReaderManager = PayPalHereSDK.getCardReaderManager();
        List<CardReaderManager.CardReader> availableReaders = cardReaderManager.getAvailableReaders();
        for(CardReaderManager.CardReader reader: availableReaders){
            if(reader.getReaderType().equals(CardReaderListener.ReaderTypes.MagneticCardReader)){
                return true;
            }
        }
        return false;
    }

    public boolean isEMVReaderConnected(){
        Log.d(LOG_TAG,"isEMVReaderConnected");
        CardReaderManager cardReaderManager = PayPalHereSDK.getCardReaderManager();
        List<CardReaderManager.CardReader> availableReaders = cardReaderManager.getAvailableReaders();
        for(CardReaderManager.CardReader reader: availableReaders){
            if(reader.getReaderType().equals(CardReaderListener.ReaderTypes.ChipAndPinReader)){
                return true;
            }
        }
        return false;
    }

    /**
     * Beginning the payment. This was the STEP 1 of the payment process
     * @param amount amount for which we need to process payment
     * @return it returns the Invoice object with the amount specified in argument. Application can use this invoice
     *         to add the further items in to it.
     */
    public Invoice beginPayment(BigDecimal amount){
        return PayPalHereSDK.getTransactionManager().beginPayment(amount,this);
    }

    /**
     * Take Payment this will be the STEP 2 of the process payment.
     * @param listener listener to get notified about the result of processPayment api call
     */
    public void takePayment(final PayPalHereSDKWrapperCallbacks listener){
        PayPalHereSDK.getTransactionManager().processPaymentWithSDKUI(TransactionManager.PaymentType.CardReader, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
            @Override
            public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                Log.d(LOG_TAG,"onSuccess");
                if(null != listener){
                    listener.onPaymentSuccess(responseObject);
                }
            }

            @Override
            public void onError(PPError<TransactionManager.PaymentErrors> error) {
                Log.d(LOG_TAG,"onError");
                if(null != listener){
                    listener.onPaymentFailure(error.getErrorCode());
                }
            }
        });
    }

    /**
     * For doing the refund
     * @param transactionRecord transaction record which we get after completing the payment
     * @param amount amount for which we need to process refund. It can be any amount less than or equal to the amount for which
     *               payment is completed
     * @param callback callback to get notified about the result.
     */
    public void doRefund(TransactionRecord transactionRecord, BigDecimal amount, final PayPalHereSDKWrapperCallbacks callback){
        PayPalHereSDK.getTransactionManager().beginRefund(transactionRecord,this);
        PayPalHereSDK.getTransactionManager().processRefund(amount, new DefaultResponseHandler<TransactionManager.PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
            @Override
            public void onSuccess(TransactionManager.PaymentResponse responseObject) {
                Log.d(LOG_TAG, "processRefund onSuccess");
                if (null != callback) {
                    callback.onRefundSuccess(responseObject);
                }
            }

            @Override
            public void onError(PPError<TransactionManager.PaymentErrors> error) {
                Log.d(LOG_TAG, "processRefund onError");
                if (null != callback) {
                    callback.onRefundFailure(error.getErrorCode());
                }
            }
        });
    }

    /**
     * for connecting to the Bluetooth EMV Reader
     * @param activity activity which is on front which will be used by sdk to start other activities inside sdk for showing the relavent UI
     * @param device bluetooth device for which we need to connect to
     * @param callback callback to get notified about the connection status.
     */
    public void connectoToEMVReader(Activity activity, BluetoothDevice device, final PayPalHereSDKWrapperCallbacks callback) {
        PayPalHereSDK.getCardReaderManager().connectToDevice(activity, device, new DefaultResponseHandler<BluetoothDevice, PPError<CardReaderManager.ChipAndPinConnectionStatus>>() {

            @Override
            public void onSuccess(BluetoothDevice responseObject) {
                Log.d(LOG_TAG, " connectDevice Response Handler: onSuccess");
                if (null != callback) {
                    callback.onEMVReaderConnected();
                }
            }

            @Override
            public void onError(PPError<CardReaderManager.ChipAndPinConnectionStatus> error) {
                Log.d(LOG_TAG, "connectDevice Response Handler: onError: " + error.getErrorCode());
                if (null != callback) {
                    callback.onEMVReaderConnectionFailure(error);
                }
            }
        });
    }

    public void disConnectEMVReader(BluetoothDevice device){
        PayPalHereSDK.getCardReaderManager().disconnectFromDevice(device);
    }

    public void setActiveReader(CardReaderListener.ReaderTypes readerType){
        PayPalHereSDK.getCardReaderManager().setActiveReader(readerType);
    }

    public CardReaderListener.ReaderTypes getActiveReader(){
        return PayPalHereSDK.getCardReaderManager().getActiveReaderType();
    }

    public List<CardReaderManager.CardReader> getConnectedReaders(){
        return PayPalHereSDK.getCardReaderManager().getAvailableReaders();
    }

    public void registerCardReaderEventListener() {
        PayPalHereSDK.getCardReaderManager().registerCardReaderKeyboardListener(new CardReaderKeyBoardEventListener());

    }

    public void unregisterCardReaderEventListener() {
        PayPalHereSDK.getCardReaderManager().unregisterCardReaderKeyboardListener();
    }

    public class CardReaderKeyBoardEventListener implements CardReaderManager.CardReaderEventListener {

        @Override
        public void onConfirmPressed() {
            Log.d(LOG_TAG,"onConfirmPressed");
        }

        @Override
        public void onCancelPressed() {
            Log.d(LOG_TAG,"onCancelPressed");
        }

        @Override
        public void onBackArrowPressed() {
            Log.d(LOG_TAG,"onBackArrowPressed");
        }
    }

    @Override
    public void onPaymentReaderConnected(CardReaderListener.ReaderTypes readerTypes, CardReaderListener.ReaderConnectionTypes readerConnectionTypes) {
        Log.d(LOG_TAG,"onPaymentReaderConnected readerType: "+readerTypes+" ReaderConnectionType: "+readerConnectionTypes);

        if(CardReaderListener.ReaderTypes.MagneticCardReader.equals(readerTypes)){
            if(null != mListener){
                mListener.onMagstripeReaderConnected();
            }
        }else if(CardReaderListener.ReaderTypes.ChipAndPinReader.equals(readerTypes)){
            if(null != mListener){
                mListener.onEMVReaderConnected();
            }
        }
    }

    @Override
    public void onPaymentReaderDisconnected(CardReaderListener.ReaderTypes readerTypes) {
        Log.d(LOG_TAG,"onPaymentReaderDisconnected readerTypes: "+readerTypes);
        if(CardReaderListener.ReaderTypes.MagneticCardReader.equals(readerTypes)){
            if(null != mListener){
                mListener.onMagstripeReaderDisconnected();
            }
        }else if(CardReaderListener.ReaderTypes.ChipAndPinReader.equals(readerTypes)){
            if(null != mListener){
                mListener.onEMVReaderDisconnected();
            }
        }
    }

    @Override
    public void onConnectedReaderNeedsSoftwareUpdate(boolean b) {
        Log.d(LOG_TAG,"onConnectedReaderNeedsSoftwareUpdate required: "+b);
    }

    @Override
    public void onConnectedReaderSoftwareUpdateComplete() {
        Log.d(LOG_TAG,"onConnectedReaderSoftwareUpdateComplete");
    }

    @Override
    public void onMultipleCardReadersConnected(List<CardReaderListener.ReaderTypes> list) {
        Log.d(LOG_TAG,"onMultipleCardReadersConnected list: "+list);
        if(null != mListener){
            mListener.onMultipleCardReadersConnected(list);
        }
    }

    @Override
    public void onActiveReaderChanged(CardReaderListener.ReaderTypes readerTypes) {
        Log.d(LOG_TAG,"onActiveReaderChanged readerTypes: "+readerTypes);
    }

    @Override
    public void onInvalidToken() {
        Log.d(LOG_TAG,"onInvalidToken");
    }

    @Override
    public TransactionControlAction onPreAuthorize(Invoice inv, String preAuthJSON) {
        Log.d(LOG_TAG,"onPreAuthorize");
        return null;
    }

    @Override
    public void onPostAuthorize(boolean didFail) {
        Log.d(LOG_TAG,"onPostAuthorize");
    }

    @Override
    public void onPrintRequested(Activity activity, Invoice invoice) {
        Log.d(LOG_TAG,"onPrintRequested");
    }

    @Override
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    @Override
    public SDKSignatureScreenOptions getSignatureScreenOpts() {
        Log.d(LOG_TAG,"getSignatureScreenOpts");
        return null;
    }

    @Override
    public SDKReceiptScreenOptions getReceiptScreenOptions() {
        Log.d(LOG_TAG,"getReceiptScreenOptions");
        return null;
    }

    @Override
    public void onUserPaymentOptionSelected(PaymentOption paymentOption) {
        Log.d(LOG_TAG,"onUserPaymentOptionSelected");
        if(null != mListener){
            mListener.onSuccessfulCardRead();
        }
    }

    @Override
    public void onUserRefundOptionSelected(PaymentOption paymentOption) {
        Log.d(LOG_TAG,"onUserRefundOptionSelected");
    }

    @Override
    public void onTokenExpired(Activity activity, TokenExpirationHandler listener) {
        Log.d(LOG_TAG,"onTokenExpired");
    }

    @Override
    public void onContactlessReaderTimeout(Activity activity, ContactlessReaderTimeoutOptionsHandler handler) {
        Log.d(LOG_TAG,"onContactlessReaderTimeout");
    }

    @Override
    public void onReadyToCancelTransaction(CancelTransactionReason cancelTransactionReason) {
        Log.d(LOG_TAG,"onReadyToCancelTransaction");
    }

    @Override
    public TipPromptOptions shouldPromptForTips() {
        return TipPromptOptions.NONE;
    }

    @Override
    public Bitmap provideSignatureBitmap() {
        return null;
    }

    @Override
    public void onReaderDisplayUpdated(PresentedReaderDisplay readerDisplay) {
        Log.d(LOG_TAG,"onReaderDisplayUpdated : " + readerDisplay);
    }
}
