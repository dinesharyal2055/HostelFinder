package com.example.findhostels.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findhostels.R;
import com.example.findhostels.models.Hostel;
import com.example.findhostels.ui.hostel.HostelDetailsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HostelAdapter extends RecyclerView.Adapter<HostelAdapter.MyViewHolder> {
    Context context;
    List<Hostel> hostels;
    private AlertDialog.Builder builder;
    private FirebaseAuth mAuth;
    private FragmentManager fragmentManager;

    public HostelAdapter(Context context, List<Hostel> hostels, FragmentManager fragmentManager) {
        this.context = context;
        this.hostels = hostels;
        this.fragmentManager = fragmentManager;

    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hostel_card_design, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Hostel hostel = hostels.get(position);
        mAuth = FirebaseAuth.getInstance();
        builder = new AlertDialog.Builder(context);


        DatabaseReference reference = FirebaseDatabase.getInstance(context.getResources().getString(R.string.db_url)).getReference("Hostels")
                .child(hostel.getKey()).child("rating");


        ValueEventListener valueEventListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                int ratingSum = 0;
                float ratingTotal = 0;
                float ratingAvg;

                for (DataSnapshot child : snapshot.getChildren()) {
                    ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                    ratingTotal++;
                }

                if (ratingTotal != 0) {
                    ratingAvg = ratingSum / ratingTotal;
                    holder.myRatings.setRating(ratingAvg);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        reference.addValueEventListener(valueEventListener1);


        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(hostel.getLat(), hostel.getLng(), 1);
            String country = addresses.get(0).getCountryName();
            String city = addresses.get(0).getLocality();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            holder.myLocation.setText(country + ", " + city + ", " + postalCode + ", " + knownName);
        } catch (IOException e) {
            Toast.makeText(context, "Error while fetching address: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        Picasso.get().load(hostel.getImgUrl()).into(holder.myImage);

        holder.myName.setText(hostel.getName());
        holder.myRent.setText("Rs: " + hostel.getRent());
        holder.myType.setText(hostel.getType() + " hostel");

        if (!hostel.isWifi()) {
            holder.myWifi.setText("Wifi Status: Not Available");
        }
        holder.myDesc.setText(hostel.getDesc());


        DatabaseReference mDatabase = FirebaseDatabase.getInstance(context.getResources().getString(R.string.db_url)).getReference("Students")
                .child(mAuth.getCurrentUser().getUid());

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    holder.myDelete.setVisibility(View.INVISIBLE);

                } else {
                    holder.myDelete.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.addValueEventListener(valueEventListener);


        holder.myDelete.setOnClickListener(v -> {

            builder.setMessage("Are you sure to delete??")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {

                        FirebaseDatabase.getInstance(context.getResources().getString(R.string.db_url))
                                .getReference("Hostels")
                                .child(hostel.getKey())
                                .removeValue()
                                .addOnFailureListener(e -> Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.cancel();
                    });
            AlertDialog alert = builder.create();
            alert.setTitle(" Delete Confirmation ");
            alert.show();

        });

    }


    @Override
    public int getItemCount() {
        return hostels.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView myImage, myDelete;
        private RatingBar myRatings;
        private TextView myName, myRent, myLocation, myType, myWifi, myDesc;

        public MyViewHolder(@NonNull @NotNull View itemView) {
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

    public List<Hostel> getHostels() {
        return hostels;
    }

    public void setHostels(List<Hostel> hostels) {
        this.hostels = hostels;
    }
}
