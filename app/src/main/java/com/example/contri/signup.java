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
    EditText Email, Pass;
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

                                    // Create a map for user data with the correct structure
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("ownEmail", email);  // Store own email

                                    // Initialize the 'friends' node with an empty friend structure
                                    Map<String, Object> friends = new HashMap<>();
                                    Map<String, Object> friendDetails = new HashMap<>();

                                    // Initialize the balances and expenses for the friend
                                    friendDetails.put("balanceUser1ToUser2", 0);
                                    friendDetails.put("balanceUser2ToUser1", 0);
                                    friendDetails.put("netBalance", 0);

                                    // Initialize empty expenses for the friend
                                    Map<String, Object> expenses = new HashMap<>();
                                    friendDetails.put("expenses", expenses);  // Empty expenses node

                                    friends.put("friendUserId1", friendDetails);  // Add a friend (use actual friend ID in practice)

                                    // Add the 'friends' node to the user data
                                    userData.put("friends", friends);

                                    // Save the data structure to Firebase
                                    userRef.setValue(userData)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Sign-up success
                                                        Toast.makeText(signup.this, "Sign-up successful. Welcome!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(signup.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // If saving to database fails, show a message
                                                        Toast.makeText(signup.this, "Failed to create user in database.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
