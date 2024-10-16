package com.example.contri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsAdapter extends ArrayAdapter<Friend> {

    public FriendsAdapter(Context context, ArrayList<Friend> friends) {
        super(context, 0, friends);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Friend friend = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_item, parent, false);
        }

        // Lookup view for data population
        TextView friendEmailTextView = convertView.findViewById(R.id.friendEmailTextView);
        TextView netBalanceTextView = convertView.findViewById(R.id.netBalanceTextView);

        // Populate the data into the template view using the data object
        if (friend != null) {
            friendEmailTextView.setText(friend.getEmail());
            netBalanceTextView.setText("Net Balance: " + friend.getNetBalance() + "₹"); // Display net balance
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
