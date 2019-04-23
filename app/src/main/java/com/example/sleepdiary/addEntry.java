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

import java.text.ParseException;

public class addEntry extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    boolean startTrue; //boolean used for determining if start or end time/date is being entered
    private DBHelper db; //database helper for adding entry to diary database
    long msSlept; //long for time slept in milliseconds
    boolean editExisting; //boolean for if an existing record is being edited rather than a new one being added
    String oST; //string for original start time, used when selecting existing records from database
    String oSD;//string for original start date, also used for database queries

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        editExisting = false; //initialise as entry being created, not edited from existing record
        TextView lblStart = findViewById(R.id.lblStart);
        lblStart.append(Html.fromHtml(getString(R.string.redstar))); //add red asterisk to TextView to show field is mandatory
        TextView lblEnd = findViewById(R.id.lblEnd);
        lblEnd.append(Html.fromHtml(getString(R.string.redstar)));

        db = new DBHelper(this);
        final TextView txtStartTime = findViewById(R.id.txtStartTime); //TextView to enter start time
        txtStartTime.setOnClickListener(new View.OnClickListener() { //When TextView pressed
            @Override
            public void onClick(View v) {
                startTrue = true; //Time for starting sleep is being set
                SharedPreferences spTimes = getSharedPreferences("times", 0); //Open shared preferences
                int hour = Integer.parseInt(spTimes.getString("ST", "23").substring(0, 2)); //Set default time as last time entered
                int min = Integer.parseInt(spTimes.getString("ST", "23:00").substring(3, 5));
                String title = getString(R.string.set_start_time); //Set title for dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(addEntry.this, addEntry.this, hour, min, DateFormat.is24HourFormat(getApplicationContext())); //Initialise time picker
                timePickerDialog.setTitle(title); //Set title
                timePickerDialog.show(); //Display time picker
            }
        });
        final TextView txtEndTime = findViewById(R.id.txtEndTime); //TextView to enter end time
        txtEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = false;
                SharedPreferences spTimes = getSharedPreferences("times", 0);
                int hour = Integer.parseInt(spTimes.getString("ET", "07").substring(0, 2));
                int min = Integer.parseInt(spTimes.getString("ET", "07:00").substring(3, 5));
                String title = getString(R.string.set_end_time);
                TimePickerDialog timePickerDialog = new TimePickerDialog(addEntry.this, addEntry.this, hour, min, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                timePickerDialog.setTitle(title);
                timePickerDialog.show();
            }
        });
        final TextView txtEndDate = findViewById(R.id.txtEndDate); //TextView to enter end date
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = false;
                Calendar c = Calendar.getInstance(); //Get current date
                //Get current year, month and day
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                String title = getString(R.string.set_end_date); //Set title
                DatePickerDialog datePickerDialog = new DatePickerDialog(addEntry.this, addEntry.this, year, month, day); //Initialise date picker with current date as default
                datePickerDialog.setTitle(title);
                datePickerDialog.show();
            }
        });
        final TextView txtStartDate = findViewById(R.id.txtStartDate); //TextView to enter start date
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrue = true;
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1); //When picking start date, set default for start date as previous day
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = (c.get(Calendar.DAY_OF_MONTH));
                String title = "Set end date";
                DatePickerDialog datePickerDialog = new DatePickerDialog(addEntry.this, addEntry.this, year, month, day);
                datePickerDialog.setTitle(title);
                datePickerDialog.show();
            }
        });
        final Button btnSubmit = findViewById(R.id.btnSubmit); //Button to submit diary entry
        btnSubmit.setOnClickListener(new View.OnClickListener() { //When submit button pressed
            @Override
            public void onClick(View v) {
                //if any mandatory fields have not been entered
                if (txtEndDate.getText() == "" || txtStartDate.getText() == "" || txtStartTime.getText() == "" || txtEndTime.getText() == "" || txtEndDate.getText() == "") {
                    AlertDialog.Builder incomplete = new AlertDialog.Builder(addEntry.this); //display error message
                    incomplete.setTitle(R.string.error)
                            .setMessage("Please complete all mandatory fields before submitting")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return; //Button to exit dialog
                                }
                            });
                    Dialog incompleteDialog = incomplete.create();
                    incompleteDialog.show(); //Display
                } else if (!startFirst(txtStartDate.getText().toString(), txtStartTime.getText().toString(), txtEndDate.getText().toString(), txtEndTime.getText().toString())) {
                    AlertDialog.Builder incomplete = new AlertDialog.Builder(addEntry.this); //display error message
                    incomplete.setTitle(R.string.error)
                            .setMessage("Bedtime cannot be before start time")
                            .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return; //Button to exit dialog
                                }
                            });
                    Dialog incompleteDialog = incomplete.create();
                    incompleteDialog.show(); //Display
                } else if (!editExisting) {//If creating new record rather than editing old one
                    //if entry is a duplicate to already existing entry
                    if (db.recordExists(txtStartDate.getText().toString(), txtStartTime.getText().toString(), txtEndDate.getText().toString(), txtEndTime.getText().toString())) {
                        AlertDialog.Builder duplicate = new AlertDialog.Builder(addEntry.this); //display error message
                        duplicate.setTitle("Error")
                                .setMessage("An entry already exists with these details. Please delete or edit this entry in the View Entries menu.")
                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                });
                        Dialog duplicateDialog = duplicate.create();
                        duplicateDialog.show();
                    } else {
                        InsertRecords(); //Else, insert entry into database
                    }
                } else { //If editing existing record
                    final DBHelper dbHelper = new DBHelper(addEntry.this); //Open database
                    final SQLiteDatabase db = dbHelper.getReadableDatabase();
                    final String[] whereArgs = new String[]{dbHelper.convertUTC(oSD, true), oST}; //Arguments for query - start date and time
                    db.delete("SleepDiaryDB", "StartDate = ? AND StartTime = ?", whereArgs); //Delete existing record
                    InsertRecords(); //Enter new record
                }
            }
        });
        final Button btnBack = findViewById(R.id.btnBack); //Button to return to previous activity
        btnBack.setOnClickListener(new View.OnClickListener() { //When back button pressed
            @Override
            public void onClick(View v) {
                onBackPressed(); //Go back to previous activity
            }
        });
        final Button btnHome = findViewById(R.id.btnHome); //Button to return to main activity
        btnHome.setOnClickListener(new View.OnClickListener() { //When home button pressed
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addEntry.this, MainActivity.class);
                startActivity(intent); //Start main activity
            }
        });
        final TextView lblSlept = findViewById(R.id.lblSlept); //Text view to display time slept for
        final RatingBar ratingBar = findViewById(R.id.ratingBar); //Rating bar to enter rating for sleep
        final TextView txtDream = findViewById(R.id.txtDream); //Text view to enter dream
        Bundle extras = getIntent().getExtras(); //Get extras from intent used to open activity
        if (extras != null && extras.getBoolean("WakeUp")) { //if extra 'WakeUp' true, intent has been called from notification
            //Load details from sleep plan
            SharedPreferences spTimes = getSharedPreferences("times", 0); //Open shared preferences
            msSlept = spTimes.getLong("MS", 0); //Get time slept in milliseconds
            txtStartTime.setText(spTimes.getString("ST", "")); //Display start time
            txtEndTime.setText(spTimes.getString("ET", "")); //Display end time
            txtStartDate.setText(spTimes.getString("SD", "")); //Display start date
            txtEndDate.setText(spTimes.getString("ED", "")); //Display end date
            lblSlept.setText("Sleep time: " + msToUnits.get(msSlept)); //Display time slept in hours and minutes
        } else if (extras != null && (extras.getChar("Action") == 'V' || extras.getChar("Action") == 'E')) { //If activity was opened to view/edit record
            //Display record from intent extras
            oSD = extras.getString("vSD");
            txtStartDate.setText(oSD);
            oST = extras.getString("vST");
            txtStartTime.setText(oST);
            txtEndDate.setText(extras.getString("vED"));
            txtEndTime.setText(extras.getString("vET"));
            msSlept = extras.getLong("vMS");
            lblSlept.setText("Sleep time: " + msToUnits.get(msSlept));
            ratingBar.setRating(extras.getFloat("vRT"));
            txtDream.setText(extras.getString("vDR"));
            switch (extras.getChar("Action")) {
                //If record is being viewed
                case 'V': //Hide buttons or instructions to enter information
                    TextView lblRate = findViewById(R.id.lblRate);
                    lblRate.setVisibility(View.GONE);
                    txtDream.setHint("");
                    btnSubmit.setVisibility(View.GONE);
                    //Make TextViews and other widgets non-clickable
                    txtStartDate.setClickable(false);
                    txtStartTime.setClickable(false);
                    txtEndDate.setClickable(false);
                    txtEndTime.setClickable(false);
                    txtDream.setFocusable(false);
                    ratingBar.setIsIndicator(true);
                case 'E':
                    editExisting = true; //If file being edited, set global variable to true
            }

        } else {
            //Show widgets and make them usable
            TextView lblRate = findViewById(R.id.lblRate);
            lblRate.setVisibility(View.VISIBLE);
            txtDream.setHint("Enter any dream notes (optional):");
            btnSubmit.setVisibility(View.VISIBLE);
            txtStartDate.setClickable(true);
            txtStartTime.setClickable(true);
            txtEndDate.setClickable(true);
            txtEndTime.setClickable(true);
            txtDream.setFocusable(true);
            ratingBar.setIsIndicator(false);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) { //If time entered in dialog
        TextView txtTime;
        if (startTrue) { //If start time is being entered
            txtTime = findViewById(R.id.txtStartTime); //Display in start time text view
        } else {
            txtTime = findViewById(R.id.txtEndTime); //Else, display end time in respective text view
        }
        txtTime.setText(convNum(hourOfDay) + ":" + convNum(minute)); //Display time
        calcDiff(); //Call function to calculate difference between start and end time
    }

    public static String convNum(int num) { //Convert number to string that can be displayed as 24 hour time
        String strNum;
        if (num < 10) { //If number is 1 digit, i.e. less than 10
            strNum = "0" + num; //Make string of "0"+number
            return strNum; //return string value
        }
        return Integer.toString(num); //else, return string of number
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        TextView txtDate;
        if (startTrue) {
            txtDate = findViewById(R.id.txtStartDate);
        } else {
            txtDate = findViewById(R.id.txtEndDate);
        }
        txtDate.setText(convNum(dayOfMonth) + "/" + convNum(month + 1) + "/" + year);
        calcDiff();
    }

    private void InsertRecords() {//Insert record into database
        //Get widgets by ID
        TextView txtStartDate = findViewById(R.id.txtStartDate);
        TextView txtEndDate = findViewById(R.id.txtEndDate);
        TextView txtStartTime = findViewById(R.id.txtStartTime);
        TextView txtEndTime = findViewById(R.id.txtEndTime);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView txtDream = findViewById(R.id.txtDream);

        //Get values from widgets
        String StartDate = txtStartDate.getText().toString();
        String StartTime = txtStartTime.getText().toString();
        String EndDate = txtEndDate.getText().toString();
        String EndTime = txtEndTime.getText().toString();
        Float rating = ratingBar.getRating();
        String dream = txtDream.getText().toString();

        db.insertRecord(StartDate, StartTime, EndDate, EndTime, msSlept, rating, dream); //Send fields to DBHelper insert function
        Toast toast = Toast.makeText(getApplicationContext(), "Sleep record entered! ", Toast.LENGTH_SHORT); //Display
        toast.show(); //Display toast
        Intent intent = new Intent(addEntry.this, ViewEntries.class); //Go to View activity to show record in table
        startActivity(intent);
    }

    private void calcDiff() {//Calculate difference between start and end times
        TextView txtST = findViewById(R.id.txtStartTime);
        TextView txtSD = findViewById(R.id.txtStartDate);
        TextView txtET = findViewById(R.id.txtEndTime);
        TextView txtED = findViewById(R.id.txtEndDate);
        TextView lblSlept = findViewById(R.id.lblSlept);
        if (txtED.getText() != "" && txtET.getText() != "" && txtSD.getText() != "" && txtST.getText() != "") {//if all fields are not empty
            java.util.Date startDT, endDT; //Initialise date variables for whole date
            startDT = getMS(txtSD.getText().toString(), txtST.getText().toString()); //Get start datetime
            endDT = getMS(txtED.getText().toString(), txtET.getText().toString()); //Get end datetime
            msSlept = Math.abs(endDT.getTime() - startDT.getTime()); //find milliseconds between two datetimes
            lblSlept.setText("Sleep time: " + msToUnits.get(msSlept)); //write to TextView
        }
    }

    private Boolean startFirst(String SD, String ST, String ED, String ET) {
        java.util.Date startDT, endDT; //Initialise date variables for whole date
        startDT = getMS(SD, ST); //Get start datetime
        endDT = getMS(ED, ET); //Get end datetime
        if (startDT.getTime() < endDT.getTime()) {
            return true; //If start time is before bed time, return true
        }
        return false; //Else, return false
    }

    private java.util.Date getMS(String strDate, String strTime) {//Calculate difference between start and end times
        String strDT = strDate + " " + strTime; //Concatenate  date and time
        java.util.Date DT; //Initialise date variables for whole date
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm"); //Create date format for parsing strings
        try {
            DT = format.parse(strDT); //Parse datetime string to date
        } catch (ParseException e) {
            DT = null;
            e.printStackTrace(); //If failed, record error
        }
        return DT; //Return date time
    }
}