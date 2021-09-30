package com.example.findhostels.ui.hosteler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.findhostels.R;
import com.example.findhostels.models.Hostel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class HostelerHomeFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private final float DEFAULT_ZOOM = 13;
    private LocationCallback locationCallback;
    private ProgressDialog mDialog;
    private Geocoder geocoder;

    private DatabaseReference mDatabase;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private Uri mUri;

    private static final int HOSTEL_IMAGE_PICK_CODE = 1005;
    private static final int HOSTEL_PERMISSION_CODE = 1002;
    private Uri mUriHOSTEL;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private StorageReference mStorageRefHostel;


    private ImageView hostelerImage;
    private TextView hostelerName, hostelerEmail;
    private Button btnAddHostel;

    private Dialog popDialog;
    private TextView closePopup;
    private ImageView add_a_hostel_photo;
    private EditText hostel_name, hostel_rent, hostel_description;
    private RadioButton boys, girls, wifi, no_wifi;
    private Button add_hostel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hosteler_home, container, false);
        hostelerImage = view.findViewById(R.id.hostelerImage);
        hostelerName = view.findViewById(R.id.hostelerName);
        hostelerEmail = view.findViewById(R.id.hostelerEmail);
        btnAddHostel = view.findViewById(R.id.btnAddHostel);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mMap);
        supportMapFragment.getMapAsync(HostelerHomeFragment.this);

        popDialog = new Dialog(getContext());

        btnAddHostel.setOnClickListener(v -> showPopUp(v));

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(getContext());
        mStorageRef = FirebaseStorage.getInstance("gs://find-hostels.appspot.com").getReference("uploads");
        mStorageRefHostel = FirebaseStorage.getInstance("gs://find-hostels.appspot.com").getReference("hostels");

        hostelerEmail.setText("" + mAuth.getCurrentUser().getEmail());

        mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostelers")
                .child(mAuth.getCurrentUser().getUid());

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                if (map.get("fullName") != null) {
                    hostelerName.setText(map.get("fullName").toString());
                }

                if (map.get("image") == null) {
                    hostelerImage.setImageResource(R.drawable.add);
                } else {
                    Picasso.get().load(map.get("image").toString()).into(hostelerImage);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.addValueEventListener(valueEventListener);

        hostelerImage.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //permission not granted
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    //show pop up
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system os is less than marshmallow
                pickImageFromGallery();
            }
        });
        return view;
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
                    Toast.makeText(getContext(), "Error fetching place name: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                options.title("Your current location is: " + country + ", " + city + ", " + postalCode + ", " + knownName);
                mMap.addMarker(options);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            } else {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationRequest == null) {
                            return;
                        }
                        mLastKnownLocation = locationResult.getLastLocation();

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                };
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceCurrentLocation();
            }
        }


        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGallery();
                } else {
                    //permission denied
                    Toast.makeText(getContext(), "Permission denied .. !!", Toast.LENGTH_LONG).show();
                }
            }

            case HOSTEL_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGalleryHostel();
                } else {
                    //permission denied
                    Toast.makeText(getContext(), "Permission denied .. !!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image
            mUri = data.getData();

            if (mUri != null) {
                mDialog.setMessage("Uploading Photo...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                StorageReference storageReference = mStorageRef.child(System.currentTimeMillis() + "."
                        + getFileExtension(mUri));

                storageReference.putFile(mUri)
                        .addOnSuccessListener(taskSnapshot -> taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostelers")
                                        .child(mAuth.getCurrentUser().getUid())
                                        .child("image")
                                        .setValue(uri.toString());
                                mDialog.dismiss();
                            }
                        }))
                        .addOnFailureListener(e -> {
                            mDialog.dismiss();
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(getContext(), "No file selected", Toast.LENGTH_LONG).show();
            }
        }


        if (resultCode == RESULT_OK && requestCode == HOSTEL_IMAGE_PICK_CODE) {
            mUriHOSTEL = data.getData();
            add_a_hostel_photo.setImageURI(mUriHOSTEL);
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

        btnAddHostel.setVisibility(View.INVISIBLE);
        mDialog.setMessage("Getting current location...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //check if gps is enabled or not and request user to enable it
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            getDeviceCurrentLocation();
            btnAddHostel.setVisibility(View.VISIBLE);
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

    private void showPopUp(View v) {
        popDialog.setContentView(R.layout.add_hostel_popup_layout);

        closePopup = popDialog.findViewById(R.id.close_popup);
        add_a_hostel_photo = popDialog.findViewById(R.id.add_hostel_images);
        hostel_name = popDialog.findViewById(R.id.et_hostel_name);
        hostel_rent = popDialog.findViewById(R.id.et_rent);
        hostel_description = popDialog.findViewById(R.id.et_desc);
        boys = popDialog.findViewById(R.id.rb_boys_hostel);
        girls = popDialog.findViewById(R.id.rb_girls_hostel);
        wifi = popDialog.findViewById(R.id.rb_wifi_yes);
        no_wifi = popDialog.findViewById(R.id.rb_wifi_no);
        add_hostel = popDialog.findViewById(R.id.btn_add_hostel);

        add_a_hostel_photo.setOnClickListener(v13 -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //permission not granted
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    //show pop up
                    requestPermissions(permissions, HOSTEL_PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGalleryHostel();
                }
            } else {
                //system os is less than marshmallow
                pickImageFromGalleryHostel();
            }
        });

        add_hostel.setOnClickListener(v12 -> {
            String name = hostel_name.getText().toString().trim();
            String rent = hostel_rent.getText().toString().trim();
            String desc = hostel_description.getText().toString().trim();

            if (name.isEmpty()) {
                hostel_name.setError("Please enter hostel name");
                hostel_name.requestFocus();
                return;
            }
            if (rent.isEmpty()) {
                hostel_rent.setError("Please enter hostel rent");
                hostel_rent.requestFocus();
                return;
            }

            if (desc.length() < 50) {
                hostel_description.setError("Please enter hostel description of minimum 50 characters ");
                hostel_description.requestFocus();
                return;
            }

            if (mUriHOSTEL == null) {
                Toast.makeText(getContext(), "Please select the image for hostel", Toast.LENGTH_LONG).show();
                return;
            }

            mDialog.setMessage("Adding your hostel...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            if (mUriHOSTEL != null) {
                StorageReference storageReference = mStorageRefHostel.child(System.currentTimeMillis() + "."
                        + getFileExtension(mUriHOSTEL));

                storageReference.putFile(mUriHOSTEL)
                        .addOnSuccessListener(taskSnapshot -> taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(uri -> {
                            String type = "";
                            if (boys.isChecked()) {
                                type = "boys";
                            } else if (girls.isChecked()) {
                                type = "girls";
                            }

                            boolean isWifi = false;

                            if (wifi.isChecked()) {
                                isWifi = true;
                            } else if (no_wifi.isChecked()) {
                                isWifi = false;
                            }

                            mUriHOSTEL = null;

                            Hostel hostel = new Hostel(name, rent, desc, type, isWifi, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), uri.toString(), mAuth.getUid());
                            DatabaseReference ref = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostels");
                            ref.push()
                                    .setValue(hostel)
                                    .addOnCompleteListener(task -> {

                                        mUriHOSTEL = null;
                                        mDialog.dismiss();
                                        popDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        mDialog.dismiss();
                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }))
                        .addOnFailureListener(e -> {
                            mDialog.dismiss();
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

        });

        closePopup.setOnClickListener(v1 -> popDialog.dismiss());
        popDialog.show();
    }

    private void pickImageFromGalleryHostel() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, HOSTEL_IMAGE_PICK_CODE);
    }
}
