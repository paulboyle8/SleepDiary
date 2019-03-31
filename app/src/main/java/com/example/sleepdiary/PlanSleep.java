package com.example.sleepdiary;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlanSleep extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    boolean wakeTrue;
    TextView txtWake;
    TextView txtBed;
    SeekBar sbH;
    SeekBar sbM;
    Button btnSubmit;
    String bedPrep;
    TextView lblHM;
    Switch swOn;
    boolean boolReminders;

    NotificationManager notificationManager;
    NotificationCompat.Builder buildBedPrep;
    NotificationCompat.Builder buildBedTime;
    NotificationCompat.Builder buildWake;
    String channelID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_sleep);
//        SeekBar sbH = findViewById(R.id.sbH);
//        SeekBar sbM = findViewById(R.id.sbM);
//        TextView lblH = findViewById(R.id.lblH);
//        TextView lblM = findViewById(R.id.lblH);
//        TextView txtWake = findViewById(R.id.txtWake);
//        TextView txtBed = findViewById(R.id.txtBed);
        wakeTrue = true;
        txtWake = findViewById(R.id.txtWake);
        txtBed = findViewById(R.id.txtBed);
        sbH = findViewById(R.id.sbH);
        sbM = findViewById(R.id.sbM);
        btnSubmit = findViewById(R.id.btnSubmit);
        Button btnBack = findViewById(R.id.btnBack);
        lblHM = findViewById(R.id.lblHM);
        swOn = findViewById(R.id.swOn);

        SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
        txtWake.setText(sharedPlan.getString("Wake", ""));
        txtBed.setText(sharedPlan.getString("Bed", ""));
        bedPrep = sharedPlan.getString("bedPrep", "");
        swOn.setChecked(sharedPlan.getBoolean("Reminders", false));
        sbH.setProgress(sharedPlan.getInt("sbHours", 8));
        sbM.setProgress(sharedPlan.getInt("sbMins", 0));
        lblHM.setText(displayHM());

        sbH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblHM.setText(displayHM());
                update();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblHM.setText(displayHM());
                update();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        txtWake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wakeTrue = true;
                TimePickerDialog timePickerDialog = new TimePickerDialog(PlanSleep.this, PlanSleep.this, 7, 0, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                timePickerDialog.setTitle("Set wake-up time");
                //return
                timePickerDialog.show();
            }
        });
        txtBed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wakeTrue = false;
                TimePickerDialog timePickerDialog = new TimePickerDialog(PlanSleep.this, PlanSleep.this, 22, 0, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                timePickerDialog.setTitle("Set bed time");
                //return
                timePickerDialog.show();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                channelID = "SleepDiaryCID";
                CharSequence channelName = "SleepDiary Channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);
                notificationManager.createNotificationChannel(notificationChannel);*/
                SharedPreferences sharedTimes = getSharedPreferences("times", 0);
                SharedPreferences.Editor spEditor = sharedTimes.edit();
                spEditor.putString("ST", txtBed.getText().toString()).commit();
                spEditor.putString("ET", txtWake.getText().toString()).commit();
                long msSlept = msToUnits.getMSfromUnits(sbH.getProgress(), (sbM.getProgress())*5);
                int sSlept = (int)(msSlept/1000);
                spEditor.putLong("MS", msSlept).commit();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Calendar calendar = Calendar.getInstance();
                Date SD = calendar.getTime();
                calendar.add(Calendar.SECOND, sSlept);
                Date ED = calendar.getTime();
                String strSD = dateFormat.format(SD);
                String strED = dateFormat.format(ED);
                spEditor.putString("SD", strSD).commit();
                spEditor.putString("ED", strED).commit();

                Intent intent_service = new Intent(getApplicationContext(), alarmService.class);
                intent_service.putExtra("bed", txtBed.getText()); //Optional parameters
                intent_service.putExtra("wake", txtWake.getText()); //Optional parameters
                intent_service.putExtra("bedPrep", bedPrep); //Optional parameters
                intent_service.putExtra("sleepTime", lblHM.getText()); //Optional parameters
                intent_service.putExtra("boolReminders", swOn.isChecked()); //Optional parameters
                startService(intent_service);

            }
        });
        swOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    Intent intent_service = new Intent(getApplicationContext(), alarmService.class);
                    intent_service.putExtra("boolReminders", isChecked);
                    startService(intent_service);
                }
                SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
                SharedPreferences.Editor planEditor = sharedPlan.edit();
                planEditor.putBoolean("Reminders", isChecked).apply();
            }


        });
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        /*buildBedPrep = new NotificationCompat.Builder(this, channelID)
                .setContentTitle("Get Ready for Bed")
                .setContentText("Go to bed by " + txtBed.getText() + " to get " + lblHM.getText() + " sleep")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);*/
    }

    public String displayHM(){
//        SeekBar sbH = findViewById(R.id.sbH);
//        SeekBar sbM = findViewById(R.id.sbM);
        String result = sbH.getProgress() + " Hours ";
        if (sbM.getProgress() > 0){
            result += (sbM.getProgress())*5 + " Mins";
        }
        return result;
    }

    @Override
    public void onTimeSet (TimePicker view,int hourOfDay, int minute){
//        TextView txtWake = findViewById(R.id.txtWake);
//        TextView txtBed = findViewById(R.id.txtBed);
//        SeekBar sbH = findViewById(R.id.sbH);
//        SeekBar sbM = findViewById(R.id.sbM);
        if (wakeTrue) {
            txtWake.setText(addEntry.convNum(hourOfDay) + ":" + addEntry.convNum(minute));
            txtBed.setText(subTime(hourOfDay, minute, sbH.getProgress(), sbM.getProgress()*5));
        } else {
            txtBed.setText(addEntry.convNum(hourOfDay) + ":" + addEntry.convNum(minute));
            txtWake.setText(addTime(hourOfDay, minute, sbH.getProgress(), sbM.getProgress()*5));
        }
        char[] bed = txtBed.getText().toString().toCharArray();
        int bH = Integer.parseInt(bed[0] + "" + bed[1]);
        int bM = Integer.parseInt(bed[3] + "" + bed[4]);
        bedPrep = subTime(bH, bM, 0, 30);
        updateSharedPreferences();
        //txtTime.setText(addEntry.convNum(hourOfDay) + ":" + addEntry.convNum(minute));
       // calcDiff();
    }

    public String addTime (int sH, int sM, int eH, int eM){
        int rM = sM + eM;
        int rH = 0;
        while (rM > 59) {
            rM -= 60;
            rH += 1;
        }
        rH += sH + eH;
        while (rH > 23){
            rH -= 24;
        }
        return addEntry.convNum(rH) + ":" + addEntry.convNum(rM);
    }

    public String subTime (int sH, int sM, int eH, int eM){
        int rM = sM - eM;
        int rH = 0;
        while (rM < 0) {
            rM += 60;
            rH -= 1;
        }
        rH += sH - eH;
        while (rH < 0){
            rH += 24;
        }
        return addEntry.convNum(rH) + ":" + addEntry.convNum(rM);
    }

    public void update(){
        if (txtBed.getText()=="" && txtWake.getText()==""){
            return;
        }
        else {//if (txtBed.getText()!="" && txtWake.getText()==""){
            char[] bed = txtBed.getText().toString().toCharArray();
            int bH = Integer.parseInt(bed[0] + "" + bed[1]);
            int bM = Integer.parseInt(bed[3] + "" + bed[4]);
            txtWake.setText(addTime(bH, bM, sbH.getProgress(), sbM.getProgress()*5));
            updateSharedPreferences();
        }
//        else {
//            char[] bed = txtWake.getText().toString().toCharArray();
//            int wH = Integer.parseInt(bed[0] + "" + bed[1]);
//            int wM = Integer.parseInt(bed[3] + "" + bed[4]);
//            txtBed.setText(subTime(wH, wM, sbH.getProgress(), sbM.getProgress()*5));
//        }

    }

    private void updateSharedPreferences(){
        SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
        SharedPreferences.Editor planEditor = sharedPlan.edit();
        planEditor.putString("Wake", txtWake.getText().toString()).apply();
        planEditor.putString("Bed", txtBed.getText().toString()).apply();
        planEditor.putInt("sbHours", sbH.getProgress()).apply();
        planEditor.putInt("sbMins", sbM.getProgress()).apply();
        planEditor.putString("bedPrep", bedPrep).apply();
    }

    protected void onPause() {
        super.onPause();
        //swOn.removeOnAttachStateChangeListener(CompoundButton.OnCheckedChangeListener());
    }

    public void runInBackground(){

    }
}
