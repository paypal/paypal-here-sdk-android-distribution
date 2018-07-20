package com.paypal.heresdk.sampleapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.paypalretailsdk.DeviceUpdate;
import com.paypal.paypalretailsdk.FormFactor;
import com.paypal.paypalretailsdk.Invoice;
import com.paypal.paypalretailsdk.PaymentDevice;
import com.paypal.paypalretailsdk.RetailSDK;
import com.paypal.paypalretailsdk.RetailSDKException;
import com.paypal.paypalretailsdk.TransactionBeginOptions;
import com.paypal.paypalretailsdk.TransactionContext;
import com.paypal.paypalretailsdk.TransactionManager;
import com.paypal.paypalretailsdk.TransactionRecord;

import org.androidannotations.annotations.EActivity;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@EActivity
public class ChargeActivity extends ToolbarActivity implements View.OnClickListener
{
    private static final String LOG_TAG = ChargeActivity.class.getSimpleName();
    public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
    public static final String INTENT_AUTH_ID = "AUTH_ID";
    public static final String INTENT_INVOICE_ID = "INVOICE_ID";
    private static final int REQUEST_OPTIONS_ACTIVITY = 1;

    TransactionContext currentTransaction;
    Invoice currentInvoice;
    Invoice invoiceForRefund;

    private EditText amountEditText;
    private StepView createInvoiceStep;
    private StepView createTxnStep;
    private StepView acceptTxnStep;
    private LinearLayout paymentOptionsStep;
    private TextView step3Text;
    private TextView paymentOptionsText;
    private ImageView paymentOptionsArrow;
    private LinearLayout offlineModeContainer;
    private TextView enabledText;
    private SharedPreferences sharedPrefs;

    // payment option constants
    public static final String OPTION_AUTH_CAPTURE = "authCapture";
    public static final String OPTION_CARD_READER_PROMPT = "cardReader";
    public static final String OPTION_APP_PROMPT= "appPrompt";
    public static final String OPTION_TIP_ON_READER = "tipReader";
    public static final String OPTION_AMOUNT_TIP = "amountTip";
    public static final String OPTION_MAGNETIC_SWIPE = "magneticSwipe";
    public static final String OPTION_CHIP = "chip";
    public static final String OPTION_CONTACTLESS = "contactless";
    public static final String OPTION_MANUAL_CARD= "manualCard";
    public static final String OPTION_SECURE_MANUAL= "secureManual";
    public static final String OPTION_TAG= "tag";

    // payment option booleans
    private boolean isAuthCaptureEnabled = false;
    private boolean isCardReaderPromptEnabled = true;
    private boolean isAppPromptEnabled = true;
    private boolean isTippingOnReaderEnabled = false;
    private boolean isAmountBasedTippingEnabled = false;
    private boolean isMagneticSwipeEnabled = true;
    private boolean isChipEnabled = true;
    private boolean isContactlessEnabled = true;
    private boolean isManualCardEnabled = true;
    private boolean isSecureManualEnabled = true;
    private String tagString = "";



    @Override
    public int getLayoutResId()
    {
        return R.layout.transaction_activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        amountEditText = (EditText)findViewById(R.id.amount);
        TextView paymentAmountText = (TextView) findViewById(R.id.payment_amount_text);
        paymentAmountText.setText(getString(R.string.payment_amount) + " (" + NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + ")");
        createInvoiceStep = (StepView)findViewById(R.id.create_invoice_step);
        createInvoiceStep.setOnButtonClickListener(this);
        createTxnStep = (StepView)findViewById(R.id.create_txn_step);
        createTxnStep.setOnButtonClickListener(this);
        acceptTxnStep = (StepView)findViewById(R.id.accept_txn_step);
        acceptTxnStep.setOnButtonClickListener(this);

        paymentOptionsStep = (LinearLayout) findViewById(R.id.payment_options_container);
        paymentOptionsStep.setOnClickListener(this);
        paymentOptionsText = (TextView) findViewById(R.id.payment_options_text);
        step3Text = (TextView) findViewById(R.id.step3_text);
        paymentOptionsArrow = (ImageView) findViewById(R.id.payment_options_arrow);
        disablePaymentOptionsStep();

        offlineModeContainer = (LinearLayout) findViewById(R.id.offline_mode_container);
        offlineModeContainer.setOnClickListener(this);

        sharedPrefs = getSharedPreferences(OfflinePayActivity.PREF_NAME, Context.MODE_PRIVATE);
        enabledText = (TextView) findViewById(R.id.offline_mode_status_text);



      amountEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
          {

            if (actionId == EditorInfo.IME_ACTION_DONE){
                if (TextUtils.isEmpty(amountEditText.getText().toString()))
                {
                    Toast.makeText(ChargeActivity.this, "Amount cannot be empty", Toast.LENGTH_SHORT).show();
                    return true;
                }else
                {
                    createInvoiceStep.setStepEnabled();
                    createTxnStep.setStepDisabled();
                    disablePaymentOptionsStep();
                    paymentOptionsStep.setOnClickListener(null);
                    acceptTxnStep.setStepDisabled();
                    return false;

                }
            }
            return false;
          }
        });


    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if(sharedPrefs.getBoolean(OfflinePayActivity.OFFLINE_MODE,false))
        {
            enabledText.setText("ENABLED");
            enabledText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }else{
            enabledText.setText("DISABLED");
            enabledText.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        }
    }


    public void enablePaymentOptionsStep(){
        paymentOptionsArrow.setAlpha(1f);
        paymentOptionsText.setTextColor(getResources().getColor(R.color.sdk_black));
        step3Text.setTextColor(getResources().getColor(R.color.sdk_black));
        paymentOptionsStep.setOnClickListener(this);

    }
    public void disablePaymentOptionsStep(){
        paymentOptionsArrow.setAlpha(0.5f);
        paymentOptionsText.setTextColor(getResources().getColor(R.color.sdk_gray));
        step3Text.setTextColor(getResources().getColor(R.color.sdk_gray));
        paymentOptionsStep.setOnClickListener(null);

    }



    public void onCreateInvoiceClicked()
    {
        Log.d(LOG_TAG, "onCreateInvoiceClicked");


        amountEditText = (EditText) findViewById(R.id.amount);
        String amountText = amountEditText.getText().toString();
        BigDecimal amount = BigDecimal.ZERO;
        if (null != amountText && amountText.length() > 0) {
            amountText = String.format("%.2f", Double.parseDouble(amountText));
            amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) == 0)
            {
                showInvalidAmountAlertDialog();
                return;
            }
        }
        else
        {
            showInvalidAmountAlertDialog();
            return;
        }
        Log.d(LOG_TAG, "onCreateInvoiceClicked amount:" + amount);

        currentInvoice = new Invoice(null);
        BigDecimal quantity = new BigDecimal(1);
        currentInvoice.addItem("Item", quantity, amount, 1, null);
        // BigDecimal gratuityAmt = new BigDecimal(gratuityField.getText().toString());
        // if(gratuityAmt.intValue() > 0){
        //    invoice.setGratuityAmount(gratuityAmt);
        // }

    }



    public void onCreateTransactionClicked()
    {
        Log.d(LOG_TAG, "onCreateTransactionClicked");


        RetailSDK.getTransactionManager().createTransaction(currentInvoice, new TransactionManager.TransactionCallback()
        {
            @Override
            public void transaction(RetailSDKException e, final TransactionContext context)
            {
                if (e != null) {
                    final String errorTxt = e.toString();
                    ChargeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "create transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    currentTransaction = context;
                }
            }
        });
    }

    public void onAcceptTransactionClicked()
    {
        Log.d(LOG_TAG, "onAcceptTransactionClicked");

        PaymentDevice activeDevice = RetailSDK.getDeviceManager().getActiveReader();
        DeviceUpdate deviceUpdate = activeDevice.getPendingUpdate();
        if (deviceUpdate != null && deviceUpdate.getIsRequired() && !deviceUpdate.getWasInstalled())
        {
            deviceUpdate.offer(new DeviceUpdate.CompletedCallback()
            {
                @Override
                public void completed(RetailSDKException e, Boolean aBoolean)
                {
                    Log.d(LOG_TAG, "device update completed");
                    ChargeActivity.this.beginPayment();
                }
            });

        }
        else {
            beginPayment();
        }
    }




    private void beginPayment()
    {
        currentTransaction.setCompletedHandler(new TransactionContext.TransactionCompletedCallback() {
            @Override
            public void transactionCompleted(RetailSDKException error, TransactionRecord record) {
                ChargeActivity.this.transactionCompleted(error, record);
            }
        });

        TransactionBeginOptions options = new TransactionBeginOptions();
        options.setShowPromptInCardReader(isCardReaderPromptEnabled);
        options.setShowPromptInApp(isAppPromptEnabled);
        options.setIsAuthCapture(isAuthCaptureEnabled);
        options.setAmountBasedTipping(isAmountBasedTippingEnabled);
        options.setTippingOnReaderEnabled(isTippingOnReaderEnabled);
        options.setTag(tagString);
        options.setPreferredFormFactors(getPreferredFormFactors());
        currentTransaction.beginPayment(options);
    }

    void transactionCompleted(RetailSDKException error, final TransactionRecord record) {
        if (error != null) {
            final String errorTxt = error.toString();

            if (errorTxt.toLowerCase().contains("offline payment enabled")){
                goToOfflinePayCompleteActivity();
            }else
            {
                this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), "transaction error: " + errorTxt, Toast.LENGTH_SHORT).show();
                        //refundButton.setEnabled(false);
                    }
                });
            }
        } else {
            invoiceForRefund = currentTransaction.getInvoice();
            final String recordTxt =  record.getTransactionNumber();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAuthCaptureEnabled) {
                        goToAuthCaptureActivity(record);
                    }
                    else
                    {
                        goToRefundActivity();
                    }
                    Toast.makeText(getApplicationContext(), String.format("Completed Transaction %s", recordTxt), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void goToOfflinePayCompleteActivity()
    {
        Intent intent = new Intent(ChargeActivity.this,OfflinePaySuccessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public void goToAuthCaptureActivity(TransactionRecord record){
        Log.d(LOG_TAG, "goToAuthCaptureActivity");
        AuthCaptureActivity.invoiceForRefund = invoiceForRefund;
        Intent intent = new Intent(ChargeActivity.this, AuthCaptureActivity.class);
        String authId = record.getTransactionNumber();
        String invoiceId = record.getInvoiceId();
        BigDecimal amount = currentInvoice.getTotal();
        Log.d(LOG_TAG, "goToAuthCaptureActivity total: " + amount);
        intent.putExtra(INTENT_TRANX_TOTAL_AMOUNT, amount);
        intent.putExtra(INTENT_AUTH_ID, authId);
        intent.putExtra(INTENT_INVOICE_ID, invoiceId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goToRefundActivity(){
        Log.d(LOG_TAG, "goToRefundActivity");
        RefundActivity.invoiceForRefund = invoiceForRefund;
        Intent refundIntent = new Intent(ChargeActivity.this, RefundActivity.class);
        BigDecimal amount = currentInvoice.getTotal();
        Log.d(LOG_TAG, "goToRefundActivity total: " + amount);
        refundIntent.putExtra(INTENT_TRANX_TOTAL_AMOUNT, amount);
        refundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(refundIntent);
    }


    private void showInvalidAmountAlertDialog(){
        Log.d(LOG_TAG, "showInvalidAmountAlertDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(ChargeActivity.this);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.error_invalid_amount);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, "takePayment invalid amount alert dialog onClick");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == REQUEST_OPTIONS_ACTIVITY)
            {
                Bundle optionsBundle = data.getExtras();
                isAuthCaptureEnabled = optionsBundle.getBoolean(OPTION_AUTH_CAPTURE);
                isAppPromptEnabled = optionsBundle.getBoolean(OPTION_APP_PROMPT);
                isTippingOnReaderEnabled = optionsBundle.getBoolean(OPTION_TIP_ON_READER);
                isAmountBasedTippingEnabled = optionsBundle.getBoolean(OPTION_AMOUNT_TIP);
                isMagneticSwipeEnabled = optionsBundle.getBoolean(OPTION_MAGNETIC_SWIPE);
                isChipEnabled = optionsBundle.getBoolean(OPTION_CHIP);
                isContactlessEnabled = optionsBundle.getBoolean(OPTION_CONTACTLESS);
                isManualCardEnabled = optionsBundle.getBoolean(OPTION_MANUAL_CARD);
                isSecureManualEnabled = optionsBundle.getBoolean(OPTION_SECURE_MANUAL);
                tagString = optionsBundle.getString(OPTION_TAG);

                acceptTxnStep.setStepEnabled();
            }
        }
    }


    @Override
    public void onClick(View v)
    {
        if (v == createInvoiceStep.getButton())
        {
            onCreateInvoiceClicked();
            createInvoiceStep.setStepCompleted();
            createTxnStep.setStepEnabled();
        }
        else if(v == createTxnStep.getButton())
        {
            onCreateTransactionClicked();
            createTxnStep.setStepCompleted();
            enablePaymentOptionsStep();

        }
        else if(v == acceptTxnStep.getButton())
        {
            onAcceptTransactionClicked();
        }else if(v == paymentOptionsStep){
            Intent optionsActivity = new Intent(this,PaymentOptionsActivity.class);
            optionsActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            optionsActivity.putExtras(getOptionsBundle());
            startActivityForResult(optionsActivity,REQUEST_OPTIONS_ACTIVITY);
        }else if(v == offlineModeContainer){
            Intent offlineActivity = new Intent(this,OfflinePayActivity.class);
            offlineActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(offlineActivity);
        }


    }

    public List<FormFactor> getPreferredFormFactors()
    {
        List<FormFactor> formFactors = new ArrayList<>();
        if (isMagneticSwipeEnabled)
        {
            formFactors.add(FormFactor.MagneticCardSwipe);
        }
        if (isChipEnabled)
        {
            formFactors.add(FormFactor.Chip);
        }
        if (isContactlessEnabled)
        {
            formFactors.add(FormFactor.EmvCertifiedContactless);
        }
        if (isSecureManualEnabled)
        {
            formFactors.add(FormFactor.SecureManualEntry);
        }
        if (isManualCardEnabled)
        {
            formFactors.add(FormFactor.ManualCardEntry);
        }

        if (formFactors.size() == 0)
        {
            formFactors.add(FormFactor.None);
        }
        return formFactors;

    }



    private Bundle getOptionsBundle()
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(OPTION_AUTH_CAPTURE,isAuthCaptureEnabled);
        bundle.putBoolean(OPTION_CARD_READER_PROMPT,isCardReaderPromptEnabled);
        bundle.putBoolean(OPTION_APP_PROMPT,isAppPromptEnabled);
        bundle.putBoolean(OPTION_TIP_ON_READER,isTippingOnReaderEnabled);
        bundle.putBoolean(OPTION_AMOUNT_TIP,isAmountBasedTippingEnabled);
        bundle.putBoolean(OPTION_MAGNETIC_SWIPE,isMagneticSwipeEnabled);
        bundle.putBoolean(OPTION_CHIP,isChipEnabled);
        bundle.putBoolean(OPTION_CONTACTLESS,isContactlessEnabled);
        bundle.putBoolean(OPTION_MANUAL_CARD,isManualCardEnabled);
        bundle.putBoolean(OPTION_SECURE_MANUAL,isSecureManualEnabled);
        bundle.putString(OPTION_TAG,tagString);
        return bundle;
    }
}
