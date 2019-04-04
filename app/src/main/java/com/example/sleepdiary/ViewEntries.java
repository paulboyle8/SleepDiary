package com.example.sleepdiary;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewEntries extends AppCompatActivity implements GestureDetector.OnGestureListener {
    GestureDetector detector; //Initialise detector for long press

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entries);
        detector = new GestureDetector(ViewEntries.this, ViewEntries.this); //set up gesture detector

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() { //On back button press, go to previous activity
            @Override
            public void onClick(View v) {
                onStop();
                onBackPressed();
            }
        });
    }

    private void display() {//Display table of records, average sleep time and average sleep rating
        final DBHelper dbHelper = new DBHelper(this);
        final TableLayout tblLayout = findViewById(R.id.tblLayout);
        TableRow header = new TableRow(this);
        //Configure table
        header.setGravity(Gravity.CENTER);
        header.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        String[] headerText = {"Date", "Time", "Duration", "Rating"};
        for (String title : headerText) { //Configure table headers
            TextView lbl = new TextView(this);
            lbl.setText(title);
            lbl.setGravity(Gravity.CENTER);
            lbl.setTextColor(Color.BLACK);
            lbl.setTextSize(24);
            lbl.setPadding(5, 5, 5, 5);
            header.addView(lbl);
        }
        tblLayout.addView(header);//Add table headers
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        TextView lblAvgMS = findViewById(R.id.lblAvgMS);
        Cursor cAvg = db.rawQuery("SELECT AVG(MsSlept) FROM SleepDiaryDB", null); //Get average of sleep time
        if (cAvg.moveToFirst()) {
            lblAvgMS.setText("Average sleep time: " + msToUnits.get(cAvg.getInt(0))); //Display average using MS to units function
        }
        TextView lblAvgR = findViewById(R.id.lblAvgR);
        Cursor cR = db.rawQuery("SELECT AVG(Rating) FROM SleepDiaryDB", null); //Get average of ratings
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        DecimalFormat format = new DecimalFormat("#.##", symbols);
        if (cR.moveToFirst()) {
            lblAvgR.setText("Average sleep rating: " + format.format(cR.getFloat(0)) + "/5"); //Display average rating
        }
        try {
            final String selectQuery = "SELECT StartDate, StartTime, MsSlept, Rating FROM SleepDiaryDB ORDER BY StartDate ASC, StartTime ASC"; //Get fields for table
            final Cursor crsr = db.rawQuery(selectQuery, null); //Call database query
            if (crsr != null && crsr.moveToFirst()) {//For each result record
                try {
                    do {
                        final String StartDate = crsr.getString(crsr.getColumnIndex("StartDate"));
                        final String StartTime = crsr.getString(crsr.getColumnIndex("StartTime"));
                        final long MsSlept = crsr.getLong(crsr.getColumnIndex("MsSlept"));
                        final float Rating = crsr.getFloat(crsr.getColumnIndex("Rating"));
                        final String finalStartDate = dbHelper.convertUTC(StartDate, false); //Convert date to common format as final variable
                        new Thread(new Runnable() { //New concurrent thread
                            @Override
                            public void run() {
                                final TableRow row = new TableRow(ViewEntries.this); //Make and configure table row
                                row.setGravity(Gravity.CENTER);
                                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                row.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) { //When clicked
                                        AlertDialog.Builder editEntry = new AlertDialog.Builder(ViewEntries.this);
                                        final String options[] = {"View", "Edit", "Delete"}; //Display dialog with options
                                        editEntry.setTitle("Sleep Diary Record")
                                                .setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) { //When option clicked
                                                        action(options[which], StartDate, StartTime); //Call action function with chosen option
                                                    }
                                                });
                                        Dialog choose = editEntry.create();
                                        choose.show(); //Show dialog
                                    }
                                });
                                final String[] record = {finalStartDate, StartTime, msToUnits.get(MsSlept), Float.toString(Rating)}; //Make record with current row
                                for (String text : record) {//For each item in record, make and configure TextView
                                    TextView lbl = new TextView(ViewEntries.this);
                                    lbl.setText(text);
                                    lbl.setGravity(Gravity.CENTER);
                                    lbl.setTextSize(18);
                                    lbl.setTextColor(Color.BLACK);
                                    lbl.setPadding(5, 5, 5, 5);
                                    row.addView(lbl);
                                }
                                tblLayout.addView(row); //Add row to table
                            }
                        }).start(); //start thread
                    }
                    while (crsr.moveToNext());
                    db.setTransactionSuccessful();
                } finally {
                    crsr.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void action(String option, String SD, String ST) {//When option pressed on dialog
        final String[] whereArgs = new String[]{SD, ST};//Declare arguments used in SQL queries
        switch (option) {
            case "View": //If user wants to view record
                viewOrEdit('V', whereArgs);
                break;
            case "Edit": //If user wants to edit record
                viewOrEdit('E', whereArgs);
                break;
            case "Delete": //If user wants to delete record
                final AlertDialog.Builder deleteEntry = new AlertDialog.Builder(ViewEntries.this);
                deleteEntry.setTitle("Sleep Diary Record")
                        .setMessage("Are you sure you want to delete this record?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final DBHelper dbHelper = new DBHelper(ViewEntries.this);
                                final SQLiteDatabase db = dbHelper.getReadableDatabase();
                                db.delete("SleepDiaryDB", "StartDate = ? AND StartTime = ?", whereArgs);
                                TableLayout tblLayout = findViewById(R.id.tblLayout);
                                tblLayout.removeAllViews();
                                display();
                                return;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                Dialog choose = deleteEntry.create();
                choose.show();
                break;
        }
    }

    private void viewOrEdit(Character action, String[] whereArgs) {
        Intent intent = new Intent(ViewEntries.this, addEntry.class);
        intent.putExtra("Action", action);
        intent.putExtra("whereArgs", whereArgs);

        final DBHelper dbHelper = new DBHelper(ViewEntries.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor crsr = db.rawQuery("SELECT StartDate,StartTime,EndDate,EndTime,MsSlept,Rating,Dream FROM SleepDiaryDB WHERE StartDate = ? AND StartTime = ?", whereArgs);
        diaryRecord ViewEntry = null;
        if (crsr.moveToFirst()) {
            do {
                ViewEntry = new diaryRecord(crsr.getString(0), crsr.getString(1), crsr.getString(2), crsr.getString(3), crsr.getLong(4), crsr.getFloat(5), crsr.getString(6));
            } while (crsr.moveToNext());
        }
        crsr.close();
        db.close();
        intent.putExtra("vSD", dbHelper.convertUTC(ViewEntry.StartDate, false));
        intent.putExtra("vST", ViewEntry.StartTime);
        intent.putExtra("vED", dbHelper.convertUTC(ViewEntry.EndDate, false));
        intent.putExtra("vET", ViewEntry.EndTime);
        intent.putExtra("vMS", ViewEntry.MsSlept);
        intent.putExtra("vRT", ViewEntry.Rating);
        intent.putExtra("vDR", ViewEntry.Dream);
        startActivity(intent);
        if (action == 'E') {
            onStop();
        }
    }

    protected void onStart() {
        super.onStart();
        TableLayout tblLayout = findViewById(R.id.tblLayout);
        tblLayout.removeAllViews(); //clear table
        display(); //Display table, average sleep time and rating
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("Touch", "onLongPress: " + e.getDownTime());
        AlertDialog.Builder deleteAll = new AlertDialog.Builder(ViewEntries.this);
        deleteAll.setTitle("Delete all records?")
                .setMessage("Are you sure you want to delete all your sleep records?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final DBHelper dbHelper = new DBHelper(ViewEntries.this);
                        final SQLiteDatabase db = dbHelper.getReadableDatabase();
                        db.delete("SleepDiaryDB", null, null);
                        final TableLayout tblLayout = findViewById(R.id.tblLayout);
                        tblLayout.removeAllViews();
                        display();
                        return;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        Dialog deleteDialog = deleteAll.create();
        deleteDialog.show();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}