package com.example.stock_watch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView symbol;
    TextView name;
    TextView latestprice;
    TextView change;
    TextView changepercent;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        symbol = itemView.findViewById(R.id.VHsymbol);
        name = itemView.findViewById(R.id.VHname);
        latestprice = itemView.findViewById(R.id.VHlatestprice);
        change = itemView.findViewById(R.id.VHchange);
        changepercent = itemView.findViewById(R.id.VHchangepercent);
    }
}