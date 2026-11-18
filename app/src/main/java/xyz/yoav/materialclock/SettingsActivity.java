package xyz.yoav.materialclock;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;

/** Widget configuration screen. Shown when a widget is placed (Android 11 and older) or reconfigured. */
public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String ARG_WIDGET_ID = "appWidgetId";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private SharedPreferences prefs;
    private FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        // Light status bar icons by default: the screen sits over the wallpaper.
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        // Always OK: leaving the screen keeps the widget with whatever was chosen so far.
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId));

        prefs = WidgetPrefs.get(this, appWidgetId);
        setContentView(R.layout.settings_activity);
        preview = findViewById(R.id.preview_view);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.savebtn) {
                finish();
                return true;
            }
            return false;
        });

        View settings = findViewById(R.id.settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, 0, bars.right, 0);
            preview.setPadding(0, bars.top, 0, 0);
            preview.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.preview_height) + bars.top;
            settings.setPadding(0, 0, 0, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        matchStatusBarToWallpaper();

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_WIDGET_ID, appWidgetId);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, fragment).commit();
        }
    }

    /** Dark status bar icons when the wallpaper is light (Android 12+ exposes this hint). */
    private void matchStatusBarToWallpaper() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        try {
            WallpaperColors colors = WallpaperManager.getInstance(this).getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
            if (colors == null) return;
            boolean darkText = (colors.getColorHints() & WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0;
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(darkText);
        } catch (RuntimeException ignored) {
            // Some devices restrict wallpaper access; keep the default.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
        render();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        render();
    }

    /** Redraws the in-app preview and pushes the same views to the home-screen widget. */
    private void render() {
        RemoteViews views = new WidgetViewCreator(this, prefs).createRemoteViews();
        preview.removeAllViews();
        preview.addView(views.apply(this, preview));
        AppWidgetManager.getInstance(this).updateAppWidget(appWidgetId, views);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            int appWidgetId = requireArguments().getInt(ARG_WIDGET_ID);
            getPreferenceManager().setSharedPreferencesName(WidgetPrefs.fileName(appWidgetId));
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference timeFormat = findPreference(WidgetPrefs.TIME_FORMAT);
            timeFormat.setSummaryProvider(p -> {
                String value = ((EditTextPreference) p).getText();
                return value == null || value.trim().isEmpty() ? getString(R.string.time_format_system) : value;
            });

            ListPreference colorMode = findPreference(WidgetPrefs.COLOR_MODE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                colorMode.setVisible(true);
                updateColorPickers(colorMode.getValue());
                colorMode.setOnPreferenceChangeListener((p, value) -> {
                    updateColorPickers((String) value);
                    return true;
                });
            }
        }

        private void updateColorPickers(String colorMode) {
            boolean custom = WidgetViewCreator.COLOR_CUSTOM.equals(colorMode);
            Preference time = findPreference(WidgetPrefs.TIME_COLOR);
            Preference date = findPreference(WidgetPrefs.DATE_COLOR);
            time.setEnabled(custom);
            date.setEnabled(custom);
        }
    }
}
