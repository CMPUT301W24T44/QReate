package com.example.qreate.organizer.qrmenu;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.qreate.R;
import com.example.qreate.administrator.AdministratorEventDetailsFragment;
import com.example.qreate.attendee.AttendeeSignedUpEventsDetailsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


/**
 * Fragment that allows the user to delete and see event details
 * @author Harshita
 */
public class OrganizerEventDetailsFragment extends Fragment {
    private ImageView poster;
    private TextView eventName;
    private TextView eventOrganizer;
    private TextView eventDescription;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventLocation;
    private Button backButton;
    private Button deleteButton;
    private FirebaseFirestore db;
    private EventDeletionListener eventDeletionListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attendee_event_view_details, container, false);
        db = FirebaseFirestore.getInstance();
        poster = view.findViewById(R.id.event_details_poster);
        eventName = view.findViewById(R.id.event_details_name);
        eventOrganizer = view.findViewById(R.id.event_details_organizer);
        eventDescription = view.findViewById(R.id.event_details_description);
        eventDate = view.findViewById(R.id.event_details_date);
        eventTime = view.findViewById(R.id.event_details_time);
        eventLocation = view.findViewById(R.id.event_details_location);

        backButton = view.findViewById(R.id.event_details_back_button);
        deleteButton = view.findViewById(R.id.event_details_delete_button);
        deleteButton.setVisibility(View.VISIBLE);
        Button signUpButton = view.findViewById(R.id.event_details_signup_button);
        signUpButton.setVisibility(View.INVISIBLE);

        // Retrieve the event ID passed from the previous fragment
        Bundle args = getArguments();
        String eventId;
        if (args != null) {
            eventId = args.getString("eventId");
        } else {
            eventId = null;
        }

        displayEventDetails(eventId);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop the current fragment off the stack to return to the previous one
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete event reference from each attendee's signup_event_list, delete event reference from organizer's event_list & delete event document from event collection
                deleteEvent(eventId);
                notifyEventDeletion();
                Log.d("Interface", "notifyEventDeletion function called");

                // pop back fragment
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {

                    getParentFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }


    public static OrganizerEventDetailsFragment newInstance(String eventId) {
        OrganizerEventDetailsFragment fragment = new OrganizerEventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    private void displayEventDetails(String eventId) {
        if (eventId != null) {
            db.collection("Events").document(eventId)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Extract event details from the document and update the UI
                                eventName.setText(document.getString("name"));
                                String posterPath = document.getString("poster");
                                if (posterPath != null && !posterPath.isEmpty()) {
                                    loadPosterImage(posterPath);
                                }
                                String device_id = document.getString("org_device_id");
                                db.collection("Users")
                                        .whereEqualTo("device_id", device_id)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                                                // Assuming device_id is unique, get the first document.
                                                QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task1.getResult().getDocuments().get(0);
                                                String orgName = doc.getString("name");
                                                eventOrganizer.setText(orgName);
                                            }
                                        });
                                eventDescription.setText(document.getString("description"));
                                Timestamp dateTimestamp = document.getTimestamp("date");
                                if (dateTimestamp != null) {
                                    // Format the Timestamp as a String to include only the date part in dd-MM-yyyy format
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                    String formattedDate = dateFormat.format(dateTimestamp.toDate());
                                    eventDate.setText(formattedDate);
                                }
                                eventTime.setText(document.getString("timeOfEvent"));
                                // poster.setText(document.getString("poster"));
                                // eventLocation.setText(document.getString("location"));
                            } else {
                                Log.d("Firestore", "Error getting documents: ", task.getException());
                            }
                        } else {
                            Log.d("Firestore", "Task Failure: ", task.getException());
                        }
                    });
        }
    }

    private void deleteEvent(String eventId) {
        // delete event reference from each attendee's signup_event_list
        removeEventFromAttendee(eventId, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // after removing from attendees, delete event reference from organizer's event_list
                removeEventFromOrganizer(eventId, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // after removing from attendee & organizer, delete event document from event collection
                        removeEvent(eventId, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //now that all deletion steps are complete, call notifyEventDeletion
                                notifyEventDeletion();
                            }
                        });
                    }
                });
            }
        });
    }

    private void removeEventFromAttendee(String eventId, OnCompleteListener<Void> completionListener) {
        DocumentReference eventRef = db.collection("Events").document(eventId);
        db.collection("Attendees")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create a list to hold all the update tasks
                        List<Task<Void>> updateTasks = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            List<DocumentReference> signUpEventList = (List <DocumentReference>) document.get("signup_event_list");
                            if (signUpEventList != null && signUpEventList.contains(eventRef)) {
                                // Add each update operation's Task to the list
                                Task<Void> updateTask = db.collection("Attendees")
                                        .document(document.getId())
                                        .update("signup_event_list", FieldValue.arrayRemove(eventRef));
                                updateTasks.add(updateTask);
                            }
                        }
                        // Wait for all update tasks to complete
                        Tasks.whenAllComplete(updateTasks)
                                .addOnCompleteListener(tasks -> {
                                    // All update operations are complete at this point
                                    Task<Void> completionTask = Tasks.forResult(null);
                                    completionListener.onComplete(completionTask);
                                });

                    } else {
                        Log.w("Firestore", "Error querying documents: ", task.getException());
                        // Signal completion with failure if the initial query failed
                        Task<Void> failureTask = Tasks.forException(task.getException());
                        completionListener.onComplete(failureTask);
                    }
                });
    }

    private void removeEventFromOrganizer(String eventId, OnCompleteListener<Void> completionListener) {
        DocumentReference eventRef = db.collection("Events").document(eventId);
       eventRef
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // retrieving the device_id of the organizer
                        String org_device_id = documentSnapshot.getString("org_device_id");
                        Log.d("Firestore", "Organizer Device ID: " + org_device_id);

                        // finding the event organizer of the event using the device_id and deleting the event from their event_list
                        db.collection("Organizers")
                                .whereEqualTo("device_id", org_device_id)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot organizerSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                        DocumentReference organizerRef = organizerSnapshot.getReference();
                                        Task<Void> updateTask = organizerRef.update("events_list", FieldValue.arrayRemove(eventRef));
                                        updateTask.addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Event successfully removed from organizer's event_list");
                                            // Notify the completion listener only after the update is successful
                                            completionListener.onComplete(null);
                                        }).addOnFailureListener(e -> {
                                            Log.w("Firestore", "Error updating organizer", e);
                                            // Notify the completion listener in case of failure as well
                                            completionListener.onComplete(null);
                                        });

                                    } else {
                                        Log.d("Firestore", "Organizer with given device ID not found");
                                        // If no organizer is found, consider it as operation completed
                                        completionListener.onComplete(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("Firestore", "Error finding organizer", e);
                                    // If there's an error fetching the organizer, notify completion listener
                                    completionListener.onComplete(null);
                                });
                    } else {
                        Log.d("Firestore", "Event document does not exist");
                        // If the event document does not exist, consider it as operation completed
                        completionListener.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error getting document", e);
                    // If there's an error fetching the event document, notify completion listener
                    completionListener.onComplete(null);
                });
    }

    private void removeEvent(String eventId, OnCompleteListener<Void> completionListener) {
        db.collection("Events").document(eventId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // If the deletion is successful, invoke the completion listener with success
                            completionListener.onComplete(task);
                        }
                    }
                });
    }


    // Interface for callback
    public interface EventDeletionListener {
        void onEventDeleted();
    }

    // Method to set the listener
    public void setEventDeletionListener(EventDeletionListener eventDeletionListener) {
        this.eventDeletionListener = eventDeletionListener;
        Log.d("Interface", "EventDeletionListener has been set in OrganizerEventDetailsFragment");
    }

    // Invoke this method when the event is successfully deleted
    private void notifyEventDeletion() {
        if (eventDeletionListener != null) {
            eventDeletionListener.onEventDeleted();
            Log.d("Interface", "EventDeletionListener (Activity) calls onEventDeleted function overidden in the activity");
        }
    }

    private void loadPosterImage(String posterPath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference posterRef = storage.getReference(posterPath);

        posterRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(OrganizerEventDetailsFragment.this)
                        .load(uri.toString())
                        .into(poster);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firestore", "Error getting poster image: ", e);
            }
        });

    }

}
