package com.paypal.sampleapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.paypal.merchant.sdk.domain.TransactionRecord;

import java.util.ArrayList;

public class LocalPreferences {
    private static final String LOG_TAG = LocalPreferences.class.getSimpleName();
    private static final String PREFERENCES_FILE_NAME = "PayPalHereSampleApp";
    private static final String PREFERENCE_NAME_BN_CODE = "BNCode";
    private static final String PREFERENCE_NAME_CASHIER_ID = "CashierID";
    private static final String PREFERENCE_NAME_REFRESH_URL = "RefershURL";
    private static final String PREFERENCE_NAME_AUTHORIZE_OPTION = "AuthorizeOption";
    private static SharedPreferences mSharedPreferences;

    private static String mBNCode;
    private static String mCashierID;
    private static String mRefreshUrl;
    private static boolean mAuthorizeOption;

    /* Temporarily storing the transaction records for this session only. Ideally we need to store these persistantly
       Since we are have some trouble with serializing the record, we are storing temporarily for each session.
       Soon we need to fix the serialization of transaction record
     */
    private static ArrayList<TransactionRecord> mAuthorizedTransactionRecordList;
    private static ArrayList<TransactionRecord> mCompletedTransactionRecordList;
    private static TransactionRecord mRecentTransactionRecord;

    public static void init(Context c){
        mSharedPreferences = c.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        mBNCode = mSharedPreferences.getString(PREFERENCE_NAME_BN_CODE,null);
        mCashierID = mSharedPreferences.getString(PREFERENCE_NAME_CASHIER_ID,null);
        mRefreshUrl = mSharedPreferences.getString(PREFERENCE_NAME_REFRESH_URL, null);
        mAuthorizeOption = mSharedPreferences.getBoolean(PREFERENCE_NAME_AUTHORIZE_OPTION,false);

        mAuthorizedTransactionRecordList = new ArrayList<TransactionRecord>();
        mCompletedTransactionRecordList = new ArrayList<TransactionRecord>();
    }

    public static void setBNCode(String bnCode){
        if(null != mSharedPreferences && null != bnCode){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(PREFERENCE_NAME_BN_CODE,bnCode);
            editor.commit();
            mBNCode = bnCode;
        }
    }

    public static String getBNCode(){
        return mBNCode;
    }

    public static void setCashierID(String cahsierID){
        if(null != mSharedPreferences && null != cahsierID){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(PREFERENCE_NAME_CASHIER_ID,cahsierID);
            editor.commit();
            mCashierID = cahsierID;
        }
    }

    public static String getCashierID(){
        return mCashierID;
    }

    public static void setRefreshURL(String refreshURL){
        if(null != mSharedPreferences && null != refreshURL){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(PREFERENCE_NAME_REFRESH_URL,refreshURL);
            editor.commit();
            mRefreshUrl = refreshURL;
        }
    }

    public static String getRefreshUrl(){
        return mRefreshUrl;
    }

    public static void removeRefreshURL(){
        if(null != mSharedPreferences){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(PREFERENCE_NAME_REFRESH_URL);
            editor.commit();
            mRefreshUrl = null;
        }
    }

    public static void setAuthorizeOption(boolean on){
        if(null != mSharedPreferences) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(PREFERENCE_NAME_AUTHORIZE_OPTION, on);
            mAuthorizeOption = on;
        }
    }

    public static boolean getAuthorizeOption(){
        return mAuthorizeOption;
    }

    public static void storeAuthorizedTransactionRecord(TransactionRecord record){
        mAuthorizedTransactionRecordList.add(record);
    }

    public static ArrayList<TransactionRecord> getAuthorizedTransactionRecordList(){
        return mAuthorizedTransactionRecordList;
    }

    public static void removeTransactionRecordFromAuthorizedList(TransactionRecord record){
        if(null != record) {
            for (TransactionRecord record1 : mAuthorizedTransactionRecordList) {
                if (record1.getInvoice().getId().equals(record.getInvoice().getId())) {
                    mAuthorizedTransactionRecordList.remove(record1);
                }
            }
        }
    }

    public static void storeCompletedTransactionRecord(TransactionRecord record){
        mCompletedTransactionRecordList.add(record);
    }

    public static ArrayList<TransactionRecord> getCompletedTransactionRecordList(){
        return mCompletedTransactionRecordList;
    }

    public static void removeTransactionRecordFromCompletedList(TransactionRecord record){
        if(null != record) {
            for (TransactionRecord record1 : mCompletedTransactionRecordList) {
                if (record1.getInvoice().getId().equals(record.getInvoice().getId())) {
                    mCompletedTransactionRecordList.remove(record1);
                }
            }
        }
    }

    public static void storeRecentTransactionRecord(TransactionRecord record){
        mRecentTransactionRecord = record;
    }

    public static TransactionRecord getmRecentTransactionRecord(){
        return mRecentTransactionRecord;
    }
}
