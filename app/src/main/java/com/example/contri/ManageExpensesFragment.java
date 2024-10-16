package com.example.contri;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageExpensesFragment extends Fragment {

    private EditText friendEmailInput, expenseAmountInput;
    private Button addExpenseButton;
    private ListView friendsListView;
    private ArrayList<Friend> friendsList;
    private FriendsAdapter friendsAdapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_expenses, container, false);

        friendEmailInput = view.findViewById(R.id.friendEmailInput);
        expenseAmountInput = view.findViewById(R.id.expenseAmountInput);
        addExpenseButton = view.findViewById(R.id.addExpenseButton);
        friendsListView = view.findViewById(R.id.friendsListView);

        mAuth = FirebaseAuth.getInstance();
        friendsList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(getContext(), friendsList);
        friendsListView.setAdapter(friendsAdapter);

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("friends");
            loadFriendsFromDatabase();
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        addExpenseButton.setOnClickListener(v -> {
            String friendEmail = friendEmailInput.getText().toString().trim();
            String expenseAmountStr = expenseAmountInput.getText().toString().trim();

            if (TextUtils.isEmpty(friendEmail) || TextUtils.isEmpty(expenseAmountStr)) {
                Toast.makeText(getContext(), "Please enter both email and expense", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double expenseAmount = Double.parseDouble(expenseAmountStr);
                updateFriendBalance(friendEmail, expenseAmount);
                expenseAmountInput.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Enter a valid expense", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadFriendsFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Friend friend = snapshot.getValue(Friend.class);
                    friendsList.add(friend);
                }
                friendsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFriendBalance(String friendEmail, double expenseAmount) {
        databaseReference.orderByChild("email").equalTo(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                        double currentBalance = friendSnapshot.child("balance").getValue(Double.class);
                        double newBalance = currentBalance + expenseAmount;
                        friendSnapshot.getRef().child("balance").setValue(newBalance)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(getContext(), "Friend not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to update balance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
