package com.example.qreate.organizer.attendeesmenu;

public class OrganizerAttendeeCheckin {
    private String attendeeName;
    private String id;
    private String checkinCount;

    public OrganizerAttendeeCheckin(String attendeeName, String id, Integer checkinCount) {
        this.attendeeName = attendeeName;
        this.id = id;
        this.checkinCount = String.valueOf(checkinCount);
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public String getId() {
        return id;
    }

    public String getCheckinCount() {
        return checkinCount;
    }
}