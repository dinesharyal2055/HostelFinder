package com.example.findhostels.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.findhostels.R;
import com.example.findhostels.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class ProfileFragment extends Fragment {

    private EditText et_full_name, et_contact;
    private Button btn_update;
    private FirebaseAuth mAuth;
    private String emailValidator = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        et_full_name = view.findViewById(R.id.et_full_name);
        et_contact = view.findViewById(R.id.et_contact);
        btn_update = view.findViewById(R.id.btn_update);
        mAuth = FirebaseAuth.getInstance();

        getUserDetails();

//        FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Students")
//                .child(mAuth.getCurrentUser().getUid())
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.getResult().exists()) {
//
//                    }else {
//
//                    }
//                });


        return view;
    }

    private void getUserDetails() {


        DatabaseReference mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Students")
                .child(mAuth.getCurrentUser().getUid());


        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostelers")
                            .child(mAuth.getCurrentUser().getUid());


                    ValueEventListener valueEventListener1 = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                            if (map.get("fullName") != null) {
                                et_full_name.setText(map.get("fullName").toString());
                            }

                            if (map.get("contact") != null) {
                                et_contact.setText(map.get("contact").toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    };

                    mDatabase.addValueEventListener(valueEventListener1);


                    btn_update.setOnClickListener(v -> {
                        String fullName = et_full_name.getText().toString().trim();
                        String contact = et_contact.getText().toString().trim();

                        if (fullName.isEmpty()) {
                            et_full_name.setError("Please enter fullname");
                            et_full_name.requestFocus();
                            return;
                        }

                        if (contact.isEmpty()) {
                            et_contact.setError("Please enter contact number");
                            et_contact.requestFocus();
                            return;
                        }


                        FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostelers")
                                .child(mAuth.getCurrentUser().getUid())
                                .child("fullName")
                                .setValue(fullName);
                        FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostelers")
                                .child(mAuth.getCurrentUser().getUid())
                                .child("contact")
                                .setValue(contact);

                        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();


                    });


                    return;
                }


                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                if (map.get("fullName") != null) {
                    et_full_name.setText(map.get("fullName").toString());
                }

                if (map.get("contact") != null) {
                    et_contact.setText(map.get("contact").toString());
                }


                btn_update.setOnClickListener(v -> {
                    String fullName = et_full_name.getText().toString().trim();
                    String contact = et_contact.getText().toString().trim();

                    if (fullName.isEmpty()) {
                        et_full_name.setError("Please enter fullname");
                        et_full_name.requestFocus();
                        return;
                    }

                    if (contact.isEmpty()) {
                        et_contact.setError("Please enter contact number");
                        et_contact.requestFocus();
                        return;
                    }


                    FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Students")
                            .child(mAuth.getCurrentUser().getUid())
                            .child("fullName")
                            .setValue(fullName)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Students")
                                                .child(mAuth.getCurrentUser().getUid())
                                                .child("contact")
                                                .setValue(contact)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        Toast.makeText(getContext(), "" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                });


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.addValueEventListener(valueEventListener);

    }
}