package com.example.findhostels.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.findhostels.R;
import com.example.findhostels.adapter.HostelAdapter;
import com.example.findhostels.models.Hostel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;
    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private final float DEFAULT_ZOOM = 13;
    private LocationCallback locationCallback;
    private Button btnFindHostels;
    private ProgressDialog mDialog;
    private Geocoder geocoder;
    private DatabaseReference mDatabase;
    private List<Hostel> nearestHostel = new ArrayList<>();

    private LinearLayout linearLayout;
    private ImageView closeBtn;
    private RecyclerView recyclerView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mMap);
        supportMapFragment.getMapAsync(HomeFragment.this);
        mDialog = new ProgressDialog(getContext());

        ImageSlider imageSlider = root.findViewById(R.id.image_slider);
        List<SlideModel> slideModels = new ArrayList<>();

        slideModels.add(new SlideModel(R.drawable.students, "Hostel for every students", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.bed, "Comfortable beds and rooms", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.hostel, "Find Best hostel for you", ScaleTypes.CENTER_CROP));


        imageSlider.setImageList(slideModels);
        btnFindHostels = root.findViewById(R.id.btnFindHostels);

        linearLayout = root.findViewById(R.id.linearLayoutNearestPlace);
        closeBtn = root.findViewById(R.id.btnCloseNearestPlace);
        recyclerView = root.findViewById(R.id.recyclerViewNearestPlace);


        closeBtn.setOnClickListener(v -> linearLayout.setVisibility(View.INVISIBLE));

        btnFindHostels.setOnClickListener(v -> {
            findHostel();

            nearestHostel.clear();
        });


        return root;
    }

    private void findHostel() {

        mDialog.setMessage("Looking for hostels...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostels");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    linearLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "No hostels found", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Hostel> hostels = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Hostel hostel = dataSnapshot.getValue(Hostel.class);
                    hostel.setKey(dataSnapshot.getKey());
                    hostels.add(hostel);
                }

                for (int i = 0; i < hostels.size(); i++) {
                    float[] results = new float[1];
                    Location.distanceBetween(hostels.get(i).getLat(), hostels.get(i).getLng(), mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), results);
                    float distance = results[0];

                    int kilometer = (int) (distance / 1000);

                    if (kilometer < 5) {
                        nearestHostel.add(hostels.get(i));
                    }
                }
                addMarkerOnMap();

                showRecyclerViewOfNearestPlaces();

                mDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addValueEventListener(valueEventListener);

    }

    private void showRecyclerViewOfNearestPlaces() {
        linearLayout.setVisibility(View.VISIBLE);

        FragmentManager fragmentManager = getFragmentManager();

        HostelAdapter hostelAdapter = new HostelAdapter(getContext(), nearestHostel, fragmentManager);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(hostelAdapter);
        hostelAdapter.notifyDataSetChanged();

    }

    private void addMarkerOnMap() {
        if (nearestHostel.size() == 0) {
            Toast.makeText(getContext(), "There are no hostels near you", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < nearestHostel.size(); i++) {

            mMap.addMarker(new MarkerOptions().position(new LatLng(nearestHostel.get(i).getLat(), nearestHostel.get(i).getLng()))
                    .anchor(0.5f, 0.5f).title(nearestHostel.get(i).getName()));
            mMap.animateCamera(newLatLngZoom(new LatLng(nearestHostel.get(i).getLat(), nearestHostel.get(i).getLng()), 15));
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceCurrentLocation();
            }
        }
    }


    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        //check permissions
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //call method
            getDeviceCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            return;

        }


        mDialog.setMessage("Getting current location...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(50000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        //check if gps is enabled or not and request user to enable it
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            getDeviceCurrentLocation();

            mDialog.dismiss();
        }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                try {
                    resolvableApiException.startResolutionForResult(getActivity(), 51);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });

    }


    @SuppressLint("MissingPermission")
    private void getDeviceCurrentLocation() {

        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            mLastKnownLocation = task.getResult();
            if (mLastKnownLocation != null) {
                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));

                List<Address> addresses;
                geocoder = new Geocoder(getContext(), Locale.getDefault());
                String country = null;

                String city = null;
                String postalCode = null;
                String knownName = null;


                try {
                    addresses = geocoder.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1);
                    country = addresses.get(0).getCountryName();
                    city = addresses.get(0).getLocality();
                    postalCode = addresses.get(0).getPostalCode();
                    knownName = addresses.get(0).getFeatureName();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                options.title("Your current location is: " + country + ", " + city + ", " + postalCode + ", " + knownName);
                mMap.addMarker(options);
                mMap.animateCamera(newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            } else {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(10000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationRequest == null) {
                            return;
                        }
                        mLastKnownLocation = locationResult.getLastLocation();

                        mMap.moveCamera(newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                };
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}