package com.example.findhostels.ui.gallery;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findhostels.R;
import com.example.findhostels.adapter.HostelAdapter;
import com.example.findhostels.callBack.CardStackCallBack;
import com.example.findhostels.models.Hostel;
import com.example.findhostels.ui.hostel.HostelDetailsFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;

    private AutoCompleteTextView autoCompleteTextView;
    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;
    private ImageView searchHostel;
    private ProgressDialog mDialog;

    private CardStackLayoutManager layoutManager;
    private HostelAdapter adapter;
    private CardStackView stackView;

    List<Hostel> allHostels;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        autoCompleteTextView = root.findViewById(R.id.et_search);
        recyclerView = root.findViewById(R.id.recycleViewStudent);
        searchHostel = root.findViewById(R.id.searchHostel);
        stackView = root.findViewById(R.id.stackView);
        mDialog = new ProgressDialog(getContext());
        recyclerView.setVisibility(View.INVISIBLE);


        getAllHostels();


        layoutManager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (direction == Direction.Right) {
                    Hostel hostel = allHostels.get(layoutManager.getTopPosition() - 1);
                    HostelDetailsFragment hostelDetailsFragment = new HostelDetailsFragment();
                    Bundle extras = new Bundle();
                    extras.putString("id", hostel.getKey());
                    hostelDetailsFragment.setArguments(extras);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.nav_host_fragment_content_dashboard, hostelDetailsFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }

                if (layoutManager.getTopPosition() == adapter.getItemCount() - 5) {
                    paginate();
                }
            }

            @Override
            public void onCardRewound() {
            }

            @Override
            public void onCardCanceled() {

            }

            @Override
            public void onCardAppeared(View view, int position) {
            }

            @Override
            public void onCardDisappeared(View view, int position) {
                allHostels.add(allHostels.get(position));
            }
        });
        layoutManager.setStackFrom(StackFrom.None);
        layoutManager.setVisibleCount(3);
        layoutManager.setTranslationInterval(8.0f);
        layoutManager.setScaleInterval(0.95f);
        layoutManager.setSwipeThreshold(0.3f);
        layoutManager.setMaxDegree(20.0f);
        layoutManager.setDirections(Direction.FREEDOM);
        layoutManager.setCanScrollVertical(false);
        layoutManager.setSwipeableMethod(SwipeableMethod.Manual);
        layoutManager.setOverlayInterpolator(new LinearInterpolator());


        searchHostel.setOnClickListener(v -> {
            String name = autoCompleteTextView.getText().toString().trim();
            if (name.isEmpty()) {
                getAllHostels();
                return;
            }

            mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostels");

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "Hostel " + name + " not found", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<Hostel> searchHostel = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Hostel hostel = dataSnapshot.getValue(Hostel.class);
                        hostel.setKey(dataSnapshot.getKey());
                        searchHostel.add(hostel);
                    }

                    FragmentManager fragmentManager = getFragmentManager();
                    adapter = new HostelAdapter(getContext(), searchHostel, fragmentManager);
                    stackView.setLayoutManager(layoutManager);
                    stackView.setAdapter(adapter);
                    stackView.setItemAnimator(new DefaultItemAnimator());


                    HostelAdapter hostelAdapter = new HostelAdapter(getContext(), searchHostel, fragmentManager);

                    if (getContext() != null) {
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(hostelAdapter);
                        hostelAdapter.notifyDataSetChanged();
                    }

//                    FragmentManager fragmentManager = getFragmentManager();
//                    HostelAdapter hostelAdapter = new HostelAdapter(getContext(), searchHostel, fragmentManager);
//                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
//                    recyclerView.setHasFixedSize(true);
//                    recyclerView.setLayoutManager(layoutManager);
//                    recyclerView.setAdapter(hostelAdapter);
//                    hostelAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            mDatabase.orderByChild("name").equalTo(name).addValueEventListener(valueEventListener);
        });

        return root;
    }

    private void paginate() {
        List<Hostel> old = adapter.getHostels();
        List<Hostel> baru = new ArrayList<>(allHostels);
        CardStackCallBack callBack = new CardStackCallBack(old, baru);
        DiffUtil.DiffResult hasil = DiffUtil.calculateDiff(callBack);
        adapter.setHostels(baru);
        hasil.dispatchUpdatesTo(adapter);
    }

    private void getAllHostels() {
        mDialog.setMessage("Fetching All hostels...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostels");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                allHostels = new ArrayList<>();
                List<String> allHostelString = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Hostel hostel = dataSnapshot.getValue(Hostel.class);
                    allHostelString.add(hostel.getName());
                    hostel.setKey(dataSnapshot.getKey());
                    allHostels.add(hostel);
                }
                FragmentManager fragmentManager = getFragmentManager();
                adapter = new HostelAdapter(getContext(), allHostels, fragmentManager);
                stackView.setLayoutManager(layoutManager);
                stackView.setAdapter(adapter);
                stackView.setItemAnimator(new DefaultItemAnimator());


                HostelAdapter hostelAdapter = new HostelAdapter(getContext(), allHostels, fragmentManager);

                if (getContext() != null) {
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(hostelAdapter);
                    hostelAdapter.notifyDataSetChanged();

                    //for loading name in aCTV
                    ArrayAdapter<String> hostelArrayAdapter = new ArrayAdapter<>(
                            getContext(), android.R.layout.select_dialog_item, allHostelString
                    );
                    autoCompleteTextView.setAdapter(hostelArrayAdapter);
                    autoCompleteTextView.setThreshold(1);
                }


                mDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                mDialog.dismiss();
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addValueEventListener(valueEventListener);
    }

}