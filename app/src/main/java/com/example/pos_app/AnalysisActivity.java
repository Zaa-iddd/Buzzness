package com.example.pos_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalysisActivity extends AppCompatActivity {

    private LineChart salesChart;
    private BarChart productChart;
    private AppDatabase db;
    private TextView tvSelectedRange, tvInsight;
    private MaterialButton btnDateRange;
    private ImageButton btnClearFilter;
    private Long startDate = null;
    private Long endDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        db = AppDatabase.getInstance(this);

        salesChart = findViewById(R.id.salesChart);
        productChart = findViewById(R.id.productChart);
        tvSelectedRange = findViewById(R.id.tv_selected_range);
        tvInsight = findViewById(R.id.tv_insight);
        btnDateRange = findViewById(R.id.btn_date_range);
        btnClearFilter = findViewById(R.id.btn_clear_filter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupCharts();
        loadData();

        btnDateRange.setOnClickListener(v -> showDatePicker());
        btnClearFilter.setOnClickListener(v -> {
            startDate = null;
            endDate = null;
            tvSelectedRange.setText("Showing: All time");
            btnClearFilter.setVisibility(View.GONE);
            loadData();
        });
    }

    private void setupCharts() {
        salesChart.getDescription().setEnabled(false);
        salesChart.setDrawGridBackground(false);
        salesChart.getLegend().setEnabled(true);
        salesChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        salesChart.getXAxis().setDrawGridLines(false);
        salesChart.getAxisRight().setEnabled(false);

        productChart.getDescription().setEnabled(false);
        productChart.setDrawGridBackground(false);
        productChart.getLegend().setEnabled(false);
        productChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        productChart.getXAxis().setDrawGridLines(false);
        productChart.getAxisRight().setEnabled(false);
    }

    private void loadData() {
        loadSalesTrend();
        loadProductPerformance();
    }

    private void loadSalesTrend() {
        List<Transaction> transactions = db.appDao().getAllTransactions();
        Collections.reverse(transactions); // Oldest first for trend

        List<Entry> entries = new ArrayList<>();
        double cumulativeBalance = 0;
        int index = 0;

        for (Transaction t : transactions) {
            if (startDate != null && endDate != null) {
                if (t.timestamp < startDate || t.timestamp > endDate) continue;
            }
            cumulativeBalance += t.amount;
            entries.add(new Entry(index++, (float) cumulativeBalance));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cumulative Revenue");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        salesChart.setData(lineData);
        salesChart.animateX(1000);
        salesChart.invalidate();

        if (entries.isEmpty()) {
            tvInsight.setText("No sales data available for the selected period.");
        } else {
            tvInsight.setText(String.format(Locale.getDefault(), 
                "Your total revenue reached %s over %d transactions in this period.", 
                CurrencyUtils.formatAmount(this, cumulativeBalance), entries.size()));
        }
    }

    private void loadProductPerformance() {
        List<Product> products = db.appDao().getAllProductsByPopularity();
        // Limit to top 5
        if (products.size() > 5) {
            products = products.subList(0, 5);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            entries.add(new BarEntry(i, p.salesCount));
            labels.add(p.name);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Units Sold");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        productChart.setData(barData);
        
        XAxis xAxis = productChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setGranularity(1f);

        productChart.animateY(1000);
        productChart.invalidate();
    }

    private void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String range = sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));
            tvSelectedRange.setText("Showing: " + range);
            btnClearFilter.setVisibility(View.VISIBLE);
            
            loadData();
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}
