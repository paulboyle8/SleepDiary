package com.example.sleepdiary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAdd = findViewById(R.id.btnAdd); //Button to add new entry to diary
        btnAdd.setOnClickListener(new View.OnClickListener() { //When clicked
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, addEntry.class);
                startActivity(intent); //Start Add Entry activity
            }
        });
        Button btnView = findViewById(R.id.btnView); //Button to view entries
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewEntries.class);
                startActivity(intent); //When clicked, open View Entries activity
            }
        });
        Button btnPlan = findViewById(R.id.btnPlan); //Button to plan sleep
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlanSleep.class);
                startActivity(intent); //When clicked, open Plan Sleep activity
            }
        });
    }
}
