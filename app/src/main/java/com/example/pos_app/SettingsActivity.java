package com.example.pos_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.pos_app.data.AppDatabase;
import com.example.pos_app.data.UserSetting;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase db;
    private RadioGroup rgTheme;
    private RadioGroup rgTone;
    private boolean isUpdatingUI = true; // Start true to block initial triggers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = AppDatabase.getInstance(this);
        rgTheme = findViewById(R.id.rg_theme);
        rgTone = findViewById(R.id.rg_tone);

        setupListeners();
        loadSettings();
    }

    private void loadSettings() {
        new Thread(() -> {
            String theme = db.appDao().getSetting("theme");
            String tone = db.appDao().getSetting("tone");
            
            runOnUiThread(() -> {
                isUpdatingUI = true;
                
                // Load Theme (Light/Dark)
                if ("light".equals(theme)) {
                    rgTheme.check(R.id.rb_theme_light);
                } else if ("dark".equals(theme)) {
                    rgTheme.check(R.id.rb_theme_dark);
                } else {
                    rgTheme.check(R.id.rb_theme_system);
                }

                // Load Tone (Color Palette)
                if ("blue".equals(tone)) {
                    rgTone.check(R.id.rb_tone_blue);
                } else if ("green".equals(tone)) {
                    rgTone.check(R.id.rb_tone_green);
                } else if ("purple".equals(tone)) {
                    rgTone.check(R.id.rb_tone_purple);
                } else {
                    rgTone.check(R.id.rb_tone_gold);
                }
                
                isUpdatingUI = false;
            });
        }).start();
    }

    private void setupListeners() {
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (isUpdatingUI) return;
            
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

        rgTone.setOnCheckedChangeListener((group, checkedId) -> {
            if (isUpdatingUI) return;

            String toneValue;
            if (checkedId == R.id.rb_tone_blue) {
                toneValue = "blue";
            } else if (checkedId == R.id.rb_tone_green) {
                toneValue = "green";
            } else if (checkedId == R.id.rb_tone_purple) {
                toneValue = "purple";
            } else {
                toneValue = "gold";
            }

            // Update cache immediately to prevent recreation flicker/reversion
            ThemeUtils.setCachedTone(toneValue);
            saveSetting("tone", toneValue);
            
            // Smoothly restart activity to apply new theme without heavy flicker
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(new Intent(this, this.getClass()));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void saveSetting(String key, String value) {
        new Thread(() -> {
            db.appDao().saveSetting(new UserSetting(key, value));
        }).start();
    }
}
