package com.paypal.heresdk.sampleapp.sdk;

import com.paypal.merchant.sdk.CardReaderListener;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.PPError;

import java.util.List;

public class PayPalHereSDKWrapperCallbacks {
    private static final String LOG_TAG = PayPalHereSDKWrapperCallbacks.class.getSimpleName();

    public void onSuccessfulCompletionOfSettingAccessTokenToSDK(){}

    public void onErrorWhileSettingAccessTokenToSDK(){}

    public void onMagstripeReaderConnected(){}

    public void onEMVReaderConnected(){}

    public void onEMVReaderConnectionFailure(PPError<CardReaderManager.ChipAndPinConnectionStatus> error){}

    public void onMagstripeReaderDisconnected(){}

    public void onEMVReaderDisconnected(){}

    public void onSuccessfulCardRead(){}

    public void onMultipleCardReadersConnected(List<CardReaderListener.ReaderTypes> list){}

    public void onPaymentSuccess(TransactionManager.PaymentResponse responseObject){}

    public void onPaymentFailure(TransactionManager.PaymentErrors errors){}

    public void onRefundSuccess(TransactionManager.PaymentResponse responseObject){}

    public void onRefundFailure(TransactionManager.PaymentErrors errors){}
}
