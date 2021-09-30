package com.example.findhostels.ui.notifications;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findhostels.R;
import com.example.findhostels.adapter.HostelAdapter;
import com.example.findhostels.models.Hostel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    private ImageView hostelerImage;
    private TextView hostelerName, hostelerEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog mDialog;

    private RecyclerView recyclerView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        hostelerImage = view.findViewById(R.id.hostelerImage);
        hostelerName = view.findViewById(R.id.hostelerName);
        hostelerEmail = view.findViewById(R.id.hostelerEmail);
        recyclerView = view.findViewById(R.id.rvHostels);

        mDialog = new ProgressDialog(getContext());
        mAuth = FirebaseAuth.getInstance();

        hostelerEmail.setText("" + mAuth.getCurrentUser().getEmail());


        getUserDetails();

        addHostelsView();


        return view;
    }

    private void addHostelsView() {
        mDialog.setMessage("Fetching User hostels...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostels");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Hostel> hostels = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Hostel hostel = dataSnapshot.getValue(Hostel.class);
                        hostel.setKey(dataSnapshot.getKey());

                        hostels.add(hostel);

                        HostelAdapter hostelAdapter = new HostelAdapter(getContext(), hostels, getFragmentManager());
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(hostelAdapter);
                        hostelAdapter.notifyDataSetChanged();
                        mDialog.dismiss();
                    }

                } else {
                    mDialog.dismiss();
                    Toast.makeText(getContext(), "Hostel not available please add hostel", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                mDialog.dismiss();
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.orderByChild("wardenId").equalTo(mAuth.getUid()).addValueEventListener(valueEventListener);


    }

    private void getUserDetails() {
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
    }

}