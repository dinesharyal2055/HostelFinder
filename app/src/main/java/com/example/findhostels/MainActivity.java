package com.example.findhostels;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.findhostels.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText fullName, email, password, confirmPassword, phoneNumber;
    private RadioGroup userType;
    private RadioButton student, hosteler;
    private Button login, register;
    private String emailValidator = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private FirebaseAuth mAuth;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to remove the title feature from window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //to hide the action bar
        Objects.requireNonNull(getSupportActionBar()).hide();
        //to make window fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        fullName = findViewById(R.id.et_full_name);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        userType = findViewById(R.id.rg_user_type);
        student = findViewById(R.id.rb_student);
        hosteler = findViewById(R.id.rb_hosteler);
        login = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_register);
        phoneNumber = findViewById(R.id.et_contact);
        student = findViewById(R.id.rb_student);
        hosteler = findViewById(R.id.rb_hosteler);

        fullName.requestFocus();
        mDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();


        login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        register.setOnClickListener(v -> {
            String userName = fullName.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userContact = phoneNumber.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String userConfirmPassword = confirmPassword.getText().toString().trim();

            if (userName.isEmpty()) {
                fullName.setError("Please enter full name");
                fullName.requestFocus();
                return;
            }
            if (userEmail.isEmpty()) {
                email.setError("Please enter email");
                email.requestFocus();
                return;
            }

            if (!userEmail.matches(emailValidator)) {
                email.setError("Please enter a valid email address");
                email.requestFocus();
                return;
            }

            if (userContact.length() < 10) {
                phoneNumber.setError("Please enter a valid number");
                phoneNumber.requestFocus();
                return;
            }

            if (userPassword.length() < 6) {
                password.setError("Please enter password of at least 6 characters");
                password.requestFocus();
                return;
            }

            if (userConfirmPassword.length() < 6) {
                confirmPassword.setError("Please enter password of at least 6 characters");
                confirmPassword.requestFocus();
                return;
            }
            if (!userPassword.equals(userConfirmPassword)) {
                confirmPassword.setError("Password does not match");
                confirmPassword.requestFocus();
                return;
            }

            register.setVisibility(View.INVISIBLE);
            mDialog.setMessage("Creating User please wait...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = new User(userName, userContact);

                            String userType = "";
                            if (student.isChecked()) {
                                userType = "Students";
                            } else if (hosteler.isChecked()) {
                                userType = "Hostelers";
                            }
                            FirebaseDatabase.getInstance(getResources().getString(R.string.db_url)).getReference(userType)
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            mDialog.dismiss();
                                            register.setVisibility(View.VISIBLE);
                                            mAuth.signOut();
                                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            mDialog.dismiss();
                                            register.setVisibility(View.VISIBLE);
                                            Toast.makeText(this, "" + task1.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            mDialog.dismiss();
                            register.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "" + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }


}
