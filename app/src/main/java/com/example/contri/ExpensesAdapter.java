package com.example.contri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpensesAdapter extends ArrayAdapter<String> {

    public ExpensesAdapter(Context context, ArrayList<String> expenses) {
        super(context, 0, expenses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String expense = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_expense, parent, false);
        }

        // Lookup view for data population
        TextView expenseText = convertView.findViewById(R.id.expenseText);

        // Populate the data into the template view using the data object
        expenseText.setText(expense);

        // Return the completed view to render on screen
        return convertView;
    }
}
