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

public class CashFlowCalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow_calculator);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupCashFlowCalculator();
    }

    private void setupCashFlowCalculator() {
        TextInputEditText etInflow = findViewById(R.id.et_cash_inflow);
        TextInputEditText etOutflow = findViewById(R.id.et_cash_outflow);
        Button btnCalculate = findViewById(R.id.btn_calculate_cash_flow);
        TextView tvResult = findViewById(R.id.tv_cash_flow_result);

        String currencySymbol = CurrencyUtils.getCurrencySymbol(this);
        if (etInflow != null) etInflow.setHint("Total Inflow (" + currencySymbol + ")");
        if (etOutflow != null) etOutflow.setHint("Total Outflow (" + currencySymbol + ")");

        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                try {
                    String inflowStr = etInflow.getText().toString();
                    String outflowStr = etOutflow.getText().toString();
                    if (inflowStr.isEmpty() || outflowStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double inflow = Double.parseDouble(inflowStr);
                    double outflow = Double.parseDouble(outflowStr);
                    double net = inflow - outflow;
                    tvResult.setText(String.format(Locale.getDefault(), "Net Cash Flow: %s", 
                            CurrencyUtils.formatAmount(this, net)));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
