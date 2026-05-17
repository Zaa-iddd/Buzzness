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

public class BreakEvenCalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break_even_calculator);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupBreakEvenCalculator();
    }

    private void setupBreakEvenCalculator() {
        TextInputEditText etFixedCosts = findViewById(R.id.et_fixed_costs);
        TextInputEditText etVariableCost = findViewById(R.id.et_variable_cost);
        TextInputEditText etSellingPrice = findViewById(R.id.et_selling_price_unit);
        Button btnCalculate = findViewById(R.id.btn_calculate_break_even);
        TextView tvResult = findViewById(R.id.tv_break_even_result);

        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                try {
                    String fixedStr = etFixedCosts.getText().toString();
                    String variableStr = etVariableCost.getText().toString();
                    String sellingStr = etSellingPrice.getText().toString();

                    if (fixedStr.isEmpty() || variableStr.isEmpty() || sellingStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double fixedCosts = Double.parseDouble(fixedStr);
                    double variableCost = Double.parseDouble(variableStr);
                    double sellingPrice = Double.parseDouble(sellingStr);

                    if (sellingPrice <= variableCost) {
                        Toast.makeText(this, "Selling price must be greater than variable cost", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double breakEvenUnits = fixedCosts / (sellingPrice - variableCost);
                    
                    tvResult.setText(String.format(Locale.getDefault(), "Break-Even Point: %.2f units", breakEvenUnits));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
