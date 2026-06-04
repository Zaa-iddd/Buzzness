package com.example.pos_app;

import android.content.Context;
import com.example.pos_app.data.AppDatabase;

public class CurrencyUtils {

    private static String cachedCurrency = null;

    public static String getCurrencySymbol(Context context) {
        String currency = getCurrencyCode(context);
        switch (currency) {
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "JPY":
                return "¥";
            case "PHP":
                return "₱";
            case "USD":
            default:
                return "$";
        }
    }

    public static String getCurrencyCode(Context context) {
        if (cachedCurrency == null) {
            AppDatabase db = AppDatabase.getInstance(context);
            cachedCurrency = db.appDao().getSetting("currency");
            if (cachedCurrency == null) cachedCurrency = "USD";
        }
        return cachedCurrency;
    }

    public static void setCachedCurrency(String currency) {
        cachedCurrency = currency;
    }

    public static String formatAmount(Context context, double amount) {
        return String.format(java.util.Locale.getDefault(), "%s%.2f", getCurrencySymbol(context), amount);
    }
}
