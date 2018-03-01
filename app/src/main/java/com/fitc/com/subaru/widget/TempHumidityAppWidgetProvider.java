package com.fitc.com.subaru.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


import com.fitc.com.subaru.Constants;
import com.fitc.com.subaru.R;


public class TempHumidityAppWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Get the rotation for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temp_humidity_widget_layout);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (Constants.ACTION_TEMP_UPDATE.equals(action)) {
            int t = intent.getIntExtra(Constants.EXTRA_TEMP, Constants.INT_PARSE_ERROR);
            int h = intent.getIntExtra(Constants.EXTRA_HUMIDITY, Constants.INT_PARSE_ERROR);

            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.temp_humidity_widget_layout);

            if (t==Constants.INT_PARSE_ERROR){
                views.setTextViewText(R.id.widgettv_temperature, " -");
            } else {
                views.setTextViewText(R.id.widgettv_temperature, "" + t);
            }

            if (h==Constants.INT_PARSE_ERROR){
                views.setTextViewText(R.id.widgettv_humidity, " -");
            } else {
                views.setTextViewText(R.id.widgettv_humidity, ""+h);
            }




            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, TempHumidityAppWidgetProvider.class),views);
        }
    }
}