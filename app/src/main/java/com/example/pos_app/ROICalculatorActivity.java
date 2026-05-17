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

public class ROICalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roi_calculator);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        setupROICalculator();
    }

    private void setupROICalculator() {
        TextInputEditText etInvestment = findViewById(R.id.et_roi_investment);
        TextInputEditText etReturn = findViewById(R.id.et_roi_return);
        Button btnCalculate = findViewById(R.id.btn_calculate_roi);
        TextView tvResult = findViewById(R.id.tv_roi_result);

        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                try {
                    String investmentStr = etInvestment.getText().toString();
                    String returnStr = etReturn.getText().toString();
                    if (investmentStr.isEmpty() || returnStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double investment = Double.parseDouble(investmentStr);
                    double returnAmt = Double.parseDouble(returnStr);
                    if (investment == 0) {
                        Toast.makeText(this, "Investment cannot be zero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double roi = ((returnAmt - investment) / investment) * 100.0;
                    tvResult.setText(String.format(Locale.getDefault(), "ROI: %.2f%%", roi));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
