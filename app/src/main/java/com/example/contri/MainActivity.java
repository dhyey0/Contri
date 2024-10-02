package com.example.contri;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText friendEmailInput, expenseAmountInput;
    private Button addFriendButton, addExpenseButton, viewExpensesButton, settleExpenseButton;
    private ListView expensesListView;

    private ArrayList<String> expensesList;
    private ExpensesAdapter expensesAdapter;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initViews();

        // Firebase authentication and reference setup
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            setupFirebaseReference();
        } else {
            Toast.makeText(MainActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Load friends and expenses on start
        loadFriendsFromDatabase();
        loadExpensesForCurrentUser();

        // Set button click listeners
        setButtonListeners();
    }

    // Initialize all views
    private void initViews() {
        friendEmailInput = findViewById(R.id.friendEmailInput);
        expenseAmountInput = findViewById(R.id.expenseAmountInput);
        addFriendButton = findViewById(R.id.addFriendButton);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        viewExpensesButton = findViewById(R.id.viewExpensesButton);
        settleExpenseButton = findViewById(R.id.settleExpenseButton);
        expensesListView = findViewById(R.id.expensesListView);

        expensesList = new ArrayList<>();
        expensesAdapter = new ExpensesAdapter(this, expensesList);
        expensesListView.setAdapter(expensesAdapter);
    }

    // Setup Firebase reference for current user
    private void setupFirebaseReference() {
        String userId = mAuth.getCurrentUser().getUid();
        Toast.makeText(MainActivity.this, "User: " + userId, Toast.LENGTH_SHORT).show();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("friends");
    }

    // Set click listeners for the buttons
    private void setButtonListeners() {
        addFriendButton.setOnClickListener(view -> handleAddFriend());
        addExpenseButton.setOnClickListener(view -> handleAddExpense());
        viewExpensesButton.setOnClickListener(view -> loadExpensesForCurrentUser());
        settleExpenseButton.setOnClickListener(view -> handleSettleExpense());
    }

    // Handle adding a new friend
    private void handleAddFriend() {
        String friendEmail = friendEmailInput.getText().toString().trim();

        if (validateEmail(friendEmail)) {
            findFriendInFirebase(friendEmail);
            friendEmailInput.setText(""); // Clear input after adding
        }
    }

    // Handle adding a new expense
    private void handleAddExpense() {
        String friendEmail = friendEmailInput.getText().toString().trim();
        String expenseAmountStr = expenseAmountInput.getText().toString().trim();

        if (validateEmail(friendEmail) && validateExpenseAmount(expenseAmountStr)) {
            double expenseAmount = Double.parseDouble(expenseAmountStr);
            saveExpenseForBothUsers(friendEmail, expenseAmount);
            expenseAmountInput.setText(""); // Clear input after adding
        }
    }

    // Handle settling expenses
    private void handleSettleExpense() {
        String friendEmail = friendEmailInput.getText().toString().trim();
        if (TextUtils.isEmpty(friendEmail)) {
            Toast.makeText(MainActivity.this, "Please enter the friend's email to settle the expense", Toast.LENGTH_SHORT).show();
        } else {
            settleExpenseWithFriend(friendEmail);
        }
    }

    // Validate email input
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity.this, "Please enter a friend's email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Validate expense amount input
    private boolean validateExpenseAmount(String amountStr) {
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(MainActivity.this, "Please enter the expense amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Please enter a valid number for expense", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Load friends from Firebase
    private void loadFriendsFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expensesList.clear();
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendEmail = friendSnapshot.child("email").getValue(String.class);
                    Double balance = friendSnapshot.child("balance").getValue(Double.class);
                    expensesList.add(friendEmail + ": $" + (balance != null ? balance : 0.0));
                }
                expensesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load friends.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Find friend by email in Firebase
    private void findFriendInFirebase(String friendEmail) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    addFriendToDatabase(friendEmail);
                } else {
                    Toast.makeText(MainActivity.this, "Friend not registered in the system", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to search for friend.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add new friend to Firebase
    private void addFriendToDatabase(String friendEmail) {
        Map<String, Object> friendData = new HashMap<>();
        friendData.put("email", friendEmail);
        friendData.put("balance", 0.0);

        databaseReference.child(friendEmail.replace(".", "_")).setValue(friendData)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Friend added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to add friend", Toast.LENGTH_SHORT).show());
    }

    // Save expense for both users
    private void saveExpenseForBothUsers(String friendEmail, double expenseAmount) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String expenseId = FirebaseDatabase.getInstance().getReference("expenses").push().getKey();

        if (expenseId != null) {
            Map<String, Object> expenseData = new HashMap<>();
            expenseData.put("user1Email", mAuth.getCurrentUser().getEmail());
            expenseData.put("user2Email", friendEmail);
            expenseData.put("amount", expenseAmount);
            expenseData.put("timestamp", System.currentTimeMillis());
            expenseData.put("settled", false);

            FirebaseDatabase.getInstance().getReference("expenses").child(currentUserId).child(expenseId).setValue(expenseData);
            FirebaseDatabase.getInstance().getReference("expenses").child(friendEmail.replace(".", "_")).child(expenseId).setValue(expenseData);

            updateFriendBalance(friendEmail, expenseAmount);
        }
    }

    // Update friend's balance in Firebase
    private void updateFriendBalance(String friendEmail, double amount) {
        DatabaseReference friendRef = databaseReference.child(friendEmail.replace(".", "_")).child("balance");
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double currentBalance = dataSnapshot.getValue(Double.class);
                if (currentBalance == null) currentBalance = 0.0;
                friendRef.setValue(currentBalance + amount)
                        .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Expense added successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Settle expense with friend
    private void settleExpenseWithFriend(String friendEmail) {
        databaseReference.child(friendEmail.replace(".", "_")).child("balance").setValue(0.0)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Expense settled", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to settle expense", Toast.LENGTH_SHORT).show());
    }

    // Load expenses for the current user
    private void loadExpensesForCurrentUser() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(currentUserId);

        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expensesList.clear();
                for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                    String user1Email = expenseSnapshot.child("user1Email").getValue(String.class);
                    String user2Email = expenseSnapshot.child("user2Email").getValue(String.class);
                    Double amount = expenseSnapshot.child("amount").getValue(Double.class);
                    Boolean settled = expenseSnapshot.child("settled").getValue(Boolean.class);

                    if (user1Email != null && user2Email != null && amount != null) {
                        String expenseDetails = user1Email + " owes " + user2Email + ": $" + amount + (settled ? " (Settled)" : " (Pending)");
                        expensesList.add(expenseDetails);
                    }
                }
                expensesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
