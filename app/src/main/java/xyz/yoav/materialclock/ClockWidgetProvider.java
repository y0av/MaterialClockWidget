package xyz.yoav.materialclock;

//import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import static android.content.Context.MODE_PRIVATE;

public class ClockWidgetProvider extends AppWidgetProvider implements WidgetUpdatedInterface {
    public static final String CLICK_ACTION = "xyz.yoav.materialclock.CLICK_ACTION";
    public static final String EXTRA_ITEM = "xyz.yoav.materialclock.EXTRA_ITEM";
    public static final String APPWIDGET_UPDATE_OPTIONS = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
    public static final String APPWIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";

    WidgetViewCreator widgetViewCreator;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private void redrawWidgetFromData(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        widgetViewCreator = new WidgetViewCreator(this,context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.sp_main),MODE_PRIVATE);
        widgetViewCreator.onSharedPreferenceChanged(sharedPreferences,"");
        RemoteViews views = widgetViewCreator.createWidgetRemoteView();
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                ClockWidgetProvider.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            redrawWidgetFromData(context, appWidgetManager, widgetId);
            // Register an onClickListener
            /*Intent intent = new Intent(context, ClockWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.clock, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);*/
        }
    }

    @Override
    public void widgetDataUpdated() {

    }
}