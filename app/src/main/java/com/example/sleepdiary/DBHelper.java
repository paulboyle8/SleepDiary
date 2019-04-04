package com.example.sleepdiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "SleepDiaryDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(diaryRecord.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertRecord(String SD, String ST, String ED, String ET, long MS, float RT, String DR) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contents = new ContentValues();
        contents.put(diaryRecord.COLUMN_SD, convertUTC(SD,true));
        contents.put(diaryRecord.COLUMN_ST, ST);
        contents.put(diaryRecord.COLUMN_ED, convertUTC(ED,true));
        contents.put(diaryRecord.COLUMN_ET, ET);
        contents.put(diaryRecord.COLUMN_MS, MS);
        contents.put(diaryRecord.COLUMN_RT, RT);
        contents.put(diaryRecord.COLUMN_DR, DR);

        db.insert("SleepDiaryDB", null, contents);
        db.close();
    }

    public List<diaryRecord> getAllRecords() {
        List<diaryRecord> records = new ArrayList<>();
        String query = "SELECT * FROM SleepDiaryDB ORDER BY StartDate DESC, StartTime DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                diaryRecord record = new diaryRecord();
                record.StartDate = cursor.getString(cursor.getColumnIndex("StartDate"));
                record.StartTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                record.EndDate = cursor.getString(cursor.getColumnIndex("EndDate"));
                record.EndTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                record.MsSlept = cursor.getLong(cursor.getColumnIndex("MsSlept"));
                record.Rating = cursor.getFloat(cursor.getColumnIndex("Rating"));
                record.Dream = cursor.getString(cursor.getColumnIndex("Dream"));

                records.add(record);
            }
            while (cursor.moveToNext());
        }

        db.close();
        return records;
    }

    public boolean recordExists(String SD, String ST, String ED, String ET) {
        SD = convertUTC(SD, true);
        ED = convertUTC(ED, true);
        String query = "SELECT * FROM SleepDiaryDB WHERE StartDate = " + SD + " AND StartTime = \'" + ST + "\' OR EndDate = " + ED + " AND EndTime = \'" + ET + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public String convertUTC(String date, Boolean toUTC) {
        java.text.SimpleDateFormat DBdateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.text.SimpleDateFormat UTCdateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
        if (toUTC) {
            try {
                return UTCdateFormat.format(DBdateFormat.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                return DBdateFormat.format(UTCdateFormat.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
