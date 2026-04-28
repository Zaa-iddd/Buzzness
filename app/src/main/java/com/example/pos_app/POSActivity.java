package com.example.pos_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.CartItem;
import com.example.pos_app.data.Product;
import com.example.pos_app.data.Transaction;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class POSActivity extends AppCompatActivity {

    private AppDatabase db;
    private List<Product> allProducts;
    private List<CartItem> cartItems = new ArrayList<>();
    private ProductAdapter productAdapter;
    private CartAdapter cartAdapter;
    
    private TextView tvSubtotal;
    private TextView tvTotal;
    private MaterialSwitch swDiscount;
    private TextInputLayout tilDiscount;
    private TextInputEditText etDiscount;
    private TextInputEditText etAmountPaid;
    
    private double subtotal = 0.0;
    private double totalAmount = 0.0;
    private double discountPercent = 0.0;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    handleScanResult(result.getContents());
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
        setContentView(R.layout.activity_pos);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        if (appBarLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        db = AppDatabase.getInstance(this);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTotal = findViewById(R.id.tv_total);
        swDiscount = findViewById(R.id.switch_discount);
        tilDiscount = findViewById(R.id.til_discount);
        etDiscount = findViewById(R.id.et_discount);
        etAmountPaid = findViewById(R.id.et_amount_paid);

        setupRecyclerViews();
        setupSearch();
        setupDiscountLogic();
        loadProducts();

        findViewById(R.id.btn_checkout).setOnClickListener(v -> handleCheckout());
        findViewById(R.id.btn_qr_scan).setOnClickListener(v -> startScanning());
    }

    private void setupDiscountLogic() {
        swDiscount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tilDiscount.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                etDiscount.setText("");
                discountPercent = 0;
            }
            updateCart();
        });

        etDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    discountPercent = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                    if (discountPercent > 100) discountPercent = 100;
                } catch (NumberFormatException e) {
                    discountPercent = 0;
                }
                updateCart();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan a product QR code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        options.setCaptureActivity(CustomScannerActivity.class);
        barcodeLauncher.launch(options);
    }

    private void handleScanResult(String contents) {
        try {
            int productId = Integer.parseInt(contents);
            Product foundProduct = null;
            for (Product p : allProducts) {
                if (p.id == productId) {
                    foundProduct = p;
                    break;
                }
            }

            if (foundProduct != null) {
                addToCart(foundProduct);
                Toast.makeText(this, "Added: " + foundProduct.name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Product foundProduct = null;
            for (Product p : allProducts) {
                if (p.name.equalsIgnoreCase(contents) || (p.qrCode != null && p.qrCode.equals(contents))) {
                    foundProduct = p;
                    break;
                }
            }
            if (foundProduct != null) {
                addToCart(foundProduct);
            } else {
                Toast.makeText(this, "Invalid QR content", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerViews() {
        RecyclerView rvProducts = findViewById(R.id.rv_products);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(new ArrayList<>(), this::addToCart);
        productAdapter.setExpanded(true);
        rvProducts.setAdapter(productAdapter);

        RecyclerView rvCart = findViewById(R.id.rv_cart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems, this::removeFromCart);
        rvCart.setAdapter(cartAdapter);
    }

    private void setupSearch() {
        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts() {
        allProducts = db.appDao().getAllProductsByPopularity();
        productAdapter.setProducts(allProducts);
    }

    private void filterProducts(String query) {
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.name.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        productAdapter.setProducts(filtered);
    }

    private void addToCart(Product product) {
        if (product.quantity <= 0) {
            Toast.makeText(this, "Out of stock!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.product.id == product.id) {
                if (item.quantity < product.quantity) {
                    item.quantity++;
                    found = true;
                } else {
                    Toast.makeText(this, "Not enough stock!", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            }
        }

        if (!found) {
            cartItems.add(new CartItem(product, 1));
        }

        updateCart();
    }

    private void removeFromCart(int position) {
        cartItems.remove(position);
        updateCart();
    }

    private void updateCart() {
        cartAdapter.notifyDataSetChanged();
        subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        
        double discount = subtotal * (discountPercent / 100.0);
        totalAmount = subtotal - discount;

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", totalAmount));
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String paidText = etAmountPaid.getText().toString();
        if (paidText.isEmpty()) {
            Toast.makeText(this, "Please enter amount paid", Toast.LENGTH_SHORT).show();
            return;
        }

        double amountPaid;
        try {
            amountPaid = Double.parseDouble(paidText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount paid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountPaid < totalAmount) {
            Toast.makeText(this, "Paid amount is less than total", Toast.LENGTH_SHORT).show();
            return;
        }

        final double change = amountPaid - totalAmount;
        final List<CartItem> itemsToPrint = new ArrayList<>(cartItems);
        final double finalSubtotal = subtotal;
        final double finalDiscountPercent = discountPercent;
        final double finalTotal = totalAmount;
        final double finalPaid = amountPaid;

        StringBuilder itemsDescription = new StringBuilder();
        for (CartItem item : cartItems) {
            Product p = item.product;
            p.quantity -= item.quantity;
            p.salesCount += item.quantity;
            db.appDao().updateProduct(p);
            itemsDescription.append(item.product.name).append(" x").append(item.quantity).append(", ");
        }

        String desc = "POS Sale: " + itemsDescription.toString();
        if (finalDiscountPercent > 0) {
            desc += String.format(Locale.getDefault(), " (Discount %.0f%%)", finalDiscountPercent);
        }

        Transaction transaction = new Transaction(
                desc,
                totalAmount,
                System.currentTimeMillis(),
                ""
        );
        db.appDao().insertTransaction(transaction);

        printReceipt(itemsToPrint, finalSubtotal, finalDiscountPercent, finalTotal, finalPaid, change);

        cartItems.clear();
        etAmountPaid.setText("");
        updateCart();
        loadProducts();
        Toast.makeText(this, "Transaction Complete. Change: " + String.format(Locale.getDefault(), "$%.2f", change), Toast.LENGTH_LONG).show();
    }

    private void printReceipt(final List<CartItem> items, final double subtotal, final double discountPercent, final double total, final double paid, final double change) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Receipt";

        printManager.print(jobName, new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }

                PrintDocumentInfo info = new PrintDocumentInfo.Builder("receipt.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                
                int y = 40;
                paint.setTextSize(18f);
                paint.setFakeBoldText(true);
                canvas.drawText("STORE RECEIPT", 80, y, paint);
                
                y += 30;
                paint.setTextSize(12f);
                paint.setFakeBoldText(false);
                canvas.drawText("Date: " + new java.util.Date().toString(), 20, y, paint);
                
                y += 20;
                canvas.drawLine(20, y, 280, y, paint);
                
                y += 20;
                for (CartItem item : items) {
                    canvas.drawText(item.product.name + " x" + item.quantity, 20, y, paint);
                    canvas.drawText(String.format(Locale.getDefault(), "$%.2f", item.getTotalPrice()), 220, y, paint);
                    y += 20;
                }
                
                y += 10;
                canvas.drawLine(20, y, 280, y, paint);
                
                if (discountPercent > 0) {
                    y += 20;
                    canvas.drawText("Subtotal:", 20, y, paint);
                    canvas.drawText(String.format(Locale.getDefault(), "$%.2f", subtotal), 210, y, paint);
                    
                    y += 20;
                    canvas.drawText(String.format(Locale.getDefault(), "Discount (%.0f%%):", discountPercent), 20, y, paint);
                    canvas.drawText(String.format(Locale.getDefault(), "-$%.2f", subtotal * (discountPercent/100.0)), 210, y, paint);
                }

                y += 20;
                paint.setFakeBoldText(true);
                paint.setTextSize(14f);
                canvas.drawText("TOTAL:", 20, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "$%.2f", total), 210, y, paint);
                
                y += 25;
                paint.setFakeBoldText(false);
                paint.setTextSize(12f);
                canvas.drawText("Paid:", 20, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "$%.2f", paid), 210, y, paint);
                
                y += 20;
                canvas.drawText("Change:", 20, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "$%.2f", change), 210, y, paint);
                
                y += 40;
                paint.setTextSize(10f);
                paint.setFakeBoldText(false);
                canvas.drawText("Thank you for your business!", 60, y, paint);

                pdfDocument.finishPage(page);

                try {
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
                } finally {
                    pdfDocument.close();
                }
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }, null);
    }
}
