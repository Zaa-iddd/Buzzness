package com.example.pos_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class TaxCalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax_calculator);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupTaxCalculator();
    }

    private void setupTaxCalculator() {
        TextInputEditText etAmount = findViewById(R.id.et_tax_amount);
        TextInputEditText etRate = findViewById(R.id.et_tax_rate);
        Button btnCalculate = findViewById(R.id.btn_calculate_tax);
        TextView tvResult = findViewById(R.id.tv_tax_result);

        if (etAmount != null) {
            etAmount.setHint("Amount (" + CurrencyUtils.getCurrencySymbol(this) + ")");
        }

        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                try {
                    String amountStr = etAmount.getText().toString();
                    String rateStr = etRate.getText().toString();
                    if (amountStr.isEmpty() || rateStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount = Double.parseDouble(amountStr);
                    double rate = Double.parseDouble(rateStr);
                    double tax = amount * (rate / 100.0);
                    double total = amount + tax;
                    tvResult.setText(String.format(Locale.getDefault(), "Tax: %s | Total: %s", 
                            CurrencyUtils.formatAmount(this, tax), 
                            CurrencyUtils.formatAmount(this, total)));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
