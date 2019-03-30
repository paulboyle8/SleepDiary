package com.example.sleepdiary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;

public class addEntry extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    boolean startTrue;
    private DBHelper db;
    long msSlept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        TextView lblStart = findViewById(R.id.lblStart);
        lblStart.append(Html.fromHtml("<font color=red>*</font>"));
        TextView lblEnd = findViewById(R.id.lblEnd);
        lblEnd.append(Html.fromHtml("<font color=red>*</font>"));

        db = new DBHelper(this);
        //db = openOrCreateDatabase("SleepDiaryDB", MODE_PRIVATE, null);
        //  db.execSQL("Create table if not exists DiaryTable (StartDate date, StartTime time, EndDate date, StartTime time, Rating int, Dream string);");
        final TextView txtStartTime = findViewById(R.id.txtStartTime);
        txtStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = true;
                Calendar c = Calendar.getInstance();
                c.add(Calendar.HOUR_OF_DAY, -8);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int min = c.get(Calendar.MINUTE);
                String title = "Set start time";
                TimePickerDialog timePickerDialog = new TimePickerDialog(addEntry.this, addEntry.this, hour, min, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                //set title

                timePickerDialog.setTitle(title);
                //return
                timePickerDialog.show();

            }
        });
        final TextView txtEndTime = findViewById(R.id.txtEndTime);
        txtEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = false;
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int min = c.get(Calendar.MINUTE);
                String title = "Set end time";
                TimePickerDialog timePickerDialog = new TimePickerDialog(addEntry.this, addEntry.this, hour, min, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                timePickerDialog.setTitle(title);
                //return
                timePickerDialog.show();
            }
        });
        final TextView txtEndDate = findViewById(R.id.txtEndDate);
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = false;
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);        //set month
                int day = c.get(Calendar.DAY_OF_MONTH); //set day
                String title = "Set end date";
                DatePickerDialog datePickerDialog = new DatePickerDialog(addEntry.this, addEntry.this, year, month, day);
                datePickerDialog.setTitle(title);
                //return
                datePickerDialog.show();
            }
        });
        final TextView txtStartDate = findViewById(R.id.txtStartDate);
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = true;
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);        //set month
                int day = (c.get(Calendar.DAY_OF_MONTH)); //set day
                String title = "Set end date";
                DatePickerDialog datePickerDialog = new DatePickerDialog(addEntry.this, addEntry.this, year, month, day);
                datePickerDialog.setTitle(title);
                //return
                datePickerDialog.show();
            }
        });
        Button btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtEndDate.getText()=="" || txtStartDate.getText()=="" || txtStartTime.getText()=="" || txtEndTime.getText()=="" || txtEndDate.getText()==""){
                    AlertDialog.Builder incomplete = new AlertDialog.Builder(addEntry.this);
                    //final String options[] = {"View", "Edit", "Delete"};
                    incomplete.setTitle("Error")
                            .setMessage("Please complete all mandatory fields before submitting")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            });
                    Dialog incompleteDialog = incomplete.create();
                    incompleteDialog.show();
                }
                else if (db.recordExists(txtStartDate.getText().toString(), txtStartTime.getText().toString(),txtEndDate.getText().toString(),txtEndTime.getText().toString())){
                    AlertDialog.Builder incomplete = new AlertDialog.Builder(addEntry.this);
                    //final String options[] = {"View", "Edit", "Delete"};
                    incomplete.setTitle("Error")
                            .setMessage("An entry already exists with these details. Please delete or edit this entry in the View Entries menu.")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            });
                    Dialog incompleteDialog = incomplete.create();
                    incompleteDialog.show();
                }
                else{
                    InsertRecords();
                }
            }
        });
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("WakeUp")){
            TextView lblSlept = findViewById(R.id.lblSlept);
            SharedPreferences spTimes = getSharedPreferences("times", 0);
            msSlept = spTimes.getLong("MS", 0);
            txtStartTime.setText(spTimes.getString("ST", ""));
            txtEndTime.setText(spTimes.getString("ET", ""));
            txtStartDate.setText(spTimes.getString("SD", ""));
            txtEndDate.setText(spTimes.getString("ED", ""));
            lblSlept.setText(msToUnits.get(msSlept));
        }
    }
        @Override
        public void onTimeSet (TimePicker view,int hourOfDay, int minute){
            TextView txtTime;
            if (startTrue) {
                txtTime = findViewById(R.id.txtStartTime);
            } else {
                txtTime = findViewById(R.id.txtEndTime);
            }
            txtTime.setText(convNum(hourOfDay) + ":" + convNum(minute));
            calcDiff();
        }

        public static String convNum(int num){
            String strNum;
            if (num < 10) {
                strNum = "0" + num;
                return strNum;
            }
            return Integer.toString(num);

        }

        @Override
        public void onDateSet (DatePicker view,int year, int month, int dayOfMonth){
            TextView txtDate;
            if (startTrue) {
                txtDate = findViewById(R.id.txtStartDate);
            } else {
                txtDate = findViewById(R.id.txtEndDate);
            }
            txtDate.setText(convNum(dayOfMonth) + "/" + convNum(month) + "/" + year);
            calcDiff();
        }

        private void addRecord () {

        }

        private void InsertRecords() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            TextView txtStartDate = findViewById(R.id.txtStartDate);
            TextView txtEndDate = findViewById(R.id.txtEndDate);
            TextView txtStartTime = findViewById(R.id.txtStartTime);
            TextView txtEndTime = findViewById(R.id.txtEndTime);
            RatingBar ratingBar = findViewById(R.id.ratingBar);
            TextView txtDream = findViewById(R.id.txtDream);
            String StartDate = txtStartDate.getText().toString();
            String StartTime = txtStartTime.getText().toString();
            String EndDate = txtEndDate.getText().toString();
            String EndTime = txtStartDate.getText().toString();
            Float rating = ratingBar.getRating();
            String dream = txtDream.getText().toString();
          /*  java.util.Date StartDate;  // = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date EndDate;
            java.util.Date StartTime;
            java.util.Date EndTime;
            int rating;
            try {
                StartDate = dateFormat.parse(txtStartDate.getText().toString());
            } catch (ParseException e) {
                StartDate = null;
            }
            try {
                EndDate = dateFormat.parse(txtEndDate.getText().toString());
            } catch (ParseException e) {
                EndDate = null;
            }
            try {
                StartTime = timeFormat.parse(txtStartTime.getText().toString());
            } catch (ParseException e) {
                StartTime = null;
            }
            try {
                EndTime = timeFormat.parse(txtEndTime.getText().toString());
            } catch (ParseException e) {
                EndTime = null;
            }
            Toast toast = Toast.makeText(getApplicationContext(), "Insert into DiaryTable values (" + StartDate.toString() + ", " + StartTime.toString() + ", " + EndDate.toString() + ", " + EndTime.toString() + ", ", Toast.LENGTH_SHORT); toast.show();
            // db.execSQL("Insert into DiaryTable values (" + );
            long id = db.insertRecord(StartDate, StartTime, EndDate, EndTime, ratingBar.getNumStars(), txtDream.getText().toString());*/
            Toast toast = Toast.makeText(getApplicationContext(), txtStartDate.getText().toString() + txtStartTime.getText().toString() + txtEndDate.getText().toString() + txtEndTime.getText().toString() + ratingBar.getRating() + txtDream.getText().toString(), Toast.LENGTH_SHORT); toast.show();//Insert into DiaryTable values (" + StartDate.toString() + ", " + StartTime.toString() + ", " + EndDate.toString() + ", " + EndTime.toString() + ", ", Toast.LENGTH_SHORT); toast.show();
            // db.execSQL("Insert into DiaryTable values (" + );
            long id = db.insertRecord(StartDate, StartTime, EndDate, EndTime, msSlept, rating, dream);
            //Toast toast2 = Toast.makeText(getApplicationContext(), Long.toString(id), Toast.LENGTH_SHORT); toast2.show();//Insert into DiaryTable values (" + StartDate.toString() + ", " + StartTime.toString() + ", " + EndDate.toString() + ", " + EndTime.toString() + ", ", Toast.LENGTH_SHORT); toast.show();
        }

        private void calcDiff(){
            TextView txtST = findViewById(R.id.txtStartTime);
            TextView txtSD = findViewById(R.id.txtStartDate);
            TextView txtET = findViewById(R.id.txtEndTime);
            TextView txtED = findViewById(R.id.txtEndDate);
            TextView lblSlept = findViewById(R.id.lblSlept);
            if(txtED.getText()!="" && txtET.getText()!="" && txtSD.getText()!="" && txtST.getText()!=""){
                String startStr = txtSD.getText().toString() + " " + txtST.getText().toString();
                String endStr = txtED.getText().toString() + " " + txtET.getText().toString();
                java.util.Date startDT, endDT;
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                try {
                    startDT = format.parse(startStr);
                } catch (ParseException e) {
                    startDT = null;
                    e.printStackTrace();
                }
                try {
                    endDT = format.parse(endStr);
                } catch (ParseException e) {
                    endDT = null;
                    e.printStackTrace();
                }
                msSlept = Math.abs(endDT.getTime() - startDT.getTime());
                lblSlept.setText("Sleep time " + msToUnits.get(msSlept));
            }
        }

        public void onPause() {

            super.onPause();
            Button btnMap = findViewById(R.id.btnMap);
            //btnMap.removeOnCl
        }
    }