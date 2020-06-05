package xyz.yoav.materialclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetViewCreator implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "WidgetViewCreator";
    static int clockColor = Color.WHITE,
            dateColor = Color.WHITE;
    static boolean showHours=true, showMinutes=true, showSeconds=true;
    static String fontName = "default",
                  seperator = ":";
    static int timeTextSize=42,
                dateTextSize=22;
    static int timeAlign = Gravity.CENTER,
            dateAlign = Gravity.CENTER;
    private Context context;
    private WidgetUpdatedInterface widgetUpdatedInterface;

    public WidgetViewCreator(WidgetUpdatedInterface widgetUpdatedInterface, Context context) {
        this.context = context;
        this.widgetUpdatedInterface = widgetUpdatedInterface;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //if (key.equals(getString(R.string.sp_clock_color)))
        clockColor = sharedPreferences.getInt(context.getString(R.string.sp_clock_color), Color.WHITE);
        dateColor = sharedPreferences.getInt(context.getString(R.string.sp_date_color), Color.WHITE);
        //else if (key.equals(getString(R.string.sp_show_hour)))
        showHours = sharedPreferences.getBoolean(context.getString(R.string.sp_show_hour),true);
        //else if (key.equals(getString(R.string.sp_show_minutes)))
        showMinutes = sharedPreferences.getBoolean(context.getString(R.string.sp_show_minutes),true);
        //else if (key.equals(getString(R.string.sp_show_seconds)))
        showSeconds = sharedPreferences.getBoolean(context.getString(R.string.sp_show_seconds),true);
        //else if (key.equals(getString(R.string.sp_font)))
        fontName = sharedPreferences.getString(context.getString(R.string.sp_font),"default");
        seperator = sharedPreferences.getString(context.getString(R.string.sp_seperator),":");
        timeTextSize = sharedPreferences.getInt(context.getString(R.string.sp_time_size),42);
        dateTextSize = sharedPreferences.getInt(context.getString(R.string.sp_date_size),22);
        timeAlign = sharedPreferences.getInt(context.getString(R.string.sp_time_align),Gravity.CENTER);
        dateAlign = sharedPreferences.getInt(context.getString(R.string.sp_date_align),Gravity.CENTER);

        widgetUpdatedInterface.widgetDataUpdated();
    }

    private String getHourFormat() {
        String format = "";
        if (showHours) format += "k";
        if (showMinutes) {
            if (!format.isEmpty()) format += seperator;
            format += "mm";
        }
        if (showSeconds) {
            if (!format.isEmpty()) format += seperator;
            format += "ss";
        }
        Log.d(TAG,"## format: " + format);
        return format;
    }

    private String getDateFormat() {
        return "EEE, MMM d";
    }

    private int getLayoutResource() {
        switch (fontName) {
            case "warnes":
                return R.layout.widget_layout_warnes;
            case "latoreg":
                return R.layout.widget_layout_latoreg;
            case "latothin":
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

    private int getCorrectClockView() {
        Log.d(TAG,"## timeAlign: " + timeAlign);
        //return R.id.clock;
        switch (timeAlign) {
            case Gravity.LEFT: return R.id.clockLeft;
            case Gravity.CENTER: return R.id.clock;
            case Gravity.RIGHT: return R.id.clockRight;
            default:return R.id.clock;
        }
    }

    private int getCorrectCDateView() {
        Log.d(TAG,"## dateAlign: " + dateAlign);
        //return R.id.clock;
        switch (dateAlign) {
            case Gravity.LEFT: return R.id.dateLeft;
            case Gravity.CENTER: return R.id.date;
            case Gravity.RIGHT: return R.id.dateRight;
            default:return R.id.date;
        }
    }

    public RemoteViews createWidgetRemoteView() {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                getLayoutResource());

        //set clock alignment
        views.setViewVisibility(R.id.clockLeft, View.GONE);
        views.setViewVisibility(R.id.clock, View.GONE);
        views.setViewVisibility(R.id.clockRight, View.GONE);
        views.setViewVisibility(getCorrectClockView(), View.VISIBLE);
        //set date alignment
        views.setViewVisibility(R.id.dateLeft, View.GONE);
        views.setViewVisibility(R.id.date, View.GONE);
        views.setViewVisibility(R.id.dateRight, View.GONE);
        views.setViewVisibility(getCorrectCDateView(), View.VISIBLE);
        //set clock format
        views.setCharSequence(getCorrectClockView(),"setFormat24Hour",getHourFormat());
        views.setCharSequence(getCorrectClockView(),"setFormat12Hour",getHourFormat());
        //set date format
        views.setCharSequence(getCorrectCDateView(),"setFormat24Hour",getDateFormat());
        views.setCharSequence(getCorrectCDateView(),"setFormat12Hour",getDateFormat());
        //set time size
        views.setTextViewTextSize(getCorrectClockView(), TypedValue.COMPLEX_UNIT_SP,timeTextSize);
        //set date size
        views.setTextViewTextSize(getCorrectCDateView(), TypedValue.COMPLEX_UNIT_SP,dateTextSize);
        //set clock color
        views.setTextColor(getCorrectClockView(),clockColor);
        //set date color
        views.setTextColor(getCorrectCDateView(),dateColor);
        return views;
    }
}
