package com.example.sleepdiary;

public class diaryRecord {

  //Initialise database column names
  public static final String COLUMN_SD = "StartDate";
  public static final String COLUMN_ST = "StartTime";
  public static final String COLUMN_ED = "EndDate";
  public static final String COLUMN_ET = "EndTime";
  public static final String COLUMN_MS = "MsSlept";
  public static final String COLUMN_RT = "Rating";
  public static final String COLUMN_DR = "Dream";

  //Initialise values for record
  public String StartDate;
  public String StartTime;
  public String EndDate;
  public String EndTime;
  public long MsSlept;
  public float Rating;
  public String Dream;

  //SQL instruction to create table with fields
  public static final String CREATE_TABLE = "CREATE TABLE SleepDiaryDB (ID INTEGER PRIMARY KEY AUTOINCREMENT, StartDate TEXT, StartTime TEXT, EndDate TEXT, EndTime TEXT, MsSlept INT, Rating INT, Dream TEXT)";

  //Method for creating class with values
  public diaryRecord(String SD, String ST, String ED, String ET, long MS, float RT, String DR){
    this.StartDate = SD;
    this.StartTime = ST;
    this.EndDate = ED;
    this.EndTime = ET;
    this.MsSlept = MS;
    this.Rating = RT;
    this.Dream = DR;
  }
}