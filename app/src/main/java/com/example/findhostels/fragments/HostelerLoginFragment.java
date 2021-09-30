package com.example.findhostels.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findhostels.DashboardActivity;
import com.example.findhostels.HostelerDashboardActivity;
import com.example.findhostels.MainActivity;
import com.example.findhostels.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;


public class HostelerLoginFragment extends Fragment {

    private EditText email, password;
    private TextView forgotPassword;
    private Button login, register;
    private String emailValidator = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private FirebaseAuth mAuth;
    private TextView loginError;
    private ProgressDialog mDialog;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hosteler_login, container, false);

        email = view.findViewById(R.id.et_login_hosteler_email);
        password = view.findViewById(R.id.et_login_hosteler_password);
        forgotPassword = view.findViewById(R.id.tv_forgot_password_hosteler);
        login = view.findViewById(R.id.btn_login_hosteler);
        register = view.findViewById(R.id.btn_register_hosteler);
        loginError = view.findViewById(R.id.tv_login_hosteler_error);

        email.requestFocus();
        mDialog = new ProgressDialog(getContext());
        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        });


        login.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (!userEmail.matches(emailValidator)) {
                email.setError("Please enter a valid email");
                email.requestFocus();
                return;
            }
            if (userPassword.length() < 6) {
                password.setError("Password must be at least 6 characters");
                password.requestFocus();
                return;
            }

            register.setVisibility(View.INVISIBLE);
            mDialog.setMessage("Authenticating user please wait...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            login.setVisibility(View.INVISIBLE);

            //login with firebase
            mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            mDialog.dismiss();
                            login.setVisibility(View.VISIBLE);
                            loginError.setVisibility(View.VISIBLE);
                            loginError.setText(task.getException().getMessage());
                            Toast.makeText(getContext(), "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference("Hostelers")
                                .child(mAuth.getCurrentUser().getUid());

                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    loginError.setVisibility(View.INVISIBLE);
                                    mDialog.dismiss();
                                    login.setVisibility(View.VISIBLE);
                                    if (getContext() != null) {
                                        Intent intent = new Intent(getContext(), HostelerDashboardActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }

                                } else {
                                    mAuth.signOut();
                                    loginError.setVisibility(View.VISIBLE);
                                    loginError.setText("You are not hosteler please login in different account");
                                    mDialog.dismiss();
                                    login.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                loginError.setVisibility(View.VISIBLE);
                                loginError.setText("" + error.getMessage());
                                mDialog.dismiss();
                                login.setVisibility(View.VISIBLE);
                            }
                        };
                        mDatabase.addValueEventListener(valueEventListener);

                    });

        });

        return view;

    }
}