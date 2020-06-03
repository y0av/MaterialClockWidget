package xyz.yoav.materialclock;

import android.appwidget.AppWidgetManager;
import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetViewCreator implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "WidgetViewCreator";
    static int clockColor = Color.MAGENTA;
    static boolean showHours=true, showMinutes=true, showSeconds=true;
    static String fontName = "default";
    private Context context;
    private WidgetUpdatedInterface widgetUpdatedInterface;

    public WidgetViewCreator(WidgetUpdatedInterface widgetUpdatedInterface, Context context) {
        this.context = context;
        this.widgetUpdatedInterface = widgetUpdatedInterface;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //if (key.equals(getString(R.string.sp_clock_color)))
        clockColor = sharedPreferences.getInt(context.getString(R.string.sp_clock_color), Color.MAGENTA);
        //else if (key.equals(getString(R.string.sp_show_hour)))
        showHours = sharedPreferences.getBoolean(context.getString(R.string.sp_show_hour),true);
        //else if (key.equals(getString(R.string.sp_show_minutes)))
        showMinutes = sharedPreferences.getBoolean(context.getString(R.string.sp_show_minutes),true);
        //else if (key.equals(getString(R.string.sp_show_seconds)))
        showSeconds = sharedPreferences.getBoolean(context.getString(R.string.sp_show_seconds),true);
        //else if (key.equals(getString(R.string.sp_font)))
        fontName = sharedPreferences.getString(context.getString(R.string.sp_font),"default");
        widgetUpdatedInterface.widgetDataUpdated();
    }

    private String getHourFormat() {
        String format = "";
        if (showHours) format += "k";
        if (showMinutes) {
            if (!format.isEmpty()) format += ":";
            format += "mm";
        }
        if (showSeconds) {
            if (!format.isEmpty()) format += ":";
            format += "ss";
        }
        Log.d(TAG,"## format: " + format);
        return format;
    }

    private int getLayoutResource() {
        switch (fontName) {
            case "warnes":
                return R.layout.widget_layout_warnes;
            case "lato":
                return R.layout.widget_layout_lato;
            case "arizonia":
                return R.layout.widget_layout_arizonia;
            case "imprima":
                return R.layout.widget_layout_imprima;
            case "notosans":
                return R.layout.widget_layout_notosans;
            case "rubik":
                return R.layout.widget_layout_rubik;
            case "jollylodger":
                return R.layout.widget_layout_jollylodger;
            default:
                return R.layout.widget_layout_default;
        }
    }

    public RemoteViews createWidgetRemoteView() {

        RemoteViews views = new RemoteViews(context.getPackageName(),
                getLayoutResource());
        //set clock format
        views.setCharSequence(R.id.clock,"setFormat24Hour",getHourFormat());
        views.setCharSequence(R.id.clock,"setFormat12Hour",getHourFormat());
        //set clock color
        views.setTextColor(R.id.clock,clockColor);
        return views;
    }
}
