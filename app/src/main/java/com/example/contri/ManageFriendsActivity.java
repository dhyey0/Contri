package com.example.contri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageFriendsActivity extends AppCompatActivity {

    private ListView friendsListView;
    private ArrayList<Friend> friendsList;
    private FriendsAdapter friendsAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference rootDatabaseReference;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        friendsListView = findViewById(R.id.friendsListView);
        refreshButton = findViewById(R.id.refreshButton);
        friendsList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(this, friendsList);
        friendsListView.setAdapter(friendsAdapter);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if user is not logged in
            return;
        }

        userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("friends");
        rootDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

        loadFriends(); // Load friends initially

        // Refresh button to reload the friends list
        refreshButton.setOnClickListener(v -> loadFriends());

        friendsListView.setOnItemClickListener((parent, view, position, id) -> {
            Friend selectedFriend = friendsList.get(position);
            openFriendExpenseHistory(selectedFriend); // Open expense history when a friend is clicked
        });
    }

    private void loadFriends() {
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear(); // Clear the list before adding items

                if (snapshot.exists()) {
                    for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                        String friendId = friendSnapshot.getKey(); // Get the friend ID (key)
                        Long balanceUser1ToUser2 = friendSnapshot.child("balanceUser1ToUser2").getValue(Long.class);
                        Long balanceUser2ToUser1 = friendSnapshot.child("balanceUser2ToUser1").getValue(Long.class);
                        Long netBalance = friendSnapshot.child("netBalance").getValue(Long.class);

                        if (friendId != null && balanceUser1ToUser2 != null && balanceUser2ToUser1 != null && netBalance != null) {
                            // Fetch the friend's email from their main node
                            rootDatabaseReference.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String friendEmail = dataSnapshot.child("ownEmail").getValue(String.class);

                                    if (friendEmail != null) {
                                        // Log to verify the balances and email fetched correctly
                                        Log.d("ManageFriendsActivity", "Net Balance: " + netBalance + " for friend: " + friendEmail);

                                        // Create Friend object and add to list
                                        Friend friend = new Friend(friendId, friendEmail, netBalance);
                                        friendsList.add(friend);
                                        friendsAdapter.notifyDataSetChanged(); // Notify adapter of data change
                                    } else {
                                        Log.e("ManageFriendsActivity", "Friend email is null for friendId: " + friendId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ManageFriendsActivity.this, "Failed to load friend email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("ManageFriendsActivity", "onCancelled: " + error.getMessage());
                                }
                            });
                        } else {
                            Log.e("ManageFriendsActivity", "Balance or Friend ID is null for friendId: " + friendId);
                        }
                    }
                } else {
                    Toast.makeText(ManageFriendsActivity.this, "No friends found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageFriendsActivity.this, "Failed to load friends: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ManageFriendsActivity", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void openFriendExpenseHistory(Friend friend) {
        Intent intent = new Intent(ManageFriendsActivity.this, FriendExpenseHistoryActivity.class);
        intent.putExtra("friendId", friend.getId());
        intent.putExtra("friendEmail", friend.getEmail());
        startActivity(intent);
    }

    private void removeFriend(Friend friend) {
        String friendId = friend.getId();

        // Remove friend from the current user's friends list
        userDatabaseReference.child(friendId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Remove current user's reference from the friend's friends list
                    DatabaseReference friendDatabaseRef = rootDatabaseReference.child(friendId).child("friends").child(mAuth.getCurrentUser().getUid());
                    friendDatabaseRef.removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(ManageFriendsActivity.this, "Friend removed successfully.", Toast.LENGTH_SHORT).show();
                                loadFriends(); // Refresh the list after removal
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ManageFriendsActivity.this, "Failed to remove friend from their list: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e -> Toast.makeText(ManageFriendsActivity.this, "Failed to remove friend: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
