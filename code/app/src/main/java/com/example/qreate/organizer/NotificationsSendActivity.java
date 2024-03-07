package com.example.qreate.organizer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qreate.EventSpinnerArrayAdapter;
import com.example.qreate.R;

import java.util.ArrayList;

public class NotificationsSendActivity extends AppCompatActivity {
    ArrayList<Event> events;
    Spinner eventsSpinner;
    EventSpinnerArrayAdapter eventSpinnerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_send_notification_screen);
        events = new ArrayList<Event>();

        addEventsInit();

        eventSpinnerArrayAdapter = new EventSpinnerArrayAdapter(this, events);

        //NEED TO GRAB THE ARRAY FROM FIREBASE THEN PARSE IT INTO THIS
        eventsSpinner = findViewById(R.id.send_notifications_screen_spinner);

        eventsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addEventsInit();

        eventSpinnerArrayAdapter.setDropDownViewResource(R.layout.organizer_event_list_recycler_row_layout);

        eventsSpinner.setAdapter(eventSpinnerArrayAdapter);

        //Back Button
        ImageButton backButton = findViewById(R.id.send_notifications_screen_backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    //Temporary to test swap this with the firebase data
    private void addEventsInit(){
        String []cities ={"Edmonton", "Vancouver", "Toronto", "Hamilton", "Denver", "Los Angeles"};
        String []provinces = {"AB", "BC", "ON", "ON", "CO", "CA"};
        for(int i=0;i<cities.length;i++){
            events.add((new Event(cities[i], provinces[i])));
        }
    }
}
