package com.example.pos_app;

import android.app.Activity;
import android.content.Context;
import com.example.pos_app.data.AppDatabase;

public class ThemeUtils {

    private static String cachedTone = null;
    private static final String KEY_APPLIED_TONE = "applied_tone_tag";

    /**
     * Applies the user-selected tone (color palette) to the activity.
     * MUST be called before super.onCreate().
     */
    public static void applyTheme(Activity activity) {
        String tone = getTone(activity);

        if ("blue".equals(tone)) {
            activity.setTheme(R.style.Theme_POS_App_Blue);
        } else if ("green".equals(tone)) {
            activity.setTheme(R.style.Theme_POS_App_Green);
        } else if ("purple".equals(tone)) {
            activity.setTheme(R.style.Theme_POS_App_Purple);
        } else {
            activity.setTheme(R.style.Theme_POS_App_Gold);
        }
        
        // Tag the activity with the tone that was applied
        activity.getIntent().putExtra(KEY_APPLIED_TONE, tone);
    }

    /**
     * Checks if the tone has changed since the activity was created.
     */
    public static boolean isThemeChanged(Activity activity) {
        String appliedTone = activity.getIntent().getStringExtra(KEY_APPLIED_TONE);
        String currentTone = getTone(activity);
        return appliedTone != null && !appliedTone.equals(currentTone);
    }

    public static String getTone(Context context) {
        if (cachedTone == null) {
            AppDatabase db = AppDatabase.getInstance(context);
            cachedTone = db.appDao().getSetting("tone");
            if (cachedTone == null) cachedTone = "gold";
        }
        return cachedTone;
    }

    public static void setCachedTone(String tone) {
        cachedTone = tone;
    }
}
