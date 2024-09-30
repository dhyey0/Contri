package com.example.contri;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText friendEmailInput, expenseAmountInput;
    private Button addFriendButton, addExpenseButton;
    private ListView friendsListView;

    private ArrayList<String> friendsList;
    private FriendsAdapter friendsAdapter;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    // Regular expression for validating email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        friendEmailInput = findViewById(R.id.friendEmailInput);
        expenseAmountInput = findViewById(R.id.expenseAmountInput);
        addFriendButton = findViewById(R.id.addFriendButton);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        friendsListView = findViewById(R.id.friendsListView);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Toast.makeText(MainActivity.this, userId, Toast.LENGTH_SHORT).show();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("friends");
        } else {
            Toast.makeText(MainActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return; // Exit if user is not authenticated
        }

        friendsList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(this, friendsList);
        friendsListView.setAdapter(friendsAdapter);

        // Load existing friends and expenses from Firebase
        loadFriendsFromDatabase();
        loadExpensesForCurrentUser(); // Load expenses when activity starts

        // Add Friend button click listener
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String friendEmail = friendEmailInput.getText().toString().trim();

                if (TextUtils.isEmpty(friendEmail)) {
                    Toast.makeText(MainActivity.this, "Please enter a friend's email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidEmail(friendEmail)) {
                    Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the friend is registered in Firebase
                findFriendInFirebase(friendEmail);
                friendEmailInput.setText(""); // Clear input after adding
            }
        });

        // Add Expense button click listener
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String friendEmail = friendEmailInput.getText().toString().trim();
                String expenseAmountStr = expenseAmountInput.getText().toString().trim();

                if (TextUtils.isEmpty(friendEmail) || TextUtils.isEmpty(expenseAmountStr)) {
                    Toast.makeText(MainActivity.this, "Please enter both friend's email and expense", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double expenseAmount = Double.parseDouble(expenseAmountStr);
                    // Call method to save expense for both users
                    saveExpenseForBothUsers(friendEmail, expenseAmount);
                    expenseAmountInput.setText(""); // Clear input after adding
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number for expense", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to check if an email is valid
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // Method to load friends from Firebase
    private void loadFriendsFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendEmail = friendSnapshot.child("email").getValue(String.class);
                    Double balance = friendSnapshot.child("balance").getValue(Double.class);
                    friendsList.add(friendEmail + ": $" + (balance != null ? balance : 0.0));

                    // Log the data for debugging
                    Log.d("FirebaseData", "Friend: " + friendEmail + ", Balance: " + balance);
                }
                friendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load friends.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to find a friend by email in Firebase and add them to your friends list
    private void findFriendInFirebase(String friendEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Friend found, add to the friends list
                    addFriendToDatabase(friendEmail);
                } else {
                    // Friend not found
                    Toast.makeText(MainActivity.this, "Friend not registered in the system", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to search for friend.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to add a new friend to Firebase
    private void addFriendToDatabase(String friendEmail) {
        Map<String, Object> friendData = new HashMap<>();
        friendData.put("email", friendEmail);
        friendData.put("balance", 0.0);

        databaseReference.child(friendEmail.replace(".", "_")).setValue(friendData)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Friend added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to add friend", Toast.LENGTH_SHORT).show());
    }

    // Method to save an expense for both users
    private void saveExpenseForBothUsers(String friendEmail, double expenseAmount) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String expenseId = FirebaseDatabase.getInstance().getReference("expenses").push().getKey(); // Generate a unique key for the expense

        if (expenseId != null) {
            // Create expense data to save
            Map<String, Object> expenseData = new HashMap<>();
            expenseData.put("user1Email", mAuth.getCurrentUser().getEmail());
            expenseData.put("user2Email", friendEmail);
            expenseData.put("amount", expenseAmount);
            expenseData.put("timestamp", System.currentTimeMillis());

            // Save expense for both users
            DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses");
            expensesRef.child(expenseId).setValue(expenseData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                        updateFriendBalance(friendEmail, expenseAmount); // Update friend balance as well
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to save expense", Toast.LENGTH_SHORT).show());
        }
    }

    // Method to update the balance of a friend in Firebase
    private void updateFriendBalance(String friendEmail, double expenseAmount) {
        databaseReference.child(friendEmail.replace(".", "_")).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Double currentBalance = snapshot.getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }

                double newBalance = currentBalance + expenseAmount;
                databaseReference.child(friendEmail.replace(".", "_")).child("balance").setValue(newBalance);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to update balance.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to load expenses for the current user
    private void loadExpensesForCurrentUser() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses");

        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                    String user1Email = expenseSnapshot.child("user1Email").getValue(String.class);
                    String user2Email = expenseSnapshot.child("user2Email").getValue(String.class);
                    double amount = expenseSnapshot.child("amount").getValue(Double.class);

                    // Display expenses that involve the current user
                    if (user1Email.equals(userEmail) || user2Email.equals(userEmail)) {
                        // Log the data for debugging or you can display it in a ListView/RecyclerView
                        Log.d("ExpenseData", "User 1: " + user1Email + ", User 2: " + user2Email + ", Amount: " + amount);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
