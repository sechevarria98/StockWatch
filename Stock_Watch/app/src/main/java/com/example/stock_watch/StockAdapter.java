package com.example.stock_watch;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainAct;

    StockAdapter(List<Stock> stockList, MainActivity ma) {
        this.stockList = stockList;
        this.mainAct = ma;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row, parent, false);
        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        DecimalFormat d = new DecimalFormat("#.##");

        if(stock.getChange() < 0) {
            holder.changepercent.setText("(" + d.format(stock.getChangePercent()) + "%)");
            holder.change.setText("▼ " + d.format(stock.getChange()));
            holder.symbol.setTextColor(Color.RED);
            holder.name.setTextColor(Color.RED);
            holder.latestprice.setTextColor(Color.RED);
            holder.change.setTextColor(Color.RED);
            holder.changepercent.setTextColor(Color.RED);
        }
        else {
            holder.changepercent.setText("(" + d.format(stock.getChangePercent()) + "%)");
            holder.change.setText("▲ " + d.format(stock.getChange()));
            holder.symbol.setTextColor(Color.GREEN);
            holder.name.setTextColor(Color.GREEN);
            holder.latestprice.setTextColor(Color.GREEN);
            holder.change.setTextColor(Color.GREEN);
            holder.changepercent.setTextColor(Color.GREEN);
        }

        holder.symbol.setText(stock.getSymbol());
        holder.name.setText(stock.getName());
        holder.latestprice.setText(String.valueOf(d.format(stock.getLatestPrice())));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
