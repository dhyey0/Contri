package com.example.contri;

import android.os.Bundle;
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

public class ViewExpensesActivity extends AppCompatActivity {

    private ListView expensesListView;
    private ArrayList<ExpenseDetail> expensesList;
    private ExpenseDetailAdapter expensesAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseReference;
    private String currentUserId, currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        expensesListView = findViewById(R.id.expensesListView);
        expensesList = new ArrayList<>();
        expensesAdapter = new ExpenseDetailAdapter(this, expensesList);
        expensesListView.setAdapter(expensesAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Fetch the current user's email
        userDatabaseReference.child("ownEmail").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserEmail = dataSnapshot.getValue(String.class);
                loadExpenses(); // Load expenses after fetching current user's email
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewExpensesActivity.this, "Failed to load user email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExpenses() {
        userDatabaseReference.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
                expensesList.clear();

                // Loop through each friend
                for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();

                    // Fetch friend's email from the main "users" node using friendId
                    FirebaseDatabase.getInstance().getReference("users").child(friendId).child("ownEmail")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot emailSnapshot) {
                                    String friendEmail = emailSnapshot.getValue(String.class);

                                    // Loop through each expense of the friend
                                    DataSnapshot expensesSnapshot = friendSnapshot.child("expenses");
                                    for (DataSnapshot expenseSnapshot : expensesSnapshot.getChildren()) {
                                        Expense expense = expenseSnapshot.getValue(Expense.class);
                                        if (expense != null) {
                                            String payerEmail = expense.getPaidBy().equals(currentUserId) ? "BY YOU TO " + friendEmail : friendEmail;
                                            ExpenseDetail detail = new ExpenseDetail(payerEmail, expense.getTime(), expense.getAmount(), expense.getTitle());
                                            expensesList.add(detail);
                                        }
                                    }

                                    // Notify the adapter after processing each friend's expenses
                                    expensesAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(ViewExpensesActivity.this, "Failed to load friend's email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewExpensesActivity.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
