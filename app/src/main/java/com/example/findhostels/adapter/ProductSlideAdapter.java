package com.example.findhostels.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findhostels.R;
import com.example.findhostels.models.Hostel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProductSlideAdapter extends RecyclerView.Adapter<ProductSlideAdapter.ViewHolder> {


    private Context context;
    private List<Hostel> hostels;


    public List<Hostel> getHostels() {
        return hostels;
    }

    public void setHostels(List<Hostel> hostels) {
        this.hostels = hostels;
    }

    public ProductSlideAdapter(Context context, List<Hostel> hostels) {
        this.context = context;
        this.hostels = hostels;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.hostel_card_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Hostel hostel = hostels.get(position);
    }

    @Override
    public int getItemCount() {
        return hostels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView myImage, myDelete;
        private RatingBar myRatings;
        private TextView myName, myRent, myLocation, myType, myWifi, myDesc;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            myImage = itemView.findViewById(R.id.myImage);
            myDelete = itemView.findViewById(R.id.myDelete);
            myRatings = itemView.findViewById(R.id.myRatings);
            myRent = itemView.findViewById(R.id.myRent);
            myLocation = itemView.findViewById(R.id.myLocation);
            myType = itemView.findViewById(R.id.myType);
            myWifi = itemView.findViewById(R.id.myWifi);
            myDesc = itemView.findViewById(R.id.myDesc);
            myName = itemView.findViewById(R.id.myName);
        }
    }
}
