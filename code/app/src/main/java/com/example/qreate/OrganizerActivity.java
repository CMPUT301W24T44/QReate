package com.example.qreate;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrganizerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_handler);

        BottomNavigationView bottomNavigationView = findViewById(R.id.organizer_handler_navigation_bar);
        bottomNavigationView.setSelectedItemId(R.id.defaultNavPlaceholder);
        //bottomNavigationView.setVisibility(View.INVISIBLE);


        //I AM STILL TESTING OUT HOW THIS WORKS
        HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.organizer_handler_frame,homeScreenFragment).commit();
    }


}
