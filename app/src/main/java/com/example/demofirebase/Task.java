package com.example.demofirebase;

public class Task {
    private String id;  // Firestore document ID
    private String description;

    public Task() { }  // Default constructor required for Firebase

    public Task(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

