package com.example.stock_watch;

import java.io.Serializable;

public class Stock implements Serializable {

    private String symbol;
    private String name;
    private Double latestPrice;
    private Double change;
    private Double changePercent;

    public Stock(String comp_symbol, String comp_name) {
        symbol = comp_symbol;
        name = comp_name;
    }

    public Stock(String symbol, String name, Double latestPrice, Double change, Double changePercent) {
        this.symbol = symbol;
        this.name = name;
        this.latestPrice = latestPrice;
        this.change = change;
        this.changePercent = changePercent;
    }

    public String getSymbol() { return  symbol; }

    public String getName() { return name; }

    public Double getLatestPrice() { return latestPrice; }

    public Double getChange() { return change; }

    public Double getChangePercent() { return changePercent; }
}
