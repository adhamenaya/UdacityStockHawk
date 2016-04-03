package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.ui.StockHistoryActivity;

/**
 * Created by adhamenaya on 4/3/2016.
 */
public class MyWidgetProvider extends AppWidgetProvider {

    public static final String START_ACTION = " com.sam_chordas.android.stockhawk.STRAT_ACTION";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Fill the each widget from the remote adapter
        for(int i=0;i< appWidgetIds.length ; i++){

            // Assign the app widget service that will fill the collection (stack view)
            Intent intent = new Intent(context,MyWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Fill the collection view using the service intent of the views factory
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.my_widget_layout);
            remoteViews.setRemoteAdapter(R.id.stackview_stock, intent);

            // Set the view for empty collection
            remoteViews.setEmptyView(appWidgetIds[i],R.id.textview_no_data);

            // Here fill the template of the pending intent
            Intent startIntent = new Intent(context,MyWidgetProvider.class);
            startIntent.setAction(START_ACTION);

            // set template intent. Where the collection item will fill it's custom intent
            // later when creating the view in the remote adapter
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    startIntent,PendingIntent.FLAG_UPDATE_CURRENT
            );

            remoteViews.setPendingIntentTemplate(R.id.stackview_stock,pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i],remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(START_ACTION)){
            String symbol = intent.getExtras().getString(QuoteColumns.SYMBOL);
          /*  if(symbol != null) {
                Intent startActivityIntent = new Intent("com.company.package.FOO");
                startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startActivityIntent);
            }*/
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
