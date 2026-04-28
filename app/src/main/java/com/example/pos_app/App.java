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
        // We use synchronous reads here because this runs once at startup
        // and we need these values BEFORE the first activity starts to prevent flicker.
        String theme = db.appDao().getSetting("theme");
        
        int mode;
        if ("light".equals(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if ("dark".equals(theme)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        
        AppCompatDelegate.setDefaultNightMode(mode);
        
        // Also pre-cache the tone to avoid the first activity hitting the DB
        ThemeUtils.getTone(this);
    }
}
