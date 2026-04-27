package com.example.pos_app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.Product;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryActivity extends AppCompatActivity {

    private AppDatabase db;
    private ProductAdapter adapter;
    private TabLayout tabLayout;
    private List<Product> allProducts = new ArrayList<>();
    private String currentQuery = "";
    private static final int LOW_STOCK_THRESHOLD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        tabLayout = findViewById(R.id.tab_layout);
        RecyclerView rvInventory = findViewById(R.id.rv_inventory);
        TextInputEditText etSearch = findViewById(R.id.et_search);

        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(new ArrayList<>(), this::showEditProductDialog);
        adapter.setExpanded(true); // Show all in management screen
        rvInventory.setAdapter(adapter);

        findViewById(R.id.fab_add_product).setOnClickListener(v -> showAddProductDialog());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                applyFilter(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    private void loadData() {
        allProducts = db.appDao().getAllProductsByPopularity();
        applyFilter(tabLayout.getSelectedTabPosition());
    }

    private void applyFilter(int position) {
        List<Product> displayList = allProducts.stream()
                .filter(p -> p.name.toLowerCase().contains(currentQuery.toLowerCase()))
                .collect(Collectors.toList());

        if (position == 0) {
            // All Products - sort alphabetically
            Collections.sort(displayList, (p1, p2) -> p1.name.compareToIgnoreCase(p2.name));
        } else if (position == 1) {
            // Top Seller - already sorted by salesCount DESC from DB
            // (No extra filtering needed, displayList is derived from allProducts which is pre-sorted)
        } else if (position == 2) {
            // Low Stock
            displayList = displayList.stream()
                    .filter(p -> p.quantity < LOW_STOCK_THRESHOLD)
                    .collect(Collectors.toList());
        }

        adapter.setProducts(displayList);
    }

    private void showAddProductDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etStock = dialogView.findViewById(R.id.et_stock);

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        String name = etName.getText().toString();
                        double price = Double.parseDouble(etPrice.getText().toString());
                        int stock = Integer.parseInt(etStock.getText().toString());
                        db.appDao().insertProduct(new Product(name, stock, price));
                        loadData();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etStock = dialogView.findViewById(R.id.et_stock);

        etName.setText(product.name);
        etPrice.setText(String.valueOf(product.price));
        etStock.setText(String.valueOf(product.quantity));

        new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    try {
                        product.name = etName.getText().toString();
                        product.price = Double.parseDouble(etPrice.getText().toString());
                        product.quantity = Integer.parseInt(etStock.getText().toString());
                        db.appDao().updateProduct(product);
                        loadData();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    db.appDao().deleteProduct(product);
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
