package com.example.demofirebase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private EditText taskInputField;
    private Button addTaskButton, signOutButton;
    private LinearLayout taskContainer;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference tasksCollection;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tasksCollection = db.collection("tasks");

        // Initialize UI elements
        taskInputField = findViewById(R.id.taskInputField);
        addTaskButton = findViewById(R.id.addTaskButton);
        signOutButton = findViewById(R.id.signOutButton);
        taskContainer = findViewById(R.id.taskContainer);

        // Load tasks from Firebase
        loadTasksFromFirebase();

        // Add task button listener
        addTaskButton.setOnClickListener(v -> addTask());

        // Sign out button listener
        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Profile.this, Welcome.class));
            finish();
        });
    }

    // Function to add a task
    private void addTask() {
        String taskDescription = taskInputField.getText().toString().trim();
        if (!TextUtils.isEmpty(taskDescription)) {
            String taskId = tasksCollection.document().getId();
            Map<String, Object> newTask = new HashMap<>();
            newTask.put("id", taskId);
            newTask.put("description", taskDescription);

            tasksCollection.document(taskId).set(newTask)
                    .addOnSuccessListener(aVoid -> {
                        addTaskToLayout(taskId, taskDescription);
                        taskInputField.setText("");
                        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to load tasks from Firebase
    private void loadTasksFromFirebase() {
        tasksCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskContainer.removeAllViews();  // Clear existing views
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String taskId = document.getString("id");
                        String taskDescription = document.getString("description");
                        addTaskToLayout(taskId, taskDescription);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                );
    }

    // Helper function to add a task view to the layout
    private void addTaskToLayout(String taskId, String description) {
        View taskView = getLayoutInflater().inflate(R.layout.item_task, taskContainer, false);
        TextView taskDescriptionView = taskView.findViewById(R.id.taskDescription);
        EditText taskDescriptionEdit = taskView.findViewById(R.id.taskDescriptionEdit);
        Button editButton = taskView.findViewById(R.id.editButton);
        Button updateButton = taskView.findViewById(R.id.updateButton);
        Button deleteButton = taskView.findViewById(R.id.deleteButton);

        taskDescriptionView.setText(description);

        // Edit button listener
        editButton.setOnClickListener(v -> {
            // Switch to edit mode
            taskDescriptionView.setVisibility(View.GONE);
            taskDescriptionEdit.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);

            // Set the current text to the EditText
            taskDescriptionEdit.setText(description);
        });

        // Update button listener
        updateButton.setOnClickListener(v -> {
            String newDescription = taskDescriptionEdit.getText().toString().trim();
            if (!newDescription.isEmpty()) {
                // Update task in Firebase
                Map<String, Object> updatedTask = new HashMap<>();
                updatedTask.put("description", newDescription);

                tasksCollection.document(taskId).update(updatedTask)
                        .addOnSuccessListener(aVoid -> {
                            // Update UI and switch back to view mode
                            taskDescriptionView.setText(newDescription);
                            taskDescriptionView.setVisibility(View.VISIBLE);
                            taskDescriptionEdit.setVisibility(View.GONE);
                            updateButton.setVisibility(View.GONE);
                            editButton.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(this, "Task description cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> deleteTask(taskId, taskView));

        taskContainer.addView(taskView);
    }

    // Function to delete a task
    private void deleteTask(String taskId, View taskView) {
        tasksCollection.document(taskId).delete()
                .addOnSuccessListener(aVoid -> {
                    taskContainer.removeView(taskView);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show()
                );
    }
}
