package com.paypal.heresdk.sampleapp.ui;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
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
import com.paypal.heresdk.sampleapp.login.LoginActivity;
import com.paypal.paypalretailsdk.DeviceUpdate;
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

@EActivity
public class ChargeActivity extends ToolbarActivity implements OptionsDialogFragment.OptionsDialogListener, View.OnClickListener
{
    private static final String LOG_TAG = ChargeActivity.class.getSimpleName();
    public static final String INTENT_TRANX_TOTAL_AMOUNT = "TOTAL_AMOUNT";
    public static final String INTENT_AUTH_ID = "AUTH_ID";
    public static final String INTENT_INVOICE_ID = "INVOICE_ID";

    TransactionContext currentTransaction;
    Invoice currentInvoice;
    Invoice invoiceForRefund;


    OptionsDialogFragment optionsDialogFragment;
    OfflineModeDialogFragment offlineModeDialogFragment;

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


    @Override
    public int getLayoutResId()
    {
        return R.layout.transaction_activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        optionsDialogFragment = new OptionsDialogFragment();
        offlineModeDialogFragment = new OfflineModeDialogFragment();
        amountEditText = (EditText)findViewById(R.id.amount);
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

    }

    public void onCreateInvoiceViewCodeClicked(View view)
    {
        final TextView txtViewCode = (TextView) findViewById(R.id.txtCreateInvoiceCode);

        if (txtViewCode.getVisibility() == View.GONE)
        {
            txtViewCode.setVisibility(View.VISIBLE);
        } else
        {
            txtViewCode.setVisibility(View.GONE);
        }

    }
    public void onCreateTranxViewCodeClicked(View view)
    {
        final TextView txtViewCode = (TextView) findViewById(R.id.txtCreateTranxCode);

        if (txtViewCode.getVisibility() == View.GONE)
        {
            txtViewCode.setVisibility(View.VISIBLE);
        } else
        {
            txtViewCode.setVisibility(View.GONE);
        }
    }

    public void onViewCodeAcceptTranxClicked(View view)
    {
      final TextView txtViewCode = (TextView) findViewById(R.id.txtAcceptTransactionCode);

      if (txtViewCode.getVisibility() == View.GONE)
      {
        txtViewCode.setVisibility(View.VISIBLE);
      } else
      {
        txtViewCode.setVisibility(View.GONE);
      }
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

        final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButton1);
        final TextView txtCreateInvoice = (TextView) findViewById(R.id.txtCreateInvoice);
        final TextView txtCreateTranx = (TextView) findViewById(R.id.txtCreateTransaction);

        imgView.setImageResource(R.drawable.small_greenarrow);
        imgView.setClickable(false);
        txtCreateInvoice.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
        txtCreateInvoice.setClickable(false);

        txtCreateTranx.setClickable(true);
        txtCreateTranx.setTextColor(getResources().getColor(R.color.sdk_blue));
    }



    public void onCreateTransactionClicked()
    {
        Log.d(LOG_TAG, "onCreateTransactionClicked");
        final ImageView imgView = (ImageView) findViewById(R.id.imageBlueButton2);
        final TextView txtCreateTranx = (TextView) findViewById(R.id.txtCreateTransaction);
        final TextView txtAcceptTranx = (TextView) findViewById(R.id.txtAcceptTransaction);

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
                }
                else
                {
                    ChargeActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            currentTransaction = context;

                            imgView.setImageResource(R.drawable.small_greenarrow);
                            imgView.setClickable(false);
                            txtCreateTranx.setTextColor(getResources().getColor(R.color.sdk_dark_gray));
                            txtCreateTranx.setClickable(false);

                            txtAcceptTranx.setClickable(true);
                            txtAcceptTranx.setTextColor(getResources().getColor(R.color.sdk_blue));
                        }
                    });
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

    public void onPaymentOptionsClicked(View view){

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (optionsDialogFragment==null){

           optionsDialogFragment = new OptionsDialogFragment();
        }
        if (optionsDialogFragment.isAdded()){
            ft.show(optionsDialogFragment);
        }else
        {
            optionsDialogFragment.show(ft, "OptionsDialogFragment");
        }


    }
    public void onOfflineModeClicked(View view){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (offlineModeDialogFragment==null)
        {

            offlineModeDialogFragment = new OfflineModeDialogFragment();
        }
        if (offlineModeDialogFragment.isAdded()){
            ft.show(offlineModeDialogFragment);
        }else
        {
            offlineModeDialogFragment.show(ft, "OfflineModeDialogFragment");
        }



    }

    @Override
    public void onCloseOptionsDialogClicked(){
        optionsDialogFragment.dismiss();
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
        options.setShowPromptInCardReader(optionsDialogFragment.isCardPreaderPromptEnabled());
        options.setShowPromptInApp(optionsDialogFragment.isAppPromptSwitchEnabled());
        options.setIsAuthCapture(optionsDialogFragment.isAuthCaptureEnabled());
        options.setAmountBasedTipping(optionsDialogFragment.isAmountBasedTippingEnabled());
        options.setTippingOnReaderEnabled(optionsDialogFragment.isTippingOnReaderEnabled());
        options.setTag(optionsDialogFragment.getTagValue());
        options.setPreferredFormFactors(optionsDialogFragment.getPreferredFormFactors());
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
                    if (optionsDialogFragment.isAuthCaptureEnabled()) {
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


    public void goBackToLoginActivity(View view){
        Log.d(LOG_TAG, "goBackToLoginActivity");
        RetailSDK.logout();
        Intent loginIntent = new Intent(ChargeActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
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
            // go to payment options screen
        }else if(v == offlineModeContainer){
            // go to offline mode screen
            Intent offlineActivity = new Intent(this,OfflinePayActivity.class);
            offlineActivity.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(offlineActivity);
        }


    }
}
