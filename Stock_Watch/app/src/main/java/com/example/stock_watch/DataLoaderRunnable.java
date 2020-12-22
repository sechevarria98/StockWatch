package com.example.stock_watch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataLoaderRunnable implements Runnable {

    private static final String TAG = "DataLoaderRunnable";
    private MainActivity mainActivity;
    private String symbol;
    private int flag;
    private static final String DATA_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String yourAPIKey = "";

    DataLoaderRunnable(MainActivity mainActivity, String symbol, int flag) {
        this.mainActivity = mainActivity;
        this.symbol = symbol;
        this.flag = flag;
    }

    @Override
    public void run() {
        final String URL = DATA_URL + symbol + "/quote?token=" + yourAPIKey;

        Uri dataUri = Uri.parse(URL);
        String urlToUse = dataUri.toString();

        //Log.d(TAG, "run: " + urlToUse);
        StringBuilder sb = new StringBuilder();
        try {
            java.net.URL url = new URL(urlToUse);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                handleResults(null);
                return;
            }

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            handleResults(null);
            return;
        }
        Log.d(TAG, "run: "  + sb.toString());
        handleResults(sb.toString());
    }

    public void handleResults(final String jsonString) {
        final Stock s = parseJSON(jsonString);

        if(flag == 1) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.stockData(s);
                }
            });
        }
        else {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.restoreData(s);
                }
            });
        }
    }

    private Stock parseJSON(String s) {
        try {
            JSONObject jObjMain = new JSONObject(s);

            String symbol = jObjMain.getString("symbol");
            String name = jObjMain.getString("companyName");
            Double latestPrice = jObjMain.getDouble("latestPrice");
            Double change = jObjMain.getDouble("change");
            Double changePercent = jObjMain.getDouble("changePercent");

            return new Stock(symbol, name, latestPrice, change, changePercent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
