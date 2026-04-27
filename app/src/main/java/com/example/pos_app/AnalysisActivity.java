package com.example.pos_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;

import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.Product;
import com.example.pos_app.data.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalysisActivity extends AppCompatActivity {

    private AppDatabase db;
    private LineChart salesChart;
    private BarChart productChart;
    private TextView tvInsight;
    private TextView tvSelectedRange;
    private MaterialButton btnDateRange;
    private ImageButton btnClearFilter;

    private Long startDate = null;
    private Long endDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        salesChart = findViewById(R.id.salesChart);
        productChart = findViewById(R.id.productChart);
        tvInsight = findViewById(R.id.tv_insight);
        tvSelectedRange = findViewById(R.id.tv_selected_range);
        btnDateRange = findViewById(R.id.btn_date_range);
        btnClearFilter = findViewById(R.id.btn_clear_filter);

        btnDateRange.setOnClickListener(v -> showDateRangePicker());
        btnClearFilter.setOnClickListener(v -> {
            startDate = null;
            endDate = null;
            updateUI();
        });

        setupCharts();
        loadData();
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            updateUI();
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateUI() {
        if (startDate != null && endDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String rangeText = "Showing: " + sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));
            tvSelectedRange.setText(rangeText);
            btnClearFilter.setVisibility(View.VISIBLE);
        } else {
            tvSelectedRange.setText("Showing: All time");
            btnClearFilter.setVisibility(View.GONE);
        }
        loadData();
    }

    private void setupCharts() {
        // Line Chart setup
        salesChart.getDescription().setEnabled(false);
        salesChart.setDrawGridBackground(false);
        salesChart.getLegend().setEnabled(false);
        
        XAxis xAxis = salesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Sale " + (int)value;
            }
        });

        salesChart.getAxisRight().setEnabled(false);
        salesChart.getAxisLeft().setDrawGridLines(true);

        // Bar Chart setup
        productChart.getDescription().setEnabled(false);
        productChart.setDrawGridBackground(false);
        productChart.getLegend().setEnabled(false);

        XAxis pXAxis = productChart.getXAxis();
        pXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        pXAxis.setDrawGridLines(false);
        pXAxis.setGranularity(1f);
        pXAxis.setLabelRotationAngle(-45);

        productChart.getAxisRight().setEnabled(false);
        productChart.getAxisLeft().setDrawGridLines(true);
        productChart.getAxisLeft().setAxisMinimum(0f);
    }

    private void loadData() {
        List<Transaction> transactions = db.appDao().getAllTransactions();
        
        // Filter transactions by date if range is selected
        List<Transaction> filteredTransactions = new ArrayList<>();
        if (startDate != null && endDate != null) {
            long adjustedEnd = endDate + 24 * 60 * 60 * 1000 - 1; 
            for (Transaction t : transactions) {
                if (t.timestamp >= startDate && t.timestamp <= adjustedEnd) {
                    filteredTransactions.add(t);
                }
            }
        } else {
            filteredTransactions.addAll(transactions);
        }

        if (filteredTransactions.isEmpty()) {
            salesChart.clear();
            productChart.clear();
            tvInsight.setText("No sales data available for the selected period.");
            return;
        }

        // 1. Balance Trend Line Chart
        List<Entry> entries = new ArrayList<>();
        List<Transaction> sortedTransactions = new ArrayList<>(filteredTransactions);
        Collections.reverse(sortedTransactions);

        float cumulativeBalance = 0f;
        for (int i = 0; i < sortedTransactions.size(); i++) {
            cumulativeBalance += (float) sortedTransactions.get(i).amount;
            entries.add(new Entry(i + 1, cumulativeBalance));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Balance Trend");
        dataSet.setColor(Color.parseColor("#9C4300"));
        dataSet.setCircleColor(Color.parseColor("#9C4300"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFDBC9"));

        salesChart.setData(new LineData(dataSet));
        salesChart.invalidate();
        salesChart.animateX(800);

        // 2. Product Performance Bar Chart
        List<Product> products = db.appDao().getAllProductsByPopularity();
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < Math.min(products.size(), 10); i++) {
            Product p = products.get(i);
            barEntries.add(new BarEntry(i, p.salesCount));
            labels.add(p.name);
        }

        productChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        BarDataSet barDataSet = new BarDataSet(barEntries, "Product Sales");
        barDataSet.setColors(new int[]{Color.parseColor("#6750A4"), Color.parseColor("#9C4300"), Color.parseColor("#386A20")});
        barDataSet.setValueTextSize(12f);

        productChart.setData(new BarData(barDataSet));
        productChart.setFitBars(true);
        productChart.invalidate();
        productChart.animateY(1000);
        
        tvInsight.setText(String.format("You have recorded %d sales in this period. Your top performing product is '%s' with %d sales.", 
                filteredTransactions.size(), 
                !products.isEmpty() ? products.get(0).name : "N/A",
                !products.isEmpty() ? products.get(0).salesCount : 0));
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