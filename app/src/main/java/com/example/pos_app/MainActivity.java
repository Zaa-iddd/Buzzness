package com.example.pos_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.Product;
import com.example.pos_app.data.Transaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private ProductAdapter inventoryAdapter;
    private TransactionAdapter transactionAdapter;
    
    private TextView tvBalance;
    private TextView tvSalesCount;
    private TextView tvProductCount;
    private TextView tvLowStock;
    
    private Button btnViewAllTransactions;
    private Button btnViewAllInventory;
    private boolean isHistoryExpanded = false;

    private DrawerLayout drawerLayout;
    private EditText currentQrEditText;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null && currentQrEditText != null) {
                    currentQrEditText.setText(result.getContents());
                }
            });

    @Override
    protected void onRestart() {
        super.onRestart();
        if (ThemeUtils.isThemeChanged(this)) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        drawerLayout = findViewById(R.id.drawer_layout);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (navigationView != null) {
            setupNavigation(navigationView);
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }

        db = AppDatabase.getInstance(this);
        
        tvBalance = findViewById(R.id.tv_balance);
        tvSalesCount = findViewById(R.id.tv_sales_count);
        tvProductCount = findViewById(R.id.tv_product_count);
        tvLowStock = findViewById(R.id.tv_low_stock);
        btnViewAllTransactions = findViewById(R.id.btn_view_all_transactions);
        btnViewAllInventory = findViewById(R.id.btn_view_all_inventory);
        
        setupRecyclerViews();
        setupButtons();
        applyEntranceAnimations();
    }

    private void applyEntranceAnimations() {
        View mainContent = findViewById(R.id.main_linear_layout);
        if (mainContent != null) {
            mainContent.setAlpha(0f);
            mainContent.animate().alpha(1f).setDuration(500).start();
        }
    }

    private void setupNavigation(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                // Already here
            } else if (id == R.id.nav_pos) {
                startActivity(new Intent(this, POSActivity.class));
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryActivity.class));
            } else if (id == R.id.nav_analysis) {
                startActivity(new Intent(this, AnalysisActivity.class));
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void setupRecyclerViews() {
        RecyclerView rvInventory = findViewById(R.id.rv_inventory);
        if (rvInventory != null) {
            rvInventory.setLayoutManager(new LinearLayoutManager(this));
            inventoryAdapter = new ProductAdapter(new ArrayList<>(), this::showEditProductDialog);
            rvInventory.setAdapter(inventoryAdapter);
        }

        RecyclerView rvTrans = findViewById(R.id.rv_transactions);
        if (rvTrans != null) {
            rvTrans.setLayoutManager(new LinearLayoutManager(this));
            transactionAdapter = new TransactionAdapter(new ArrayList<>());
            transactionAdapter.setOnTransactionClickListener(transaction -> {
                ReceiptUtils.printTransactionReceipt(this, transaction);
            });
            rvTrans.setAdapter(transactionAdapter);
        }
    }

    private void setupButtons() {
        View btnAddTrans = findViewById(R.id.btn_add_transaction);
        if (btnAddTrans != null) {
            btnAddTrans.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, POSActivity.class);
                startActivity(intent);
            });
        }
        
        View btnAddInventory = findViewById(R.id.btn_add_inventory);
        if (btnAddInventory != null) {
            btnAddInventory.setOnClickListener(v -> showAddProductDialog());
        }
        
        View cvBalance = findViewById(R.id.cv_balance);
        if (cvBalance != null) {
            cvBalance.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
                startActivity(intent);
            });
        }

        View cvInventorySummary = findViewById(R.id.cv_inventory_summary);
        if (cvInventorySummary != null) {
            cvInventorySummary.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnViewAllTransactions != null) {
            btnViewAllTransactions.setOnClickListener(v -> {
                isHistoryExpanded = !isHistoryExpanded;
                if (transactionAdapter != null) {
                    transactionAdapter.setExpanded(isHistoryExpanded);
                }
                btnViewAllTransactions.setText(isHistoryExpanded ? "Show Less" : "View All");
            });
        }

        if (btnViewAllInventory != null) {
            btnViewAllInventory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                startActivity(intent);
            });
        }
    }

    private void refreshData() {
        double balance = db.appDao().getTotalBalance();
        int salesCount = db.appDao().getTransactionCount();
        int productCount = db.appDao().getProductCount();
        int lowStockCount = db.appDao().getLowStockCount(5);

        if (tvBalance != null) tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", balance));
        if (tvSalesCount != null) tvSalesCount.setText(String.valueOf(salesCount));
        if (tvProductCount != null) tvProductCount.setText(String.valueOf(productCount));
        if (tvLowStock != null) tvLowStock.setText(String.valueOf(lowStockCount));

        List<Product> products = db.appDao().getAllProductsByPopularity();
        if (inventoryAdapter != null) {
            inventoryAdapter.setProducts(products);
        }
        if (btnViewAllInventory != null) {
            if (products.size() > 5) {
                btnViewAllInventory.setVisibility(View.VISIBLE);
            } else {
                btnViewAllInventory.setVisibility(View.GONE);
                if (inventoryAdapter != null) {
                    inventoryAdapter.setExpanded(true);
                }
            }
        }
        
        List<Transaction> transactions = db.appDao().getAllTransactions();
        if (transactionAdapter != null) {
            transactionAdapter.setTransactions(transactions);
        }
        
        if (btnViewAllTransactions != null) {
            if (transactions.size() > 5) {
                btnViewAllTransactions.setVisibility(View.VISIBLE);
                btnViewAllTransactions.setText(isHistoryExpanded ? "Show Less" : "View All");
            } else {
                btnViewAllTransactions.setVisibility(View.GONE);
                if (transactionAdapter != null) {
                    transactionAdapter.setExpanded(true);
                }
            }
        }
    }

    private void showAddProductDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etStock = dialogView.findViewById(R.id.et_stock);
        EditText etQrCode = dialogView.findViewById(R.id.et_qr_code);
        Button btnScan = dialogView.findViewById(R.id.btn_scan_qr);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                currentQrEditText = etQrCode;
                ScanOptions options = new ScanOptions();
                options.setCaptureActivity(CustomScannerActivity.class);
                options.setPrompt("Scan Product QR/Barcode");
                options.setBeepEnabled(true);
                barcodeLauncher.launch(options);
            });
        }

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        String name = etName.getText().toString();
                        double price = Double.parseDouble(etPrice.getText().toString());
                        int stock = Integer.parseInt(etStock.getText().toString());
                        String qr = etQrCode.getText().toString();
                        Product product = new Product(name, stock, price);
                        product.qrCode = qr;
                        db.appDao().insertProduct(product);
                        refreshData();
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
        EditText etQrCode = dialogView.findViewById(R.id.et_qr_code);
        Button btnScan = dialogView.findViewById(R.id.btn_scan_qr);

        if (etName != null) etName.setText(product.name);
        if (etPrice != null) etPrice.setText(String.valueOf(product.price));
        if (etStock != null) etStock.setText(String.valueOf(product.quantity));
        if (etQrCode != null) etQrCode.setText(product.qrCode);

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                currentQrEditText = etQrCode;
                ScanOptions options = new ScanOptions();
                options.setCaptureActivity(CustomScannerActivity.class);
                options.setPrompt("Scan Product QR/Barcode");
                options.setBeepEnabled(true);
                barcodeLauncher.launch(options);
            });
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    try {
                        product.name = etName.getText().toString();
                        product.price = Double.parseDouble(etPrice.getText().toString());
                        product.quantity = Integer.parseInt(etStock.getText().toString());
                        product.qrCode = etQrCode.getText().toString();
                        db.appDao().updateProduct(product);
                        refreshData();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    db.appDao().deleteProduct(product);
                    refreshData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
