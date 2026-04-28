package com.example.pos_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.Transaction;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";
    private AppDatabase db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = AppDatabase.getInstance(this);
        int transactionId = getIntent().getIntExtra(EXTRA_TRANSACTION_ID, -1);

        if (transactionId != -1) {
            new Thread(() -> {
                Transaction transaction = db.appDao().getTransactionById(transactionId);
                if (transaction != null) {
                    runOnUiThread(() -> populateDetails(transaction));
                }
            }).start();
        }

        findViewById(R.id.btn_reprint_receipt).setOnClickListener(v -> {
            if (transactionId != -1) {
                new Thread(() -> {
                    Transaction transaction = db.appDao().getTransactionById(transactionId);
                    if (transaction != null) {
                        runOnUiThread(() -> ReceiptUtils.printTransactionReceipt(this, transaction));
                    }
                }).start();
            }
        });
    }

    private void populateDetails(Transaction transaction) {
        TextView tvDate = findViewById(R.id.tv_detail_date);
        TextView tvTotal = findViewById(R.id.tv_detail_total);
        TextView tvId = findViewById(R.id.tv_transaction_id);
        LinearLayout itemsContainer = findViewById(R.id.layout_items_container);

        tvDate.setText(dateFormat.format(new Date(transaction.timestamp)));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", transaction.amount));
        tvId.setText("ID: #" + transaction.id);

        itemsContainer.removeAllViews();

        // Simple parsing of the description for now
        String content = transaction.description;
        if (content.startsWith("POS Sale: ")) {
            content = content.substring(10);
        }
        
        // Remove discount info if present for item listing
        int discountIndex = content.indexOf(" (Discount");
        if (discountIndex != -1) {
            content = content.substring(0, discountIndex);
        }

        String[] items = content.split(", ");
        for (String itemStr : items) {
            if (itemStr.trim().isEmpty()) continue;
            
            View itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, itemsContainer, false);
            TextView text = itemView.findViewById(android.R.id.text1);
            text.setText(itemStr);
            text.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            itemsContainer.addView(itemView);
        }
    }
}
