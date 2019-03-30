package com.example.sleepdiary;

import java.sql.Date;
import java.sql.Time;

public class diaryRecord {

    public static final String COLUMN_SD = "StartDate";
    public static final String COLUMN_ST = "StartTime";
    public static final String COLUMN_ED = "EndDate";
    public static final String COLUMN_ET = "EndTime";
    public static final String COLUMN_MS = "MsSlept";
    public static final String COLUMN_RT = "Rating";
    public static final String COLUMN_DR = "Dream";

    public String StartDate;
    public String StartTime;
    public String EndDate;
    public String EndTime;
    public long MsSlept;
    public float Rating;
    public String Dream;

   // public static final String CREATE_TABLE = "CREATE TABLE SleepDiaryDB (ID INTEGER PRIMARY KEY AUTOINCREMENT, StartDate DATE, StartTime TIME, EndDate DATE, EndTime TIME, Rating INTEGER, Dream TEXT)";

    public static final String CREATE_TABLE = "CREATE TABLE SleepDiaryDB (ID INTEGER PRIMARY KEY AUTOINCREMENT, StartDate TEXT, StartTime TEXT, EndDate TEXT, EndTime TEXT, MsSlept INT, Rating INT, Dream TEXT)";


    public diaryRecord(String SD, String ST, String ED, String ET, long MS, float RT, String DR){
        this.StartDate = SD;
        this.StartTime = ST;
        this.EndDate = ED;
        this.EndTime = ET;
        this.MsSlept = MS;
        this.Rating = RT;
        this.Dream = DR;
    }

    public diaryRecord() {
    }
}
