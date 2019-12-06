package com.paypal.heresdk.sampleapp.activities.transactionSelection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.heresdk.sampleapp.activities.vaultAndPayTransaction.VaultAndPayTransaction;
import com.paypal.heresdk.sampleapp.activities.vaultTransaction.VaultTransaction;
import com.paypal.heresdk.sampleapp.ui.ChargeActivity;

public class TransactionSelection extends AppCompatActivity
{

  private RecyclerView _transactionSelectionRecyclerView;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_transaction_selection);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    _transactionSelectionRecyclerView = findViewById(R.id.transaction_selection_recycler_view);
    _transactionSelectionRecyclerView.setHasFixedSize(true);
    _transactionSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    _transactionSelectionRecyclerView.setAdapter(
        new TransactionSelectionAdapter(
            new String[]{
                this.getString(R.string.transaction_selection_vault_and_pay),
                this.getString(R.string.transaction_selection_vault),
                this.getString(R.string.transaction_selection_pay),
                this.getString(R.string.transaction_selection_offline),
                this.getString(R.string.transaction_selection_auth),
                this.getString(R.string.transaction_selection_auth_capture),
            }
        )
    );

  }


  static class TransactionSelectionAdapter extends RecyclerView.Adapter<TransactionSelectionAdapter.TransactionSelectionViewHolder>
  {

    private String[] _transactionTypes;


    static class TransactionSelectionViewHolder extends RecyclerView.ViewHolder
    {
      LinearLayout _linearLayout;


      TransactionSelectionViewHolder(LinearLayout linearLayout)
      {
        super(linearLayout);
        _linearLayout = linearLayout;
      }
    }


    TransactionSelectionAdapter(String[] transactionTypes)
    {
      _transactionTypes = transactionTypes;
    }


    @NonNull
    @Override
    public TransactionSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
//      TextView textView = parent.findViewById(R.id.transaction_selection_text_view);
      LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.content_transaction_selection, parent, false);
      return new TransactionSelectionViewHolder(linearLayout);
    }


    @Override
    public void onBindViewHolder(@NonNull TransactionSelectionViewHolder holder, int position)
    {
      TextView textView = holder._linearLayout.findViewById(R.id.transaction_selection_text_view);
      textView.setText(_transactionTypes[position]);
      holder._linearLayout.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View view)
        {
          transitionToActivity(view);
        }


        void transitionToActivity(@NonNull View _view)
        {
          String transactionType = ((TextView) _view.findViewById(R.id.transaction_selection_text_view)).getText().toString();
          _view.getContext().startActivity(getIntent(transactionType, _view.getContext()));
        }


        @NonNull
        Intent getIntent(@NonNull String transactionType, @NonNull Context context)
        {
          if (transactionType.equalsIgnoreCase(context.getString(R.string.transaction_selection_auth_capture)))
          {
            return new Intent(context, ChargeActivity.class);
          }
          if (transactionType.equalsIgnoreCase(context.getString(R.string.transaction_selection_auth)))
          {
            return new Intent(context, ChargeActivity.class);
          }
          if (transactionType.equalsIgnoreCase(context.getString(R.string.transaction_selection_offline)))
          {
            return new Intent(context, ChargeActivity.class);
          }
          if (transactionType.equalsIgnoreCase(context.getString(R.string.transaction_selection_pay)))
          {
            return new Intent(context, ChargeActivity.class);
          }
          if (transactionType.equalsIgnoreCase(context.getString(R.string.transaction_selection_vault)))
          {
            return new Intent(context, VaultTransaction.class);
          }
          if (transactionType.equalsIgnoreCase(context.getString(R.string.transaction_selection_vault_and_pay)))
          {
            return new Intent(context, VaultAndPayTransaction.class);
          }
          return new Intent(context, ChargeActivity.class);
        }

      });
    }


    @Override
    public int getItemCount()
    {
      return _transactionTypes.length;
    }

  }

}
