package com.example.pos_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.UserSetting;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase db;
    private RadioGroup rgTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = AppDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rgTheme = findViewById(R.id.rg_theme);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        new Thread(() -> {
            String theme = db.appDao().getSetting("theme");
            runOnUiThread(() -> {
                if ("light".equals(theme)) {
                    rgTheme.check(R.id.rb_theme_light);
                } else if ("dark".equals(theme)) {
                    rgTheme.check(R.id.rb_theme_dark);
                } else {
                    rgTheme.check(R.id.rb_theme_system);
                }
            });
        }).start();
    }

    private void setupListeners() {
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String themeValue;
            int mode;

            if (checkedId == R.id.rb_theme_light) {
                themeValue = "light";
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rb_theme_dark) {
                themeValue = "dark";
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                themeValue = "system";
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            AppCompatDelegate.setDefaultNightMode(mode);
            saveSetting("theme", themeValue);
        });
    }

    private void saveSetting(String key, String value) {
        new Thread(() -> {
            db.appDao().saveSetting(new UserSetting(key, value));
        }).start();
    }
}
