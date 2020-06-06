package xyz.yoav.materialclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import androidx.recyclerview.widget.RecyclerView;

public class WidgetViewCreator implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "WidgetViewCreator";
    static int clockColor = Color.WHITE,
            dateColor = Color.WHITE;
    static boolean showTime=true,
                showDate;
    static String timeFormat="k:mm",
                dateFormat="EEE, MMM d";
    static String fontName = "default";
    static int timeTextSize=42,
                dateTextSize=22;
    static int timeAlign = Gravity.CENTER,
            dateAlign = Gravity.CENTER;
    static final int VERTICAL=0, VERTICAL_FLIPPED=1, HORIZONTAL=2, HORIZONTAL_FLIPPED=3; //this need to be the same as the corresponding array in arrays.xml
    static int widgetOrientation = VERTICAL;
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
        showTime = sharedPreferences.getBoolean(context.getString(R.string.sp_show_time),true);
        showDate = sharedPreferences.getBoolean(context.getString(R.string.sp_show_date),true);
        timeFormat = sharedPreferences.getString(context.getString(R.string.sp_time_format),"k:mm");
        dateFormat = sharedPreferences.getString(context.getString(R.string.sp_date_format),"EEE, MMM d");
        fontName = sharedPreferences.getString(context.getString(R.string.sp_font),"default");
        timeTextSize = sharedPreferences.getInt(context.getString(R.string.sp_time_size),42);
        dateTextSize = sharedPreferences.getInt(context.getString(R.string.sp_date_size),22);
        timeAlign = sharedPreferences.getInt(context.getString(R.string.sp_time_align),Gravity.CENTER);
        dateAlign = sharedPreferences.getInt(context.getString(R.string.sp_date_align),Gravity.CENTER);
        widgetOrientation = sharedPreferences.getInt(context.getString(R.string.sp_layout),VERTICAL);

        widgetUpdatedInterface.widgetDataUpdated();
    }

    private int getLayoutResource() {
        switch (fontName) {
            case "warnes":
                return R.layout.widget_layout_warnes;
            case "latoreg":
                return R.layout.widget_layout_latoreg;
            case "latolight":
                return R.layout.widget_layout_latolight;
            case "latothin":
                return R.layout.widget_layout_latothin;
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
            case "archivoblack":
                return R.layout.widget_layout_archivoblack;
            case "bungeeshade":
                return R.layout.widget_layout_bungeeshade;
            case "coda":
                return R.layout.widget_layout_coda;
            case "ubuntulight":
                return R.layout.widget_layout_ubuntulight;
            case "handlee":
                return R.layout.widget_layout_handlee;
            default:
                return R.layout.widget_layout_default;
        }
    }

    private int getCorrectTimeView() {
        if (widgetOrientation==HORIZONTAL) return R.id.clockLeft;
        else if (widgetOrientation==HORIZONTAL_FLIPPED) return R.id.clockRight;
        else if (widgetOrientation==VERTICAL) {
            switch (timeAlign) {
                case Gravity.LEFT: return R.id.clockLeft;
                case Gravity.RIGHT: return R.id.clockRight;
                default: return R.id.clock; //center
            }
        } else { //VERTICAL_FLIPPED
            switch (timeAlign) {
                case Gravity.LEFT: return R.id.dateLeft;
                case Gravity.RIGHT: return R.id.dateRight;
                default: return R.id.date; //center
            }
        }
    }

    private int getCorrectDateView() {
        if (widgetOrientation==HORIZONTAL) return R.id.clockRight;
        else if (widgetOrientation==HORIZONTAL_FLIPPED) return R.id.clockLeft;
        else if (widgetOrientation==VERTICAL) {
            switch (dateAlign) {
                case Gravity.LEFT: return R.id.dateLeft;
                case Gravity.RIGHT: return R.id.dateRight;
                default: return R.id.date; //center
            }
        } else { //VERTICAL_FLIPPED
            switch (dateAlign) {
                case Gravity.LEFT: return R.id.clockLeft;
                case Gravity.RIGHT: return R.id.clockRight;
                default: return R.id.clock; //center
            }
        }
    }

    public RemoteViews createWidgetRemoteView() {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                getLayoutResource());

        //set clock alignment
        views.setViewVisibility(R.id.clockLeft, View.GONE);
        views.setViewVisibility(R.id.clock, View.GONE);
        views.setViewVisibility(R.id.clockRight, View.GONE);
        //set date alignment
        views.setViewVisibility(R.id.dateLeft, View.GONE);
        views.setViewVisibility(R.id.date, View.GONE);
        views.setViewVisibility(R.id.dateRight, View.GONE);
        if (showTime)
            views.setViewVisibility(getCorrectTimeView(), View.VISIBLE);
        if (showDate)
            views.setViewVisibility(getCorrectDateView(), View.VISIBLE);
        //set clock format
        views.setCharSequence(getCorrectTimeView(),"setFormat24Hour",timeFormat);
        views.setCharSequence(getCorrectTimeView(),"setFormat12Hour",timeFormat);
        //set date format
        views.setCharSequence(getCorrectDateView(),"setFormat24Hour",dateFormat);
        views.setCharSequence(getCorrectDateView(),"setFormat12Hour",dateFormat);
        //set time size
        views.setTextViewTextSize(getCorrectTimeView(), TypedValue.COMPLEX_UNIT_SP,timeTextSize);
        //set date size
        views.setTextViewTextSize(getCorrectDateView(), TypedValue.COMPLEX_UNIT_SP,dateTextSize);
        //set clock color
        views.setTextColor(getCorrectTimeView(),clockColor);
        //set date color
        views.setTextColor(getCorrectDateView(),dateColor);

        return views;
    }
}
