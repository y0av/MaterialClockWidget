package xyz.yoav.materialclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Map;

/** Builds the widget's RemoteViews from one widget's settings. */
final class WidgetViewCreator {

    // Must match res/values/arrays.xml widget_layout_values.
    static final int VERTICAL = 0, VERTICAL_FLIPPED = 1, HORIZONTAL = 2, HORIZONTAL_FLIPPED = 3;

    static final String COLOR_CUSTOM = "custom";
    static final String COLOR_DYNAMIC_LIGHT = "dynamic_light";
    static final String COLOR_DYNAMIC_DARK = "dynamic_dark";

    private final Context context;
    private final String font;
    private final int orientation;
    private final String colorMode;
    private final boolean showTime, showDate;
    private final int timeColor, dateColor;
    private final String timeFormat, dateFormat;
    private final int timeSize, dateSize;
    private final int timeAlign, dateAlign;

    WidgetViewCreator(Context context, SharedPreferences prefs) {
        this.context = context;
        // Defaults must match res/xml/root_preferences.xml.
        Map<String, ?> all = prefs.getAll();
        font = WidgetPrefs.getString(all, WidgetPrefs.FONT, "default");
        orientation = WidgetPrefs.getInt(all, WidgetPrefs.LAYOUT, VERTICAL);
        colorMode = WidgetPrefs.getString(all, WidgetPrefs.COLOR_MODE, COLOR_CUSTOM);
        showTime = WidgetPrefs.getBoolean(all, WidgetPrefs.SHOW_TIME, true);
        showDate = WidgetPrefs.getBoolean(all, WidgetPrefs.SHOW_DATE, true);
        timeColor = WidgetPrefs.getInt(all, WidgetPrefs.TIME_COLOR, Color.WHITE);
        dateColor = WidgetPrefs.getInt(all, WidgetPrefs.DATE_COLOR, Color.WHITE);
        timeFormat = WidgetPrefs.getString(all, WidgetPrefs.TIME_FORMAT, "");
        dateFormat = WidgetPrefs.getString(all, WidgetPrefs.DATE_FORMAT, "EEE, MMM d");
        timeSize = WidgetPrefs.getInt(all, WidgetPrefs.TIME_SIZE, 42);
        dateSize = WidgetPrefs.getInt(all, WidgetPrefs.DATE_SIZE, 22);
        timeAlign = WidgetPrefs.getInt(all, WidgetPrefs.TIME_ALIGN, Gravity.CENTER);
        dateAlign = WidgetPrefs.getInt(all, WidgetPrefs.DATE_ALIGN, Gravity.CENTER);
    }

    private int getLayoutResource() {
        switch (font) {
            case "thin": return R.layout.widget_layout_thin;
            case "light": return R.layout.widget_layout_light;
            case "medium": return R.layout.widget_layout_medium;
            case "black": return R.layout.widget_layout_black;
            case "condensedlight": return R.layout.widget_layout_condensedlight;
            case "condensed": return R.layout.widget_layout_condensed;
            case "serif": return R.layout.widget_layout_serif;
            case "monospace": return R.layout.widget_layout_monospace;
            case "typewriter": return R.layout.widget_layout_typewriter;
            case "casual": return R.layout.widget_layout_casual;
            case "cursive": return R.layout.widget_layout_cursive;
            case "smallcaps": return R.layout.widget_layout_smallcaps;
            default: return R.layout.widget_layout_default;
        }
    }

    private int getTimeView() {
        switch (orientation) {
            case HORIZONTAL: return R.id.clockLeft;
            case HORIZONTAL_FLIPPED: return R.id.clockRight;
            case VERTICAL_FLIPPED: return alignedView(timeAlign, R.id.dateLeft, R.id.date, R.id.dateRight);
            default: return alignedView(timeAlign, R.id.clockLeft, R.id.clock, R.id.clockRight);
        }
    }

    private int getDateView() {
        switch (orientation) {
            case HORIZONTAL: return R.id.clockRight;
            case HORIZONTAL_FLIPPED: return R.id.clockLeft;
            case VERTICAL_FLIPPED: return alignedView(dateAlign, R.id.clockLeft, R.id.clock, R.id.clockRight);
            default: return alignedView(dateAlign, R.id.dateLeft, R.id.date, R.id.dateRight);
        }
    }

    private static int alignedView(int align, int left, int center, int right) {
        if (align == Gravity.LEFT) return left;
        if (align == Gravity.RIGHT) return right;
        return center;
    }

    RemoteViews createRemoteViews() {
        RemoteViews views = new RemoteViews(context.getPackageName(), getLayoutResource());

        for (int id : new int[]{R.id.clockLeft, R.id.clock, R.id.clockRight, R.id.dateLeft, R.id.date, R.id.dateRight}) {
            views.setViewVisibility(id, View.GONE);
        }
        int timeView = getTimeView();
        int dateView = getDateView();
        if (showTime) views.setViewVisibility(timeView, View.VISIBLE);
        if (showDate) views.setViewVisibility(dateView, View.VISIBLE);

        if (timeFormat.trim().isEmpty()) {
            views.setCharSequence(timeView, "setFormat12Hour", "h:mm");
            views.setCharSequence(timeView, "setFormat24Hour", "H:mm");
        } else {
            views.setCharSequence(timeView, "setFormat12Hour", timeFormat);
            views.setCharSequence(timeView, "setFormat24Hour", timeFormat);
        }
        views.setCharSequence(dateView, "setFormat12Hour", dateFormat);
        views.setCharSequence(dateView, "setFormat24Hour", dateFormat);

        views.setTextViewTextSize(timeView, TypedValue.COMPLEX_UNIT_SP, timeSize);
        views.setTextViewTextSize(dateView, TypedValue.COMPLEX_UNIT_SP, dateSize);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !COLOR_CUSTOM.equals(colorMode)) {
            // Resolved by the launcher when it draws the widget, so the colors follow
            // wallpaper changes without the app being woken up.
            boolean light = COLOR_DYNAMIC_LIGHT.equals(colorMode);
            views.setColorStateList(timeView, "setTextColor",
                    light ? android.R.color.system_accent1_100 : android.R.color.system_accent1_700);
            views.setColorStateList(dateView, "setTextColor",
                    light ? android.R.color.system_accent2_100 : android.R.color.system_accent2_700);
        } else {
            views.setTextColor(timeView, timeColor);
            views.setTextColor(dateView, dateColor);
        }
        return views;
    }
}
