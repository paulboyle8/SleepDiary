package com.example.sleepdiary;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.sql.DriverManager.println;

public class ViewEntries extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    //  private Context context;
    // private List<diaryRecord> entries = new ArrayList<>();
    //int MsSlept = 1;
    boolean avgSet;
    boolean sleptSet;
    GestureDetector detector;

    //TODO intents, start activity with info from table, passing data between activities
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // View v = new RelativeLayout(this);
        setContentView(R.layout.activity_view_entries);
       // final TableLayout tblLayout = findViewById(R.id.tblLayout);
        avgSet = false;
        sleptSet = false;
        detector = new GestureDetector(ViewEntries.this, ViewEntries.this);
        //R.layout.activity_view_entries
        //(new GestureDetectorCompat());
            /*public void onLongPress(MotionEvent event) {
                Log.d("Touch", "onLongPress: " + event.getDownTime());
                AlertDialog.Builder deleteAll = new AlertDialog.Builder(ViewEntries.this);
                final String options[] = {"View", "Edit", "Delete"};
                deleteAll.setTitle("Delete all records?")
                        .setMessage("Are you sure you want to delete all your sleep records?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final DBHelper dbHelper = new DBHelper(ViewEntries.this);
                                final SQLiteDatabase db = dbHelper.getReadableDatabase();
                                db.delete("SleepDiaryDB", "*", null);
                                recreate();
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
        });
        };*/

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
                onBackPressed();
            }
        });


        final DBHelper dbHelper = new DBHelper(this);
        //   entries.addAll(dbHelper.getAllRecords());
        //Cursor data = db.C();


        final TableLayout tblLayout = findViewById(R.id.tblLayout);
        TableRow header = new TableRow(this);
        header.setGravity(Gravity.CENTER);
        header.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        String[] headerText = {"Date", "Time", "Duration", "Rating"};
        for (String title : headerText) {
            TextView lbl = new TextView(this);
            lbl.setText(title);
            lbl.setGravity(Gravity.CENTER);
            lbl.setTextSize(18);
            lbl.setPadding(5, 5, 5, 5);
            header.addView(lbl);
        }
        tblLayout.addView(header);
        //data.moveToFirst();
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        /*new Thread(new Runnable() {
            @Override
            public void run() {*/
                //final SQLiteDatabase db = dbHelper.getReadableDatabase();
                //db.beginTransaction();
                TextView lblAvgMS = findViewById(R.id.lblAvgMS);
                Cursor cAvg = db.rawQuery("SELECT AVG(MsSlept) FROM SleepDiaryDB", null);
                if (cAvg.moveToFirst()) {
                    lblAvgMS.setText("Average sleep time: " + msToUnits.get(cAvg.getInt(0)));
                }
                avgSet = true;
                //db.endTransaction();
                //db.close();
            /*}
        }).start();
       new Thread(new Runnable() {
            @Override
            public void run() {*/
                //final SQLiteDatabase db = dbHelper.getReadableDatabase();
                //db.beginTransaction();
                TextView lblAvgR = findViewById(R.id.lblAvgR);
                Cursor cR = db.rawQuery("SELECT AVG(Rating) FROM SleepDiaryDB", null);
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
                DecimalFormat format = new DecimalFormat("#.##", symbols);
                if (cR.moveToFirst()) {
                    lblAvgR.setText("Average sleep rating: " + format.format(cR.getFloat(0)) + "/5");
                }
                sleptSet = true;
               // db.endTransaction();
                //db.close();

            /*}
        }).start();*/
        //final SQLiteDatabase db = dbHelper.getReadableDatabase();
        //db.beginTransaction();
        try {
            final String selectQuery = "SELECT * FROM SleepDiaryDB";
            final Cursor crsr = db.rawQuery(selectQuery, null);
            if (crsr != null && crsr.moveToFirst()) {
                ;
                // }
                //
                //if (crsr.getCount() > 0){
                try {
                    //if (crsr.moveToFirst()) {
                    do {
                        //int id = crsr.getInt(crsr.getColumnIndex())
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try{
                                /*if (!crsr.moveToNext()){
                                    return;
                                }*/
                                final String StartDate = crsr.getString(crsr.getColumnIndex("StartDate"));
                                final String StartTime = crsr.getString(crsr.getColumnIndex("StartTime"));
                                String EndDate = crsr.getString(crsr.getColumnIndex("EndDate"));
                                String EndTime = crsr.getString(crsr.getColumnIndex("EndTime"));
                                final long MsSlept = crsr.getLong(crsr.getColumnIndex("MsSlept"));
                                final float Rating = crsr.getFloat(crsr.getColumnIndex("Rating"));
                                String Dream = crsr.getString(crsr.getColumnIndex("Rating"));

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                final TableRow row = new TableRow(ViewEntries.this);
                                row.setGravity(Gravity.CENTER);
                                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                row.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //db.delete("SleepDiaryDB","StartDate=? and StartTime=?", new String[]{StartTime,StartTime});
                                        AlertDialog.Builder editEntry = new AlertDialog.Builder(ViewEntries.this);
                                        final String options[] = {"View", "Edit", "Delete"};
                                        editEntry.setTitle("Sleep Diary Record")
                                                .setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        action(options[which], StartDate, StartTime);
                                                    }
                                                });
                                        Dialog choose = editEntry.create();
                                        choose.show();
                                    }
                                });
                                final String[] record = {StartDate, StartTime, msToUnits.get(MsSlept), Float.toString(Rating)};
                                //new Thread(new Runnable() {
                                    //@Override
                                   // public void run() {
                                        for (String text : record) {
                                            TextView lbl = new TextView(ViewEntries.this);
                                            lbl.setText(text);
                                            lbl.setGravity(Gravity.CENTER);
                                            lbl.setTextSize(16);
                                            lbl.setPadding(5, 5, 5, 5);
                                            row.addView(lbl);
                                        }
                                    //}
                                //}).start();
                                tblLayout.addView(row);
//                            } catch (SQLException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }).start();
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
            if (avgSet && sleptSet) {
                db.endTransaction();
                db.close();
            }
        }
    }

    private void action(String option, String SD, String ST){
        //final AlertDialog.Builder editEntry = new AlertDialog.Builder(ViewEntries.this);
        final String[] whereArgs = new String[] {SD, ST};
        switch (option){
            case "View": {Intent intent = new Intent(ViewEntries.this, addEntry.class);
            intent.putExtra("Action", 'V');
            intent.putExtra("whereArgs", whereArgs); //Optional parameters

            final DBHelper dbHelper = new DBHelper(ViewEntries.this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor crsr = db.rawQuery("SELECT StartDate,StartTime,EndDate,EndTime,MsSlept,Rating,Dream FROM SleepDiaryDB WHERE StartDate = ? AND StartTime = ?", whereArgs);
            diaryRecord ViewEntry = null;
            if (crsr.moveToFirst()){
                do {
                    ViewEntry = new diaryRecord(crsr.getString(0),crsr.getString(1),crsr.getString(2),crsr.getString(3),crsr.getLong(4),crsr.getFloat(5),crsr.getString(6));
                } while(crsr.moveToNext());
            }
            crsr.close();
            db.close();
            intent.putExtra("vSD", ViewEntry.StartDate);
            intent.putExtra("vST", ViewEntry.StartTime);
            intent.putExtra("vED", ViewEntry.EndDate);
            intent.putExtra("vET", ViewEntry.EndTime);
            intent.putExtra("vMS", ViewEntry.MsSlept);
            intent.putExtra("vRT", ViewEntry.Rating);
            intent.putExtra("vDR", ViewEntry.Dream);
            startActivity(intent);
            }
            case "Edit": break;
            case "Delete": final AlertDialog.Builder deleteEntry = new AlertDialog.Builder(ViewEntries.this);
                deleteEntry.setTitle("Sleep Diary Record")
                        .setMessage("Are you sure you want to delete this record?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final DBHelper dbHelper = new DBHelper(ViewEntries.this);
                                final SQLiteDatabase db = dbHelper.getReadableDatabase();
                                db.delete("SleepDiaryDB", "StartDate = ? AND StartTime = ?", whereArgs);
                                //db.close();
                                recreate();
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
        }
    }

    protected void  onPause() {
        super.onPause();
        /*final DBHelper dbHelper = new DBHelper(ViewEntries.this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.endTransaction();
        db.close();*/
    }

    protected void onResume() {

        super.onResume();
        //super.onPause();
        /*final DBHelper dbHelper = new DBHelper(ViewEntries.this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();*/
    }

    protected void onStart() {
        super.onStart();
        /*final DBHelper dbHelper = new DBHelper(ViewEntries.this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        //db.beginTransaction();*/
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
        final String options[] = {"View", "Edit", "Delete"};
        deleteAll.setTitle("Delete all records?")
                .setMessage("Are you sure you want to delete all your sleep records?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final DBHelper dbHelper = new DBHelper(ViewEntries.this);
                        final SQLiteDatabase db = dbHelper.getReadableDatabase();
                        db.delete("SleepDiaryDB", "*", null);
                        recreate();
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
    public boolean dispatchTouchEvent(MotionEvent event){
        detector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

   // private void action(String option, String startDate, String startTime) {
   // }