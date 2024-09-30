package com.example.contri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> friendsList;

    public FriendsAdapter(Context context, ArrayList<String> friendsList) {
        super(context, R.layout.friend_item, friendsList);
        this.context = context;
        this.friendsList = friendsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.friend_item, parent, false);

        TextView friendNameTextView = rowView.findViewById(R.id.friendNameTextView);
        friendNameTextView.setText(friendsList.get(position));

        return rowView;
    }
}

