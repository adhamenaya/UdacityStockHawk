package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StockHistoryActivity extends Activity implements OnChartValueSelectedListener{

    private BarChart mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);

        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDescription("Stock (GOOG) historical data in year 2015");

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        l.setYOffset(0f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);

        XAxis xl = mChart.getXAxis();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);

        new AsyncTask<String,Void,ChartData>(){
            ChartData chartData = new ChartData();
            @Override
            protected ChartData doInBackground(String... params) {
                return chartData.populate(params[0]);
            }

            @Override
            protected void onPostExecute(ChartData chartData) {
                super.onPostExecute(chartData);
                if(null!=chartData) setData(chartData);
            }
        }.execute("2015");
    }

    private void setData(ChartData chartData){


        // Create the values for X axis
        ArrayList<String> xValues = new ArrayList<String>();
        for(int i=1;i<=12;i++){
            xValues.add(String.valueOf(i));
        }

        // Create the values for Y axis
        ArrayList<BarEntry> yValues1 = new ArrayList<BarEntry>(); // open
        ArrayList<BarEntry> yValues2 = new ArrayList<BarEntry>(); // close
        ArrayList<BarEntry> yValues3 = new ArrayList<BarEntry>(); // low
        ArrayList<BarEntry> yValues4 = new ArrayList<BarEntry>(); // high

        // Fill the data set from the chart data
        for(int i=0;i<12;i++){
            yValues1.add(new BarEntry(chartData.openPriceArray[i],i));
            yValues2.add(new BarEntry(chartData.closePriceArray[i],i));
            yValues3.add(new BarEntry(chartData.lowPriceArray[i],i));
            yValues4.add(new BarEntry(chartData.highPriceArray[i],i));
        }

        // Set data set to chart
        BarDataSet barDataSet1 = new BarDataSet(yValues1,"Open");
        barDataSet1.setColor(Color.rgb(153, 255, 255)); // Blue
        BarDataSet barDataSet2 = new BarDataSet(yValues2,"Close");
        barDataSet2.setColor(Color.rgb(255, 255, 153)); // Yellow
        BarDataSet barDataSet3 = new BarDataSet(yValues3,"Low");
        barDataSet3.setColor(Color.rgb(255, 153, 153)); // Red
        BarDataSet barDataSet4 = new BarDataSet(yValues4,"High");
        barDataSet4.setColor(Color.rgb(204, 255, 153)); // Green


        // Group the multiple data sets
        ArrayList<IBarDataSet> iBarDataSets = new ArrayList<IBarDataSet>();
      //  iBarDataSets.add(barDataSet1);
        iBarDataSets.add(barDataSet2);
      //  iBarDataSets.add(barDataSet3);
      //  iBarDataSets.add(barDataSet4);

        // Add the data sets to bar char
        BarData barData = new BarData(xValues,iBarDataSets);

        // Display the bar chart
        mChart.setData(barData);
        mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Intent intent = new Intent(getApplicationContext(),StockMonthlyHistoryActivity.class);
        intent.putExtra("month",String.valueOf(String.format("%02d",e.getXIndex()+1)));
        intent.putExtra("year",String.valueOf(2015));
        startActivity(intent);
    }

    @Override
    public void onNothingSelected() {

    }

     class ChartData{
        public float openPriceArray[] = new float[12] ;
        public float closePriceArray[] = new float[12];
        public float lowPriceArray[] = new float[12];
        public float highPriceArray[] = new float[12];

         public ChartData populate(String year){
             // Web service data
             OkHttpClient client = new OkHttpClient();
             String urlStr = "select * from yahoo.finance.historicaldata where symbol = \"YHOO\" and startDate = \""+(year)+"-01-01\" and endDate = \""+String.valueOf(year)+"-12-31\"";
             StringBuilder urlStringBuilder = new StringBuilder();
             try{
                 urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                 urlStringBuilder.append(URLEncoder.encode(urlStr, "UTF-8"));
                 urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                         + "org%2Falltableswithkeys&callback=");

                 Request request = new Request.Builder().url(urlStringBuilder.toString()).build();
                 Response response = client.newCall(request).execute();
                 String responseString = response.body().string();
                 parseJson(responseString);

                 return this;

             }catch(IOException ioException){ }
             return null;
        }

         void parseJson(String json){
             try {
                 JSONObject jsonObject = new JSONObject(json);
                 JSONObject queryObject = jsonObject.getJSONObject("query");
                 JSONObject resultsObject = queryObject.getJSONObject("results");
                 JSONArray quoteArray = resultsObject.getJSONArray("quote");

                 for(int i =0; i< quoteArray.length();i++){
                     JSONObject quoteJson = (JSONObject)quoteArray.get(i);
                     if(quoteJson!=null){
                         String date = quoteJson.getString("Date");
                         int monthIndex = Integer.parseInt(date.split("-")[1])-1;
                         Log.d("month",monthIndex+"");
                         if(monthIndex >=0 && monthIndex <12){
                             Log.d("month",monthIndex+"");
                             float openPrice = Float.parseFloat(quoteJson.getString("Open"));
                             float closePrice = Float.parseFloat(quoteJson.getString("Close"));
                             float highPrice = Float.parseFloat(quoteJson.getString("High"));
                             float lowPrice = Float.parseFloat(quoteJson.getString("Low"));
                             openPriceArray[monthIndex] += openPrice/12;
                             closePriceArray[monthIndex] += closePrice/12;
                             lowPriceArray[monthIndex] += lowPrice/12;
                             highPriceArray[monthIndex] += highPrice/12;
                         }
                     }
                 }
             }catch(JSONException jsonException){
               return;
             }
         }
    }
}