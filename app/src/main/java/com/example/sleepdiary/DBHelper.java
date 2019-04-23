package com.example.sleepdiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;

public class DBHelper extends SQLiteOpenHelper {

    DBHelper(Context context) {
        super(context, "SleepDiaryDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(diaryRecord.CREATE_TABLE); //When created, create new table if it does not already exist
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //Add new record to database
    void insertRecord(String SD, String ST, String ED, String ET, long MS, float RT, String DR) {
        SQLiteDatabase db = this.getWritableDatabase(); //Open database
        ContentValues contents = new ContentValues(); //Add record values to content
        contents.put(diaryRecord.COLUMN_SD, convertUTC(SD,true)); //Start date
        contents.put(diaryRecord.COLUMN_ST, ST); //Start time
        contents.put(diaryRecord.COLUMN_ED, convertUTC(ED,true)); //End date
        contents.put(diaryRecord.COLUMN_ET, ET); //End time
        contents.put(diaryRecord.COLUMN_MS, MS); //Time slept in milliseconds
        contents.put(diaryRecord.COLUMN_RT, RT); //Rating out of 5
        contents.put(diaryRecord.COLUMN_DR, DR); //Dream notes

        db.insert("SleepDiaryDB", null, contents); //Add content to record of database
        db.close(); //Close database
    }

    //Find if record is a duplicate of an existing record
    boolean recordExists(String SD, String ST, String ED, String ET) {
        SD = convertUTC(SD, true); //Convert string UK date format to UTC format, i.e. year-month-day
        ED = convertUTC(ED, true);
        //Check for duplication
        String query = "SELECT * FROM SleepDiaryDB WHERE StartDate = \'" + SD + "\' AND StartTime = \'" + ST + "\' OR EndDate = \'" + ED + "\' AND EndTime = \'" + ET + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) { //If no duplicates found
            cursor.close();
            return false; //Return false
        }
        cursor.close(); //Else, if duplicate found
        return true; //Return true
    }

    String convertUTC(String date, Boolean toUTC) {
        java.text.SimpleDateFormat DBdateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy"); //Common date format for displaying date
        java.text.SimpleDateFormat UTCdateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd"); //UTC format for sorting dates
        if (toUTC) { //If converting date to UTC
            try {
                return UTCdateFormat.format(DBdateFormat.parse(date)); //Parse string to date, then format to UTC date as string
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                return DBdateFormat.format(UTCdateFormat.parse(date)); //Parse string to date, then format to common date as string
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
