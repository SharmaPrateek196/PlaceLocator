package com.example.placelocator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class DestinationsListAdapter extends RecyclerView.Adapter<DestinationsListAdapter.MyViewHolder> {
    Context mCtx;
    ArrayList<PlacePojo> list;

    public DestinationsListAdapter(){}

    public DestinationsListAdapter(Context mCtx, ArrayList<PlacePojo> list) {
        this.mCtx = mCtx;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf=LayoutInflater.from(mCtx);
        View v=inf.inflate(R.layout.cardview,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final PlacePojo placePojo=list.get(position);
        holder.tv_dest_name.setText(placePojo.getName());
        holder.tv_rating.setText(String.valueOf(placePojo.getRating()));
        holder.tv_distance.setText(String.format("%.2f", placePojo.getDistance()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(mCtx,DetailsActivity.class);
                intent.putExtra("PlaceObj",placePojo);
                mCtx.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_distance,tv_dest_name,tv_rating;
        CardView cardView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_dest_name=(TextView) itemView.findViewById(R.id.tv_dest_name);
            tv_distance=(TextView) itemView.findViewById(R.id.tv_distance);
            tv_rating=(TextView) itemView.findViewById(R.id.tv_rating_value);
            cardView=(CardView)itemView.findViewById(R.id.cardview);
        }
    }

}
