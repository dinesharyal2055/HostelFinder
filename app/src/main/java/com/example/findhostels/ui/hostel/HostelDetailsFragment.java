package com.example.findhostels.ui.hostel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findhostels.R;
import com.example.findhostels.models.Hostel;
import com.example.findhostels.models.User;
import com.example.findhostels.ui.gallery.GalleryFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Map;


public class HostelDetailsFragment extends Fragment {


    private ImageView closeBtn, hostelImage, call, imageViewHosteler;
    private TextView tvDesc, tvName, tvWname, tvPhoneNo, tvLocation, tvType, tvWifi, tvRent;
    private ProgressDialog mDialog;
    private RatingBar ratingBar;

    private String id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);
        closeBtn = view.findViewById(R.id.closeBtn);
        tvDesc = view.findViewById(R.id.desc);
        tvName = view.findViewById(R.id.name);
        tvWname = view.findViewById(R.id.wardenName);
        tvPhoneNo = view.findViewById(R.id.phoneNo);
        tvLocation = view.findViewById(R.id.location);
        tvType = view.findViewById(R.id.hostelType);
        tvWifi = view.findViewById(R.id.wifi);
        hostelImage = view.findViewById(R.id.imageHostelView);
        tvRent = view.findViewById(R.id.rent);
        call = view.findViewById(R.id.iv_call);
        imageViewHosteler = view.findViewById(R.id.imageViewHosteler);
        ratingBar = view.findViewById(R.id.ratingbar);

        mDialog = new ProgressDialog(getContext());


        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> FirebaseDatabase.getInstance(getActivity().getResources().getString(R.string.db_url)).getReference("Hostels")
                .child(id)
                .child("rating")
                .push()
                .setValue(rating)

                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                 //   System.out.println(fromUser);
                 //   System.out.println(rating);
//                   System.out.println(ratingBar.getNumStars());
                })

        );

        Bundle bundle = getArguments();
        if (bundle != null) {

            mDialog.setMessage("Fetching Hostel Details...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            id = bundle.getString("id");


            DatabaseReference ref = FirebaseDatabase.getInstance(getActivity().getResources().getString(R.string.db_url)).getReference("Hostels")
                    .child(id);

            ValueEventListener valueEventListener1 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {


                    int ratingSum = 0;
                    float ratingTotal = 0;
                    float ratingAvg;

                    for (DataSnapshot child : snapshot.getChildren()) {
                        for (DataSnapshot grandChild : child.getChildren()) {
                            ratingSum = ratingSum + Integer.valueOf(grandChild.getValue().toString());
                            ratingTotal++;
                        }

                        if (ratingTotal != 0) {
                            ratingAvg = ratingSum / ratingTotal;
                            ratingBar.setRating(ratingAvg);
                        }

                    }

                    Hostel hostel = snapshot.getValue(Hostel.class);

                    Picasso.get().load(hostel.getImgUrl()).into(hostelImage);

                    tvRent.setText("Rs: " + hostel.getRent() + " (monthly)");

                    tvName.setText(hostel.getName());

                    try {
                        //address
                        if (getContext() != null) {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(hostel.getLat(), hostel.getLng(), 1);
                            String country = addresses.get(0).getCountryName();
                            String city = addresses.get(0).getLocality();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName();
                            tvLocation.setText(country + ", " + city + ", " + postalCode + ", " + knownName);
                        }

                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Error while fetching address: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    tvType.setText(hostel.getType() + " hostel");

                    Boolean wifi = hostel.isWifi();
                    String isWifi;
                    if (wifi) {
                        isWifi = "Wifi Available";
                    } else {
                        isWifi = "Wifi not Available";
                    }
                    tvWifi.setText(isWifi);

                    tvDesc.setText(hostel.getDesc());

                    if (getActivity() != null) {
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance(getActivity().getResources().getString(R.string.db_url)).getReference("Hostelers")
                                .child(hostel.getWardenId());


                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    tvWname.setText("Anynomous");
                                    tvPhoneNo.setVisibility(View.INVISIBLE);
                                    call.setVisibility(View.INVISIBLE);
                                    imageViewHosteler.setVisibility(View.INVISIBLE);
                                    return;
                                }

                                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                                tvPhoneNo.setVisibility(View.VISIBLE);
                                call.setVisibility(View.VISIBLE);

                                call.setOnClickListener(v -> {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:+977" + map.get("contact").toString()));
                                    getActivity().startActivity(intent);
                                });
                                imageViewHosteler.setVisibility(View.VISIBLE);
                                tvPhoneNo.setText("+977-" + map.get("contact").toString());
                                tvWname.setText("Warden :" + map.get("fullName").toString());
                                Picasso.get().load(map.get("image").toString()).into(imageViewHosteler);

                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        };

                        mDatabase.addValueEventListener(valueEventListener);
                    }


                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            ref.addValueEventListener(valueEventListener1);

            mDialog.dismiss();

        }

        closeBtn.setOnClickListener(v -> {

            GalleryFragment galleryFragment = new GalleryFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment_content_dashboard, galleryFragment);
            ft.addToBackStack(null);
            ft.commit();

        });

        return view;
    }
}