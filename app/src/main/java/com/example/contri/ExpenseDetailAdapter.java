package com.example.contri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpenseDetailAdapter extends ArrayAdapter<ExpenseDetail> {
    private Context context;
    private ArrayList<ExpenseDetail> expenseDetails;

    public ExpenseDetailAdapter(Context context, ArrayList<ExpenseDetail> expenseDetails) {
        super(context, 0, expenseDetails);
        this.context = context;
        this.expenseDetails = expenseDetails;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expense_detail_item, parent, false);
        }

        // Get the current expense detail
        ExpenseDetail expenseDetail = getItem(position);

        // Lookup views for data population
        TextView payerEmailTextView = convertView.findViewById(R.id.payerEmailTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView amountTextView = convertView.findViewById(R.id.amountTextView);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);

        // Populate the data into the views
        payerEmailTextView.setText(expenseDetail.payerEmail);
        dateTextView.setText(expenseDetail.date);
        amountTextView.setText("₹" + expenseDetail.amount);
        titleTextView.setText(expenseDetail.title);

        return convertView;
    }
}
