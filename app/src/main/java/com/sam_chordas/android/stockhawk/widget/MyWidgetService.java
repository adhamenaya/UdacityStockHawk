package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.Stock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adhamenaya on 4/3/2016.
 */
public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new MyWidgetRemoteViewsFactory(getApplicationContext(),intent);
    }
}

class MyWidgetRemoteViewsFactory implements MyWidgetService.RemoteViewsFactory{

    List<Stock> mStocks = new ArrayList<Stock>();
    Intent mIntent;
    Context mContext;

    public MyWidgetRemoteViewsFactory(Context context, Intent intent){
        this.mContext = context;
        this.mIntent = intent;
    }

    public void onCreate() {
    }

    @Override
    public void onDestroy() {
            mStocks.clear();
    }

    @Override
    public int getCount() {
        return mStocks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        // Create remote view from the XML layout
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.my_widget_item);
        remoteViews.setTextViewText(R.id.textView_widget_stock,mStocks.get(position).symbol);
        remoteViews.setTextViewText(R.id.textView_widget_change,mStocks.get(position).change);

        // Create pending intent template that will be filled. This intent will fire a broadcast
        // that when received will start activity relevant to the stock selected.
        Bundle bundle = new Bundle();
        bundle.putString(QuoteColumns.SYMBOL,mStocks.get(position).symbol);

        // Fill the template intent
        Intent fillIntent = new Intent();
        fillIntent.putExtras(bundle);

        // Assign the click event to the view that will call the filling intent when clicked
        remoteViews.setOnClickFillInIntent(R.id.layout_content,fillIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public void onDataSetChanged() {
        // Read the stock quote from the content provider
        mStocks = readAllStocks();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private List<Stock> readAllStocks(){

        final long identityToken = Binder.clearCallingIdentity();

        ArrayList<Stock> stocks = new ArrayList<Stock>();
        ArrayList<String> visited = new ArrayList<String>();

        // Query from the content provider
        Cursor cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{"Distinct " + QuoteColumns.SYMBOL,QuoteColumns.CHANGE},null,null,null);


        cursor.moveToFirst();

        if(cursor != null && cursor.getCount() > 0){
            while(cursor.moveToNext()){
                String symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
                String change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));

                // Don't add the already added symbols
                if(visited.contains(symbol)) continue;

                Stock stock = new Stock();
                stock.symbol = symbol;
                stock.change = change;
                stocks.add(stock);
                visited.add(symbol);
            }
            cursor.close();
        }

        Binder.restoreCallingIdentity(identityToken);
        return stocks;
    }
}
