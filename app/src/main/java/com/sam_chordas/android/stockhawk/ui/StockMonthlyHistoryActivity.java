package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class StockMonthlyHistoryActivity extends Activity implements OnChartValueSelectedListener {

    private BarChart mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);
        String month = "";
        String year = "";
        if (getIntent().getExtras() != null) {
            month = getIntent().getExtras().getString("month");
            year = getIntent().getExtras().getString("year");

        } else finish();

        mChart = (BarChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDescription("");

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

        new AsyncTask<String, Void, ChartData>() {
            ChartData chartData = new ChartData();

            @Override
            protected ChartData doInBackground(String... params) {
                return chartData.populate(params[0], params[1]);
            }

            @Override
            protected void onPostExecute(ChartData chartData) {
                super.onPostExecute(chartData);
                if (null != chartData) setData(chartData);
            }
        }.execute(month, year);
    }

    private void setData(ChartData chartData) {


        // Create the values for X axis
        ArrayList<String> xValues = new ArrayList<String>();
        for (int i = 1; i <= chartData.daysCount; i++) {
            if(chartData.openPriceArray[i-1]!=0)xValues.add(String.valueOf(i));
        }

        // Create the values for Y axis
        ArrayList<BarEntry> yValues1 = new ArrayList<BarEntry>(); // open
        ArrayList<BarEntry> yValues2 = new ArrayList<BarEntry>(); // close
        ArrayList<BarEntry> yValues3 = new ArrayList<BarEntry>(); // low
        ArrayList<BarEntry> yValues4 = new ArrayList<BarEntry>(); // high

        // Fill the data set from the chart data
        for (int i = 0; i < chartData.daysCount; i++) {
            if(chartData.openPriceArray[i]>0)
                yValues1.add(new BarEntry(chartData.openPriceArray[i], i));

            if(chartData.closePriceArray[i]>0)
                yValues2.add(new BarEntry(chartData.closePriceArray[i], i));

            if(chartData.lowPriceArray[i]>0)
                yValues3.add(new BarEntry(chartData.lowPriceArray[i], i));

            if(chartData.highPriceArray[i]>0)
                yValues4.add(new BarEntry(chartData.highPriceArray[i], i));
        }

        // Set data set to chart
        BarDataSet barDataSet1 = new BarDataSet(yValues1, "Open");
        barDataSet1.setColor(Color.rgb(153, 255, 255)); // Blue
        BarDataSet barDataSet2 = new BarDataSet(yValues2, "Close");
        barDataSet2.setColor(Color.rgb(255, 255, 153)); // Yellow
        BarDataSet barDataSet3 = new BarDataSet(yValues3, "Low");
        barDataSet3.setColor(Color.rgb(255, 153, 153)); // Red
        BarDataSet barDataSet4 = new BarDataSet(yValues4, "High");
        barDataSet4.setColor(Color.rgb(204, 255, 153)); // Green


        // Group the multiple data sets
        ArrayList<IBarDataSet> iBarDataSets = new ArrayList<IBarDataSet>();
       // iBarDataSets.add(barDataSet1);
        iBarDataSets.add(barDataSet2);
       // iBarDataSets.add(barDataSet3);
       // iBarDataSets.add(barDataSet4);

        // Add the data sets to bar char
        BarData barData = new BarData(xValues, iBarDataSets);

        // Display the bar chart
        mChart.setData(barData);
        mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Toast.makeText(getApplicationContext(), e.getXIndex() + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    class ChartData {
        public float openPriceArray[];
        public float closePriceArray[];
        public float lowPriceArray[];
        public float highPriceArray[];
        public int daysCount = 0;

        private String getLastDayInMonth(String month, String year) {
            int monthsDays[] = {31,28,31,30,31,30,31,31,30,31,30,31};

            if(Integer.parseInt(year)%4 == 0) monthsDays[1] = 29;

            return year+"-"+month+"-"+monthsDays[Integer.parseInt(month)-1];
        }

        public ChartData populate(String month, String year) {
            // Web service data
            OkHttpClient client = new OkHttpClient();

            String firstDateInMonth = year+"-"+month+"-01";
            String lastDateInMonth = getLastDayInMonth(month, year);
            Log.d("last day",lastDateInMonth);

            daysCount = Integer.parseInt(lastDateInMonth.split("-")[2]);

            openPriceArray = new float[daysCount];
            closePriceArray = new float[daysCount];
            lowPriceArray = new float[daysCount];
            highPriceArray = new float[daysCount];

            String urlStr = "select * from yahoo.finance.historicaldata where symbol = \"YHOO\" and startDate = \"" +
                    firstDateInMonth + "\" and endDate = \"" + lastDateInMonth + "\"";
            StringBuilder urlStringBuilder = new StringBuilder();
            try {
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode(urlStr, "UTF-8"));
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");

                Log.d("url",urlStringBuilder.toString());

                Request request = new Request.Builder().url(urlStringBuilder.toString()).build();
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                parseJson(responseString);

                return this;

            } catch (IOException ioException) {
            }
            return null;
        }

        void parseJson(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject queryObject = jsonObject.getJSONObject("query");
                JSONObject resultsObject = queryObject.getJSONObject("results");
                JSONArray quoteArray = resultsObject.getJSONArray("quote");

                for (int i = 0; i < quoteArray.length(); i++) {
                    JSONObject quoteJson = (JSONObject) quoteArray.get(i);
                    if (quoteJson != null) {
                        String date = quoteJson.getString("Date");
                        int dayIndex = Integer.parseInt(date.split("-")[2]) - 1;

                        if (dayIndex >= 0 && dayIndex < daysCount) {

                            float openPrice = Float.parseFloat(quoteJson.getString("Open"));
                            float closePrice = Float.parseFloat(quoteJson.getString("Close"));
                            float highPrice = Float.parseFloat(quoteJson.getString("High"));
                            float lowPrice = Float.parseFloat(quoteJson.getString("Low"));
                            openPriceArray[dayIndex] = openPrice;
                            closePriceArray[dayIndex] = closePrice;
                            lowPriceArray[dayIndex] = lowPrice;
                            highPriceArray[dayIndex] = highPrice;
                        }
                    }
                }
            } catch (JSONException jsonException) {
                return;
            }
        }
    }
}