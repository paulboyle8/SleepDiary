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
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnView = findViewById(R.id.btnView);
        Button btnPlan = findViewById(R.id.btnPlan);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, addEntry.class);  //myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(intent);
            }
        });
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewEntries.class);  //myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(intent);
            }
        });
        btnPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlanSleep.class);  //myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(intent);
            }
        });
    }
}
