package com.example.contri;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_add_friend) {
                    Intent addFriendIntent = new Intent(MainActivity.this, AddFriendActivity.class);
                    startActivity(addFriendIntent);
                    drawerLayout.closeDrawers();
                    return true;
                } else if (item.getItemId() == R.id.nav_manage_friends) {
                    Intent manageFriendsIntent = new Intent(MainActivity.this, ManageFriendsActivity.class);
                    startActivity(manageFriendsIntent);
                    drawerLayout.closeDrawers();
                    return true;
                } else if (item.getItemId() == R.id.nav_view_expenses) {
                    Intent viewExpensesIntent = new Intent(MainActivity.this, ViewExpensesActivity.class);
                    startActivity(viewExpensesIntent);
                    drawerLayout.closeDrawers();
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Optional: Set toggle button to open/close the drawer
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }
}
