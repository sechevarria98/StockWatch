package com.example.stock_watch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivityTAG";
    private static final String URL_MARKET = "https://www.marketwatch.com/investing/stock/";

    private DatabaseHandler databaseHandler;
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swiper;
    private List<Stock> stockList = new ArrayList<>();
    private HashMap<String, String> stocks = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        if(doNetCheck()) {
            //Start the Symbol & Company Name downloader
            StockLoaderRunnable stockLoaderRunnable = new StockLoaderRunnable(this);
            new Thread(stockLoaderRunnable).start();

            databaseHandler = new DatabaseHandler(this);

            //Load Stocks in the database
            ArrayList<String[]> list = databaseHandler.loadStocks();
            if(list.size() > 0) {
                for(String[] tmp : list) {
                    Stock s = new Stock(tmp[0], tmp[1]);
                    startDownload(s.getSymbol(), 0);
                }
            }
        }
        else {
            noInternet();
        }

    }

    private boolean doNetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void doRefresh() {
        if(doNetCheck()) {
            stockList.clear();
            ArrayList<String[]> list = databaseHandler.loadStocks();
            if(list.size() > 0) {
                for(String[] tmp : list) {
                    Stock s = new Stock(tmp[0], tmp[1]);
                    startDownload(s.getSymbol(), 0);
                }
            }
            else {
                Toast.makeText(this, getString(R.string.db_empty), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            noInternet();
        }
        swiper.setRefreshing(false);
    }

    ///////////////////////////// Misc. Dialog ////////////////////////////////////
    public void symbolNotFound(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + s);
        builder.setMessage("Data for stock symbol");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void duplicateStock(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Stock");
        builder.setMessage("Stock Symbol " + s + " is already displayed");
        builder.setIcon(R.drawable.ic_baseline_warning_24);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void noInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Stocks cannot be added without a internet connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    ////////////////////////////   END    /////////////////////////////////////////

    ///////////////////////////// Dialog //////////////////////////////////////////
    public void dialogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol");
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String choice = editText.getText().toString();
                boolean flag = false;
                if (stockList.isEmpty()) {
                    findStock(choice);
                }
                else {
                    for(Stock s: stockList) {
                        if (s.getSymbol().equals(choice)) {
                            flag = false;
                            duplicateStock(choice);
                            dialogInterface.dismiss();
                            break;
                        }
                        else
                            flag = true;
                    }
                    if(flag)
                        findStock(choice);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void findStock(String s) {
        ArrayList<String> buffer = new ArrayList<>();

        for (String key : stocks.keySet()) {
            if (key.contains(s) || stocks.get(key).contains(s)) {
                buffer.add(key + " - " + stocks.get(key));
            }
        }
        Collections.sort(buffer, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareToIgnoreCase(t1);
            }
        });

        final CharSequence[] tmp = new CharSequence[buffer.size()];
        for(int i = 0; i < buffer.size(); i++)
            tmp[i] = buffer.get(i);

        if(buffer.size() == 0) {
            symbolNotFound(s);
        }
        else if (buffer.size() == 1) {
            startDownload(buffer.get(0).substring(0, buffer.get(0).indexOf(" ")), 1);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make a Selection");
            builder.setItems(tmp, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startDownload(tmp[i].toString().substring(0, tmp[i].toString().indexOf(" ")), 1);
                }
            });

            builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    ////////////////////////////   END    /////////////////////////////////////////

    ///////////////////////////// RUNNABLE ///////////////////////////////////////
    public void startDownload(String s, int i) {
        DataLoaderRunnable dataLoaderRunnable = new DataLoaderRunnable(this, s, i);
        new Thread(dataLoaderRunnable).start();
    }

    public void stockData(Stock s) {
        stockList.add(s);
        Collections.sort(stockList, new Comparator<Stock>() {
            @Override
            public int compare(Stock stock, Stock t1) {
                return stock.getSymbol().compareToIgnoreCase(t1.getSymbol());
            }
        });
        stockAdapter.notifyDataSetChanged();
        Stock tmp = new Stock(s.getSymbol(), s.getName());
        databaseHandler.addStock(tmp);
    }

    public void restoreData(Stock s) {
        stockList.add(s);
        Collections.sort(stockList, new Comparator<Stock>() {
            @Override
            public int compare(Stock stock, Stock t1) {
                return stock.getSymbol().compareToIgnoreCase(t1.getSymbol());
            }
        });
        stockAdapter.notifyDataSetChanged();
    }

    public void updateData(ArrayList<Stock> slist) {
        for(int i = 0; i < slist.size(); i++) {
            Stock stock = slist.get(i);
            stocks.put(stock.getSymbol(), stock.getName());
        }
    }
    ////////////////////////////   END    /////////////////////////////////////////

    ///////////////////////////// MENU    /////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_stock) {
            if(doNetCheck())
                dialogs();
            else
                noInternet();
        }
        else {
            String err = "Unknown Menu Item" + item.getTitle();
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
    ////////////////////////////   END    /////////////////////////////////////////

    ///////////////////////////// CLICKS  /////////////////////////////////////////
    @Override
    public void onClick(View view) {
        final int pos = recyclerView.getChildLayoutPosition(view);
        String symbol = stockList.get(pos).getSymbol();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(URL_MARKET+symbol));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View view) {
        final int pos = recyclerView.getChildLayoutPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + stockList.get(pos).getSymbol());
        builder.setIcon(R.drawable.ic_baseline_delete_24);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(!stockList.isEmpty()) {
                    String symbol = stockList.get(pos).getSymbol();
                    stockList.remove(pos);
                    stockAdapter.notifyDataSetChanged();

                    databaseHandler.deleteStock(symbol);
                    //databaseHandler.dumpDbToLog();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onClick: ");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }
    ////////////////////////////   END    /////////////////////////////////////////

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }
}