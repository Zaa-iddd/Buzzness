package com.example.pos_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryActivity extends AppCompatActivity {

    private AppDatabase db;
    private ProductAdapter adapter;
    private TabLayout tabLayout;
    private RecyclerView rvInventory;
    private List<Product> allProducts = new ArrayList<>();
    private String currentQuery = "";
    private static final int LOW_STOCK_THRESHOLD = 5;

    private EditText currentQrEditText;
    private ImageView currentDialogImageView;
    private String selectedImageUri = null;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null && currentQrEditText != null) {
                    currentQrEditText.setText(result.getContents());
                }
            });

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        // Persistence might fail depending on URI source, but we continue
                    }
                    selectedImageUri = uri.toString();
                    if (currentDialogImageView != null) {
                        currentDialogImageView.setImageURI(uri);
                    }
                }
            }
    );

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
        setContentView(R.layout.activity_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        tabLayout = findViewById(R.id.tab_layout);
        rvInventory = findViewById(R.id.rv_inventory);
        TextInputEditText etSearch = findViewById(R.id.et_search);

        if (rvInventory != null) {
            rvInventory.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ProductAdapter(new ArrayList<>(), this::showEditProductDialog);
            adapter.setExpanded(true); 
            rvInventory.setAdapter(adapter);
        }

        View fabAddProduct = findViewById(R.id.fab_add_product);
        if (fabAddProduct != null) {
            fabAddProduct.setOnClickListener(v -> showAddProductDialog());
        }

        if (tabLayout != null) {
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
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString();
                    if (tabLayout != null) {
                        applyFilter(tabLayout.getSelectedTabPosition());
                    } else {
                        applyFilter(0);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        loadData();
    }

    private void loadData() {
        allProducts = db.appDao().getAllProductsByPopularity();
        if (tabLayout != null) {
            applyFilter(tabLayout.getSelectedTabPosition());
        } else {
            applyFilter(0);
        }
    }

    private void applyFilter(int position) {
        List<Product> displayList = allProducts.stream()
                .filter(p -> p.name != null && p.name.toLowerCase().contains(currentQuery.toLowerCase()))
                .collect(Collectors.toList());

        if (position == 0) {
            Collections.sort(displayList, (p1, p2) -> {
                if (p1.name == null) return 1;
                if (p2.name == null) return -1;
                return p1.name.compareToIgnoreCase(p2.name);
            });
        } else if (position == 2) {
            displayList = displayList.stream()
                    .filter(p -> p.quantity < LOW_STOCK_THRESHOLD)
                    .collect(Collectors.toList());
        }

        if (adapter != null) {
            adapter.setProducts(displayList);
        }
    }

    private void showAddProductDialog() {
        selectedImageUri = null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etStock = dialogView.findViewById(R.id.et_stock);
        EditText etQrCode = dialogView.findViewById(R.id.et_qr_code);
        View btnScan = dialogView.findViewById(R.id.btn_scan_qr);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_product_preview);
        View btnSelectImage = dialogView.findViewById(R.id.fab_select_image);

        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> {
                currentDialogImageView = ivPreview;
                imagePickerLauncher.launch("image/*");
            });
        }

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                currentQrEditText = etQrCode;
                ScanOptions options = new ScanOptions();
                options.setCaptureActivity(CustomScannerActivity.class);
                options.setPrompt("Scan Product QR/Barcode");
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
                        Product product = new Product(name, stock, price);
                        product.qrCode = etQrCode.getText().toString();
                        product.imageUri = selectedImageUri;
                        db.appDao().insertProduct(product);
                        loadData();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        selectedImageUri = product.imageUri;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPrice = dialogView.findViewById(R.id.et_price);
        EditText etStock = dialogView.findViewById(R.id.et_stock);
        EditText etQrCode = dialogView.findViewById(R.id.et_qr_code);
        View btnScan = dialogView.findViewById(R.id.btn_scan_qr);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_product_preview);
        View btnSelectImage = dialogView.findViewById(R.id.fab_select_image);

        if (etName != null) etName.setText(product.name);
        if (etPrice != null) etPrice.setText(String.valueOf(product.price));
        if (etStock != null) etStock.setText(String.valueOf(product.quantity));
        if (etQrCode != null) etQrCode.setText(product.qrCode);
        
        if (ivPreview != null && product.imageUri != null && !product.imageUri.isEmpty()) {
            ivPreview.setImageURI(Uri.parse(product.imageUri));
        }

        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> {
                currentDialogImageView = ivPreview;
                imagePickerLauncher.launch("image/*");
            });
        }

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                currentQrEditText = etQrCode;
                ScanOptions options = new ScanOptions();
                options.setCaptureActivity(CustomScannerActivity.class);
                options.setPrompt("Scan Product QR/Barcode");
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
                        product.imageUri = selectedImageUri;
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
