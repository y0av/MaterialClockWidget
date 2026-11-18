package xyz.yoav.materialclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;

public class ClockWidgetProvider extends AppWidgetProvider {

    static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        WidgetViewCreator creator = new WidgetViewCreator(context, WidgetPrefs.get(context, appWidgetId));
        manager.updateAppWidget(appWidgetId, creator.createRemoteViews());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        // The system sends an update to all widgets right after the app is upgraded.
        WidgetPrefs.migrateLegacy(context,
                manager.getAppWidgetIds(new ComponentName(context, ClockWidgetProvider.class)));
        for (int id : appWidgetIds) {
            updateWidget(context, manager, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            WidgetPrefs.delete(context, id);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        for (int i = 0; i < oldWidgetIds.length; i++) {
            WidgetPrefs.move(context, oldWidgetIds[i], newWidgetIds[i]);
        }
    }
}
