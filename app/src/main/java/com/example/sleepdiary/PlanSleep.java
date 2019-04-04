package com.example.sleepdiary;

import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlanSleep extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    boolean wakeTrue;//Set variable to determine if time being set is start or end time
    TextView txtWake;
    TextView txtBed;
    SeekBar sbH;
    SeekBar sbM;
    Button btnSubmit;
    String bedPrep;
    TextView lblHM;
    Switch swOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_sleep);
        wakeTrue = true; //initialise as wake time being set
        txtWake = findViewById(R.id.txtWake);
        txtBed = findViewById(R.id.txtBed);
        sbH = findViewById(R.id.sbH);
        sbM = findViewById(R.id.sbM);
        btnSubmit = findViewById(R.id.btnSubmit);
        Button btnBack = findViewById(R.id.btnBack);
        lblHM = findViewById(R.id.lblHM);
        swOn = findViewById(R.id.swOn);

        SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0); //Get shared preferences
        //Set widgets to last sleep plan by default, or blank if they are no previous entries
        txtWake.setText(sharedPlan.getString("Wake", ""));
        txtBed.setText(sharedPlan.getString("Bed", ""));
        bedPrep = sharedPlan.getString("bedPrep", "");
        swOn.setChecked(sharedPlan.getBoolean("Reminders", false));
        sbH.setProgress(sharedPlan.getInt("sbHours", 8));
        sbM.setProgress(sharedPlan.getInt("sbMins", 0));

        if (swOn.isChecked()) btnSubmit.setVisibility(View.VISIBLE); //if reminders are on, show submit button
        else btnSubmit.setVisibility(View.GONE); //else, hide submit button

        lblHM.setText(displayHM()); //Call function to display time as hours and mins

        sbH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//If hour seekbar changed
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblHM.setText(displayHM());//Update hours and minutes TextView
                update(); //Update times to correspond with new sleep time
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//If minute seekbar changed

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lblHM.setText(displayHM());//Update TextView
                update();//Update wake and sleep times
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
            public void onClick(View v) { //When wake time TextView pressed
                SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
                //Use substrings to get hour and minute from time
                int hour = Integer.parseInt(sharedPlan.getString("Wake", "07:00").substring(0, 2));
                int min = Integer.parseInt(sharedPlan.getString("Wake", "07:00").substring(3, 5));
                wakeTrue = true; //Wake time is being added
                TimePickerDialog timePickerDialog = new TimePickerDialog(PlanSleep.this, PlanSleep.this, hour, min, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                timePickerDialog.setTitle("Set wake-up time");
                timePickerDialog.show(); //Show time picker dialog
            }
        });
        txtBed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//When bed time TextView pressed
                SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
                int hour = Integer.parseInt(sharedPlan.getString("Bed", "23:00").substring(0, 2));
                int min = Integer.parseInt(sharedPlan.getString("Bed", "23:00").substring(3, 5));
                wakeTrue = false; //Bed time is being added
                TimePickerDialog timePickerDialog = new TimePickerDialog(PlanSleep.this, PlanSleep.this, hour, min, DateFormat.is24HourFormat(getApplicationContext())); //set 24 hour bool
                timePickerDialog.setTitle("Set bed time");
                timePickerDialog.show();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//When submit button pressed
                SharedPreferences sharedTimes = getSharedPreferences("times", 0);
                SharedPreferences.Editor spEditor = sharedTimes.edit();
                //Add times to shared preferences
                spEditor.putString("ST", txtBed.getText().toString()).apply();
                spEditor.putString("ET", txtWake.getText().toString()).apply();
                long msSlept = msToUnits.getMSfromUnits(sbH.getProgress(), (sbM.getProgress()) * 5); //get milliseconds of sleep time
                int sSlept = (int) (msSlept / 1000); //find seconds by milliseconds/1000
                spEditor.putLong("MS", msSlept).apply();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Calendar calendar = Calendar.getInstance(); //get current date
                Date SD = calendar.getTime();
                calendar.add(Calendar.SECOND, sSlept); //get date from current time plus time to sleep
                Date ED = calendar.getTime();
                String strSD = dateFormat.format(SD);
                String strED = dateFormat.format(ED);
                spEditor.putString("SD", strSD).apply();
                spEditor.putString("ED", strED).apply();

                //Add intent extras to open with Alarm Service class to create notification
                Intent intent_service = new Intent(getApplicationContext(), alarmService.class);
                intent_service.putExtra("bed", txtBed.getText()); //Bed time
                intent_service.putExtra("wake", txtWake.getText()); //Wake time
                intent_service.putExtra("bedPrep", bedPrep); //30 mins before bed time
                intent_service.putExtra("sleepTime", lblHM.getText()); //Time to sleep
                intent_service.putExtra("boolReminders", swOn.isChecked()); //Bool for if reminders are turned on
                startService(intent_service);
            }
        });
        swOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//If reminders on/off switch changed
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {//If off
                    Intent intent_service = new Intent(getApplicationContext(), alarmService.class);
                    intent_service.putExtra("boolReminders", isChecked);
                    startService(intent_service);//Tell alarm service that reminders are switched off
                    btnSubmit.setVisibility(View.GONE);//Hide submit button
                } else {//If on
                    btnSubmit.setVisibility(View.VISIBLE);//Show submit button
                }
                SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
                SharedPreferences.Editor planEditor = sharedPlan.edit();
                planEditor.putBoolean("Reminders", isChecked).apply(); //Add current state to shared preferences
            }


        });
        btnBack.setOnClickListener(new View.OnClickListener() { //Go back to previous activity
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final BroadcastReceiver wakeSwitch = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { //When wake up broadcast received
                swOn.setChecked(false); //Turn off reminders
                unregisterReceiver(this); //unregister broadcast receiver
            }
        };
        registerReceiver(wakeSwitch, new IntentFilter("SwitchOff"));
    }

    public String displayHM() {
        String result = "";
        if (sbH.getProgress() > 0) {
            result = sbH.getProgress() + " Hours "; //Display hours in sleep time
        }
        if (sbM.getProgress() > 0) {
            result += (sbM.getProgress()) * 5 + " Mins"; //Add minutes to sleep time
        }
        return result;//Return string of sleep time
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (wakeTrue) { //If wake time being set
            txtWake.setText(addEntry.convNum(hourOfDay) + ":" + addEntry.convNum(minute)); //set text to entered time
            txtBed.setText(subTime(hourOfDay, minute, sbH.getProgress(), sbM.getProgress() * 5)); //set bed time to entered time - time to sleep
        } else {
            txtBed.setText(addEntry.convNum(hourOfDay) + ":" + addEntry.convNum(minute)); //set text to entered time
            txtWake.setText(addTime(hourOfDay, minute, sbH.getProgress(), sbM.getProgress() * 5));//set wake time to entered time + time to sleep
        }
        char[] bed = txtBed.getText().toString().toCharArray();
        int bH = Integer.parseInt(bed[0] + "" + bed[1]);
        int bM = Integer.parseInt(bed[3] + "" + bed[4]);
        bedPrep = subTime(bH, bM, 0, 30);
        updateSharedPreferences();
    }

    public String addTime(int sH, int sM, int eH, int eM) { //add two times together
        int rM = sM + eM; //result minute = start minute + adding minute
        int rH = 0; //initialise hour as 0
        while (rM > 59) { //If minutes are over 1 hour
            rM -= 60; //minus 1 hour
            rH += 1; //add 1 to hours
        }//loop until minutes are under 1 hour
        rH += sH + eH; //add start and add time to result hours
        while (rH > 23) { //while hours are over 1 day
            rH -= 24; //minus 24 hours
            //Hours can be greater than 23 due to adding 24 hour times
            //Subtracting 24 will find the time meant for the next day
        }
        return addEntry.convNum(rH) + ":" + addEntry.convNum(rM); //return new time as string time
    }

    public String subTime(int sH, int sM, int eH, int eM) {
        int rM = sM - eM;
        int rH = 0;
        while (rM < 0) { //if minute less than 0
            rM += 60; //add 60
            rH -= 1; //subtract 1 from hour
        }
        rH += sH - eH;
        while (rH < 0) { //if hours less than 0
            rH += 24; //add 24
        }
        return addEntry.convNum(rH) + ":" + addEntry.convNum(rM); //return new time as string time
    }

    public void update() {
        if (txtBed.getText() == "" && txtWake.getText() == "") { //if fields are empty
            return; //cancel
        } else { //if fields are full
            char[] bed = txtBed.getText().toString().toCharArray(); //get bedtime as character array
            int bH = Integer.parseInt(bed[0] + "" + bed[1]); //get bedtime hour
            int bM = Integer.parseInt(bed[3] + "" + bed[4]); //get bedtime minutes
            txtWake.setText(addTime(bH, bM, sbH.getProgress(), sbM.getProgress() * 5)); //calculate and display wake time using bedtime and sleep time
            updateSharedPreferences();
        }
    }

    private void updateSharedPreferences() { //get values from widgets to update shared preferences
        SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0);
        SharedPreferences.Editor planEditor = sharedPlan.edit();
        planEditor.putString("Wake", txtWake.getText().toString()).apply();
        planEditor.putString("Bed", txtBed.getText().toString()).apply();
        planEditor.putInt("sbHours", sbH.getProgress()).apply();
        planEditor.putInt("sbMins", sbM.getProgress()).apply();
        planEditor.putString("bedPrep", bedPrep).apply();
    }
}
