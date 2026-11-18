package xyz.yoav.materialclock;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Settings storage, one SharedPreferences file per widget instance.
 * Up to 1.0.4 every widget shared a single "sp_main" file. After an upgrade its values are copied
 * into each existing widget's file, so those widgets keep their look, and the old file is deleted.
 */
final class WidgetPrefs {

    // Keys match app:key in res/xml/root_preferences.xml.
    static final String FONT = "sp_font";
    static final String LAYOUT = "sp_layout";
    static final String COLOR_MODE = "sp_color_mode";
    static final String SHOW_TIME = "sp_show_time";
    static final String TIME_COLOR = "sp_clock_color";
    static final String TIME_FORMAT = "sp_time_format";
    static final String TIME_SIZE = "sp_time_size";
    static final String TIME_ALIGN = "sp_time_align";
    static final String SHOW_DATE = "sp_show_date";
    static final String DATE_COLOR = "sp_date_color";
    static final String DATE_FORMAT = "sp_date_format";
    static final String DATE_SIZE = "sp_date_size";
    static final String DATE_ALIGN = "sp_date_align";

    private static final String LEGACY_FILE = "sp_main";
    private static final String INITIALIZED = "initialized_v2";

    private WidgetPrefs() {
    }

    static String fileName(int appWidgetId) {
        return "widget_" + appWidgetId;
    }

    static SharedPreferences get(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(fileName(appWidgetId), Context.MODE_PRIVATE);
        if (!prefs.getBoolean(INITIALIZED, false)) {
            SharedPreferences.Editor editor = prefs.edit();
            copyLegacySettings(context, editor);
            editor.putBoolean(INITIALIZED, true).apply();
        }
        return prefs;
    }

    /** Copies the pre-1.1 shared settings into every existing widget, then drops them. */
    static void migrateLegacy(Context context, int[] existingWidgetIds) {
        SharedPreferences legacy = context.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE);
        if (legacy.getAll().isEmpty()) return;
        for (int id : existingWidgetIds) {
            get(context, id);
        }
        legacy.edit().clear().commit();
        context.deleteSharedPreferences(LEGACY_FILE);
    }

    static void delete(Context context, int appWidgetId) {
        context.deleteSharedPreferences(fileName(appWidgetId));
    }

    /** Called after a backup restore, when the launcher hands out new widget ids. */
    static void move(Context context, int oldId, int newId) {
        SharedPreferences from = context.getSharedPreferences(fileName(oldId), Context.MODE_PRIVATE);
        SharedPreferences.Editor to = context.getSharedPreferences(fileName(newId), Context.MODE_PRIVATE).edit();
        for (Map.Entry<String, ?> e : from.getAll().entrySet()) {
            put(to, e.getKey(), e.getValue());
        }
        to.apply();
        delete(context, oldId);
    }

    private static void copyLegacySettings(Context context, SharedPreferences.Editor editor) {
        Map<String, ?> legacy = context.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> e : legacy.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (FONT.equals(key)) {
                editor.putString(FONT, mapLegacyFont(String.valueOf(value)));
            } else if (TIME_ALIGN.equals(key) || DATE_ALIGN.equals(key) || LAYOUT.equals(key)) {
                // Stored as int before; ListPreference needs a String.
                editor.putString(key, String.valueOf(value));
            } else {
                put(editor, key, value);
            }
        }
    }

    /** Bundled fonts were removed in 1.1.0; pick the closest system font. */
    static String mapLegacyFont(String font) {
        switch (font) {
            case "latothin":
                return "thin";
            case "latolight":
            case "ubuntulight":
            case "rubik":
                return "light";
            case "archivoblack":
            case "bungeeshade":
                return "black";
            case "coda":
                return "condensed";
            case "warnes":
            case "arizonia":
                return "cursive";
            case "handlee":
            case "jollylodger":
                return "casual";
            default: // latoreg, notosans, imprima, default
                return "default";
        }
    }

    private static void put(SharedPreferences.Editor editor, String key, Object value) {
        if (value instanceof Integer) editor.putInt(key, (Integer) value);
        else if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
        else if (value instanceof String) editor.putString(key, (String) value);
        else if (value instanceof Long) editor.putLong(key, (Long) value);
        else if (value instanceof Float) editor.putFloat(key, (Float) value);
    }

    // Readers that tolerate either int or String storage.

    static int getInt(Map<String, ?> all, String key, int def) {
        Object v = all.get(key);
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof String) {
            try {
                return Integer.parseInt((String) v);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    static boolean getBoolean(Map<String, ?> all, String key, boolean def) {
        Object v = all.get(key);
        return v instanceof Boolean ? (Boolean) v : def;
    }

    static String getString(Map<String, ?> all, String key, String def) {
        Object v = all.get(key);
        return v instanceof String ? (String) v : def;
    }
}
