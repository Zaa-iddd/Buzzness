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

public class ProfitMarginCalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_margin_calculator);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupProfitCalculator();
    }

    private void setupProfitCalculator() {
        TextInputEditText etCostPrice = findViewById(R.id.et_cost_price);
        TextInputEditText etSellingPrice = findViewById(R.id.et_selling_price);
        Button btnCalculate = findViewById(R.id.btn_calculate_profit);
        TextView tvResult = findViewById(R.id.tv_profit_result);

        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                try {
                    String costStr = etCostPrice.getText().toString();
                    String sellingStr = etSellingPrice.getText().toString();
                    if (costStr.isEmpty() || sellingStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double cost = Double.parseDouble(costStr);
                    double selling = Double.parseDouble(sellingStr);
                    
                    double profit = selling - cost;
                    double margin = (selling != 0) ? (profit / selling) * 100.0 : 0;
                    
                    tvResult.setText(String.format(Locale.getDefault(), "Profit: $%.2f | Margin: %.2f%%", profit, margin));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
