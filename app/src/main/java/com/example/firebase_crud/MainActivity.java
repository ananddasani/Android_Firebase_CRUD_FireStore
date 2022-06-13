package com.example.firebase_crud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText name, message;
    Button insertButton, showAllMessageButton;

    FirebaseFirestore db;

    //receiving data to be updated
    String recId, recName, recMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //insert the data into the firestore db
        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.editTextName);
        message = findViewById(R.id.editTextMessage);
        insertButton = findViewById(R.id.insertButton);
        showAllMessageButton = findViewById(R.id.showAllButton);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            insertButton.setText("Update");

            recId = bundle.getString("id");
            recName = bundle.getString("name");
            recMessage = bundle.getString("message");

            //set the data
            name.setText(recName);
            message.setText(recMessage);

        } else {
            insertButton.setText("Insert");
        }

        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = name.getText().toString();
                String userMessage = message.getText().toString();

                if (userMessage.equals("")) {
                    message.setError("Can't be empty");
                    return;
                }

                if (userName.equals("")) {
                    name.setError("Can't be empty");
                    return;
                }

                //check if user want to update the data or insert the data
                Bundle bundle1 = getIntent().getExtras();
                if (bundle1 != null) {
                    //user want to update the data
                    String id = recId;
                    updateTheData(id, userName, userMessage);

                } else {
                    //user want to insert the data

                    //create the random id
                    String id = UUID.randomUUID().toString();

                    saveToFireStore(id, userName, userMessage);

                    //clear the fields
                    name.setText("");
                    message.setText("");
                }
            }
        });

        showAllMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Showing all the messages", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ShowMessage.class));
            }
        });
    }

    private void updateTheData(String id, String name, String message) {

        db.collection("Messages").document(id).update("Name", name, "Message", message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Something went wrong ::: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveToFireStore(String id, String userName, String userMessage) {

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("Name", userName);
        data.put("Message", userMessage);

        db.collection("Messages").document(id).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Data Saved to DB", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
