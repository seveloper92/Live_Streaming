package com.example.dbsdn.mychat;

public class RoomItem {

    String roomname;
    String photo;
    String summary;

    public RoomItem(String roomname, String photo, String summary) {
        this.roomname = roomname;
        this.photo = photo;
        this.summary = summary;
    }

    public String getName() {
        return roomname;
    }

    public String getPhoto() {
        return photo;
    }

    public String getSummary() {
        return summary;
    }

}
