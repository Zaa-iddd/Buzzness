package com.example.pos_app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.Product;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = AppDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rv_notifications);
        tvEmpty = findViewById(R.id.tv_empty_notifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        loadNotifications();
    }

    private void loadNotifications() {
        new Thread(() -> {
            List<Product> allProducts = db.appDao().getAllProductsByPopularity();
            List<Product> alerts = allProducts.stream()
                    .filter(p -> p.quantity <= 5)
                    .collect(Collectors.toList());

            runOnUiThread(() -> {
                if (alerts.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvNotifications.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvNotifications.setVisibility(View.VISIBLE);
                    adapter = new NotificationAdapter(alerts);
                    rvNotifications.setAdapter(adapter);
                }
            });
        }).start();
    }
}
