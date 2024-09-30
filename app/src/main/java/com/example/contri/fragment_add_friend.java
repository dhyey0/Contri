package com.example.contri;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class fragment_add_friend extends Fragment {

    private EditText friendEmailInput;
    private Button addFriendButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        friendEmailInput = view.findViewById(R.id.friendEmailInput);
        addFriendButton = view.findViewById(R.id.addFriendButton);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("friends");
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return view;
        }

        addFriendButton.setOnClickListener(v -> {
            String friendEmail = friendEmailInput.getText().toString().trim();

            if (TextUtils.isEmpty(friendEmail) || !Patterns.EMAIL_ADDRESS.matcher(friendEmail).matches()) {
                Toast.makeText(getContext(), "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (friendEmail.equals(mAuth.getCurrentUser().getEmail())) {
                Toast.makeText(getContext(), "You can't add yourself", Toast.LENGTH_SHORT).show();
                return;
            }

            addFriendToDatabase(friendEmail);
            friendEmailInput.setText("");
        });

        return view;
    }

    private void addFriendToDatabase(String friendEmail) {
        databaseReference.child(friendEmail.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(getContext(), "Friend already added", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> friendData = new HashMap<>();
                    friendData.put("balance", 0.0);
                    databaseReference.child(friendEmail.replace(".", ",")).setValue(friendData)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Friend added", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add friend", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to access database", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
