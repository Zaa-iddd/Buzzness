package com.example.pos_app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.pos_app.data.AppDatabase;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        applySavedTheme();
    }

    private void applySavedTheme() {
        AppDatabase db = AppDatabase.getInstance(this);
        new Thread(() -> {
            String theme = db.appDao().getSetting("theme");
            int mode;
            if ("light".equals(theme)) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if ("dark".equals(theme)) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            // Use runOnUiThread-like behavior or just call it if it's thread safe for static
            // AppCompatDelegate.setDefaultNightMode is thread-safe for the global setting
            AppCompatDelegate.setDefaultNightMode(mode);
        }).start();
    }
}
