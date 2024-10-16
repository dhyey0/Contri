package com.example.contri;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FriendExpenseHistoryActivity extends AppCompatActivity {

    private ListView expenseListView;
    private ArrayList<String> expenseList;
    private ArrayAdapter<String> expenseAdapter;
    private Button addExpenseButton, settleBalancesButton;
    private EditText amountEditText, titleEditText;
    private RadioGroup paidByRadioGroup;
    private DatabaseReference userDatabaseReference, friendDatabaseReference;
    private FirebaseAuth mAuth;
    private String friendId, currentUserId;
    private String currentUserEmail, friendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_expense_history);

        // Initialize UI components
        expenseListView = findViewById(R.id.expenseListView);
        amountEditText = findViewById(R.id.amountEditText);
        titleEditText = findViewById(R.id.titleEditText);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        settleBalancesButton = findViewById(R.id.settleBalancesButton);
        paidByRadioGroup = findViewById(R.id.paidByRadioGroup);

        // Setup list and adapter
        expenseList = new ArrayList<>();
        expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseList);
        expenseListView.setAdapter(expenseAdapter);

        // Get Firebase instance and current user info
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        friendId = getIntent().getStringExtra("friendId");
        friendEmail = getIntent().getStringExtra("friendEmail");
        currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Set database references
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("friends").child(friendId);
        friendDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(friendId).child("friends").child(currentUserId);

        // Load existing expenses
        loadExpenses();

        // Adding an expense
        addExpenseButton.setOnClickListener(v -> addExpense());

        // Settling the balances
        settleBalancesButton.setOnClickListener(v -> settleBalances());
    }

    private void loadExpenses() {
        userDatabaseReference.child("expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                        String title = expenseSnapshot.child("title").getValue(String.class);
                        Long amount = expenseSnapshot.child("amount").getValue(Long.class);
                        String time = expenseSnapshot.child("time").getValue(String.class);
                        String paidBy = expenseSnapshot.child("paidBy").getValue(String.class);

                        // Show which user paid for the expense and details
                        String expenseDetail = String.format("%s paid ₹%d for %s on %s", paidBy.equals(currentUserId) ? "You" : friendEmail, amount, title, time);
                        expenseList.add(expenseDetail);
                    }
                    expenseAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendExpenseHistoryActivity.this, "Failed to load expenses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addExpense() {
        String title = titleEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        int selectedPaidById = paidByRadioGroup.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr) || selectedPaidById == -1) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = Long.parseLong(amountStr);
        RadioButton selectedPaidByRadioButton = findViewById(selectedPaidById);
        String paidById = selectedPaidByRadioButton.getText().toString().equals("You") ? currentUserId : friendId;

        // Get current time for the "time" field
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a unique ID for the expense
        String expenseId = userDatabaseReference.child("expenses").push().getKey();

        // Creating expense object
        Expense expense = new Expense(expenseId, title, amount, paidById, currentUserId, currentTime);

        // Add the expense to both users' friend nodes
        userDatabaseReference.child("expenses").child(expenseId).setValue(expense);
        friendDatabaseReference.child("expenses").child(expenseId).setValue(expense);

        updateBalances(amount, paidById.equals(currentUserId));

        titleEditText.setText("");
        amountEditText.setText("");
        paidByRadioGroup.clearCheck();

        // Send email notification to the friend
        sendEmailNotificationForExpense(title, amount);
    }

    private void updateBalances(long amount, boolean currentUserPaid) {
        userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentBalanceUser1ToUser2 = snapshot.child("balanceUser1ToUser2").getValue(Long.class);
                long currentBalanceUser2ToUser1 = snapshot.child("balanceUser2ToUser1").getValue(Long.class);

                if (currentUserPaid) {
                    currentBalanceUser1ToUser2 += amount;
                } else {
                    currentBalanceUser2ToUser1 += amount;
                }

                long netBalance = currentBalanceUser1ToUser2 - currentBalanceUser2ToUser1;

                // Update balances for both users
                userDatabaseReference.child("balanceUser1ToUser2").setValue(currentBalanceUser1ToUser2);
                userDatabaseReference.child("balanceUser2ToUser1").setValue(currentBalanceUser2ToUser1);
                userDatabaseReference.child("netBalance").setValue(netBalance);

                friendDatabaseReference.child("balanceUser1ToUser2").setValue(currentBalanceUser2ToUser1);
                friendDatabaseReference.child("balanceUser2ToUser1").setValue(currentBalanceUser1ToUser2);
                friendDatabaseReference.child("netBalance").setValue(-netBalance); // Friend's net balance is the negative of current user's
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendExpenseHistoryActivity.this, "Failed to update balances: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void settleBalances() {
        // Set all balances to zero for both users
        userDatabaseReference.child("balanceUser1ToUser2").setValue(0);
        userDatabaseReference.child("balanceUser2ToUser1").setValue(0);
        userDatabaseReference.child("netBalance").setValue(0);

        friendDatabaseReference.child("balanceUser1ToUser2").setValue(0);
        friendDatabaseReference.child("balanceUser2ToUser1").setValue(0);
        friendDatabaseReference.child("netBalance").setValue(0);

        Toast.makeText(FriendExpenseHistoryActivity.this, "Balances have been settled!", Toast.LENGTH_SHORT).show();

        // Send email notification to the friend
        sendEmailNotificationForSettlement();
    }

    // Method to send email when an expense is added
    private void sendEmailNotificationForExpense(String title, long amount) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822"); // MIME type for email
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{friendEmail}); // Friend's email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Expense Added");

        String emailBody = "Hi there!\n\nA new expense has been added to your account with the following details:\n"
                + "Expense: " + title + "\n"
                + "Amount: ₹" + amount + "\n"
                + "Added by: " + currentUserEmail + "\n\n"
                + "Please check your app for more details.";

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via:"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(FriendExpenseHistoryActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to send email when balances are settled
    private void sendEmailNotificationForSettlement() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822"); // MIME type for email
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{friendEmail}); // Friend's email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Balances Settled");

        String emailBody = "Hi there!\n\nYour balances with " + currentUserEmail + " have been settled.\n"
                + "Please check your app for confirmation.";

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via:"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(FriendExpenseHistoryActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
