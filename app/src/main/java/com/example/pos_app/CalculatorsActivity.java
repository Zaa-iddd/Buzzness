package com.example.pos_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class CalculatorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculators);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupNavigation();
    }

    private void setupNavigation() {
        MaterialCardView cvTax = findViewById(R.id.cv_tax_calculator);
        MaterialCardView cvCashFlow = findViewById(R.id.cv_cash_flow_calculator);
        MaterialCardView cvRoi = findViewById(R.id.cv_roi_calculator);
        MaterialCardView cvProfitMargin = findViewById(R.id.cv_profit_margin_calculator);
        MaterialCardView cvBreakEven = findViewById(R.id.cv_break_even_calculator);

        if (cvTax != null) {
            cvTax.setOnClickListener(v -> startActivity(new Intent(this, TaxCalculatorActivity.class)));
        }
        if (cvCashFlow != null) {
            cvCashFlow.setOnClickListener(v -> startActivity(new Intent(this, CashFlowCalculatorActivity.class)));
        }
        if (cvRoi != null) {
            cvRoi.setOnClickListener(v -> startActivity(new Intent(this, ROICalculatorActivity.class)));
        }
        if (cvProfitMargin != null) {
            cvProfitMargin.setOnClickListener(v -> startActivity(new Intent(this, ProfitMarginCalculatorActivity.class)));
        }
        if (cvBreakEven != null) {
            cvBreakEven.setOnClickListener(v -> startActivity(new Intent(this, BreakEvenCalculatorActivity.class)));
        }
    }
}
