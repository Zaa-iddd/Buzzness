package com.example.pos_app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.Transaction;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private AppDatabase db;
    private TransactionAdapter adapter;
    private RecyclerView rvHistory;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = AppDatabase.getInstance(this);
        rvHistory = findViewById(R.id.rv_history);
        tvEmpty = findViewById(R.id.tv_empty_history);

        setupRecyclerView();
        loadData();
    }

    private void setupRecyclerView() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(null);
        adapter.setExpanded(true); // Show all in this screen
        adapter.setOnTransactionClickListener(transaction -> {
            ReceiptUtils.printTransactionReceipt(this, transaction);
        });
        rvHistory.setAdapter(adapter);
    }

    private void loadData() {
        List<Transaction> transactions = db.appDao().getAllTransactions();
        if (transactions == null || transactions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            adapter.setTransactions(transactions);
        }
    }
}
