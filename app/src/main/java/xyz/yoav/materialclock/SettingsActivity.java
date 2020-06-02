package xyz.yoav.materialclock;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsActivity";
    int appWidgetId;
    static int clockColor = Color.MAGENTA;
    static boolean showHours=true, showMinutes=true, showSeconds=true;
    static String fontName = "default";

    FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        preview =  findViewById(R.id.preview_view);

        setupPreviewFrame();
        widgetSetup();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"## listen");
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sp_main),MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences,"");
    }

    @Override
    public void onPause() {
        super.onPause();
        getSharedPreferences(getString(R.string.sp_main),MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //if (key.equals(getString(R.string.sp_clock_color)))
            clockColor = sharedPreferences.getInt(getString(R.string.sp_clock_color), Color.MAGENTA);
        //else if (key.equals(getString(R.string.sp_show_hour)))
            showHours = sharedPreferences.getBoolean(getString(R.string.sp_show_hour),true);
        //else if (key.equals(getString(R.string.sp_show_minutes)))
            showMinutes = sharedPreferences.getBoolean(getString(R.string.sp_show_minutes),true);
        //else if (key.equals(getString(R.string.sp_show_seconds)))
            showSeconds = sharedPreferences.getBoolean(getString(R.string.sp_show_seconds),true);
        //else if (key.equals(getString(R.string.sp_font)))
            fontName = sharedPreferences.getString(getString(R.string.sp_font),"default");
        widgetSetup();

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.sp_main),MODE_PRIVATE);

            ColorPreferenceCompat colorPreference = findPreference(getString(R.string.sp_clock_color));
            colorPreference.setDefaultValue(sharedPref.getInt(getString(R.string.sp_clock_color), Color.MAGENTA));
            colorPreference.setOnPreferenceChangeListener(listener);
            TwoStatePreference show_hour = findPreference(getString(R.string.sp_show_hour));
            show_hour.setChecked(sharedPref.getBoolean(getString(R.string.sp_show_hour),true));
            show_hour.setOnPreferenceChangeListener(listener);
            TwoStatePreference show_minutes = findPreference(getString(R.string.sp_show_minutes));
            show_minutes.setChecked(sharedPref.getBoolean(getString(R.string.sp_show_minutes),true));
            show_minutes.setOnPreferenceChangeListener(listener);
            TwoStatePreference show_seconds = findPreference(getString(R.string.sp_show_seconds));
            show_seconds.setChecked(sharedPref.getBoolean(getString(R.string.sp_show_seconds),true));
            show_seconds.setOnPreferenceChangeListener(listener);
            ListPreference fontList = findPreference(getString(R.string.sp_font));
            fontList.setOnPreferenceChangeListener(listener);
        }

        Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.sp_main),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if (getString(R.string.sp_clock_color).equals(preference.getKey())) {
                    clockColor = (int)newValue;
                    editor.putInt(getString(R.string.sp_clock_color), clockColor);
                    String newDefaultColor = Integer.toHexString((int) newValue);
                    Log.d(TAG, "## New default color is: #" + newDefaultColor);
                }
                if (getString(R.string.sp_show_hour).equals(preference.getKey()))
                    editor.putBoolean(getString(R.string.sp_show_hour),(boolean)newValue);
                if (getString(R.string.sp_show_minutes).equals(preference.getKey()))
                    editor.putBoolean(getString(R.string.sp_show_minutes),(boolean)newValue);
                if (getString(R.string.sp_show_seconds).equals(preference.getKey()))
                    editor.putBoolean(getString(R.string.sp_show_seconds),(boolean)newValue);
                if (getString(R.string.sp_font).equals(preference.getKey())) {
                    editor.putString(getString(R.string.sp_font),(String)newValue);
                }

                editor.apply();
                return true;
            }
        };

    }

    private void setupPreviewFrame() {
        ImageView preview = findViewById(R.id.bg);
        if (isReadStoragePermissionGranted()) {
            final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            preview.setImageDrawable(wallpaperDrawable);
        }
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setupPreviewFrame();
    }

    private void widgetSetup() {
        Log.d(TAG,"## widgetSetup");
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(),
                getLayoutResource());
        //set clock format
        views.setCharSequence(R.id.clock,"setFormat24Hour",getHourFormat());
        //set clock color
        views.setTextColor(R.id.clock,clockColor);

        preview.removeAllViews();
        View previewView = views.apply(this,preview);
        preview.addView(previewView);



        appWidgetManager.updateAppWidget(appWidgetId, views);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        //finish();

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
            case "warnes": return R.layout.widget_layout_warnes;
            case "lato": return R.layout.widget_layout_lato;
            case "arizonia": return R.layout.widget_layout_arizonia;
            case "imprima": return R.layout.widget_layout_imprima;
            case "notosans": return R.layout.widget_layout_notosans;
            case "rubik": return R.layout.widget_layout_rubik;
            case "jollylodger": return R.layout.widget_layout_jollylodger;
            default: return R.layout.widget_layout_default;
        }
    }

}