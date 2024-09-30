package com.example.contri;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {
    Button b;
    EditText Email, Pass, phone;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        Pass = findViewById(R.id.passid);
        Email = findViewById(R.id.emailid);

        b = findViewById(R.id.bt1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the input from the EditText fields inside the onClick method
                String email = Email.getText().toString().trim();
                String password = Pass.getText().toString().trim();

                // Check if email or password are empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(signup.this, "Enter email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(signup.this, "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(signup.this, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new user with the provided email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Save the user in Firebase Database
                                    String userId = mAuth.getCurrentUser().getUid();
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("email", email);
                                    userRef.setValue(userData);

                                    // Sign-up success
                                    Toast.makeText(signup.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(signup.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign-up fails, display a message to the user
                                    Toast.makeText(signup.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
