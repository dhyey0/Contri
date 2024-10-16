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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button addFriendButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        emailInput = findViewById(R.id.emailInput);
        addFriendButton = findViewById(R.id.addFriendButton);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString().trim();
                String currentUserEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(AddFriendActivity.this, "Please enter a friend's email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentUserEmail != null && email.equals(currentUserEmail)) {
                    Toast.makeText(AddFriendActivity.this, "You cannot add yourself as a friend.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Begin the process to add the friend
                checkIfFriendExists(mAuth.getCurrentUser().getUid(), email);
            }
        });
    }

    // Check if the friend already exists in the current user's friend list
    private void checkIfFriendExists(String userId, String email) {
        databaseReference.child(userId).child("friends").orderByValue().equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(AddFriendActivity.this, "This friend already exists in your list.", Toast.LENGTH_SHORT).show();
                        } else {
                            addFriend(userId, email);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AddFriendActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Add the friend to the current user's friend list
    private void addFriend(String userId, String email) {
        String friendKey = databaseReference.child(userId).child("friends").push().getKey();
        if (friendKey != null) {
            // Find the user ID by email to add the current user as a friend in their list
            findUserIdByEmail(email, userId);
        } else {
            Toast.makeText(AddFriendActivity.this, "Failed to generate friend key.", Toast.LENGTH_SHORT).show();
        }
    }

    // Find the user by their email and add both as friends with full structure
    private void findUserIdByEmail(String email, String currentUserId) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;

                // Loop through all users to find the user with the given email
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.child("ownEmail").getValue(String.class);

                    if (userEmail != null && userEmail.equals(email)) {
                        String friendUserId = userSnapshot.getKey();
                        String currentUserEmail = mAuth.getCurrentUser().getEmail();

                        // Add both users as friends with full structure
                        addFriendsToEachOther(currentUserId, friendUserId, currentUserEmail, email);
                        userFound = true;
                        break;
                    }
                }

                if (!userFound) {
                    Toast.makeText(AddFriendActivity.this, "No user found with this email.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddFriendActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add both users as friends with full structure (balance and expenses)
    private void addFriendsToEachOther(String currentUserId, String friendUserId, String currentUserEmail, String friendEmail) {
        // Create the initial friend structure for both users
        Map<String, Object> friendData = new HashMap<>();
        friendData.put("balanceUser1ToUser2", 0);
        friendData.put("balanceUser2ToUser1", 0);
        friendData.put("netBalance", 0);
        friendData.put("expenses", new HashMap<>()); // Empty expenses node

        // Add friend to the current user's friend list
        databaseReference.child(currentUserId).child("friends").child(friendUserId).setValue(friendData)
                .addOnSuccessListener(aVoid -> {
                    // Add current user to friend's friend list with same structure
                    databaseReference.child(friendUserId).child("friends").child(currentUserId).setValue(friendData)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(AddFriendActivity.this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                                emailInput.setText(""); // Clear the input field

                                // Trigger email after successful friend addition
                                sendEmailNotification(friendEmail, currentUserEmail);
                            })
                            .addOnFailureListener(e -> Toast.makeText(AddFriendActivity.this, "Failed to add friend to their list.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(AddFriendActivity.this, "Failed to add friend to your list.", Toast.LENGTH_SHORT).show());
    }

    // Method to trigger email intent after adding a friend
    private void sendEmailNotification(String friendEmail, String currentUserEmail) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822"); // MIME type for email
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{friendEmail}); // Recipient email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "You have been added as a friend!"); // Email subject

        // Email body with the current user's email included
        String emailBody = "Hi there!\n\n" + currentUserEmail + " has added you as a friend on the Contri app.\n"
                + "You can start sharing your expenses now!\n\n"
                + "If you need further assistance, feel free to contact them at: " + currentUserEmail;

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody); // Email body content

        try {
            // Launch the email client
            startActivity(Intent.createChooser(emailIntent, "Send email via:"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AddFriendActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
