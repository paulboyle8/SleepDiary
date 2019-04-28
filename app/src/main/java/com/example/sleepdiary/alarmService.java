package com.example.sleepdiary;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

public class alarmService extends Service {

    String bed;
    String wake;
    String bedPrep;
    String sleepTime;
    NotificationManager notificationManager;
    NotificationCompat.Builder buildBedPrep;
    NotificationCompat.Builder buildBedTime;
    NotificationCompat.Builder buildWake;
    NotificationCompat.Builder buildLuxPrep;
    NotificationCompat.Builder buildLuxBed;
    boolean boolReminders;
    boolean remindSent;
    boolean bedSent;
    boolean wakeSent;
    BroadcastReceiver wakeBR;
    BroadcastReceiver bedBR;
    BroadcastReceiver prepBR;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;
    float lux;

    @Override
    public void onCreate() {
        super.onCreate();
        initChannel();//Make notification channel
}

    @Override
    public void onDestroy(){ //Destroy service
        super.onDestroy();
        //Unregister broadcast receivers
        if (wakeBR!=null) {
            unregisterReceiver(wakeBR);
        }
        if (bedBR!=null) {
            unregisterReceiver(bedBR);
        }
        if (prepBR!=null) {
            unregisterReceiver(prepBR);
        }
        if (sensorEventListener!=null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
    private void makeNotifs(){
        if (Build.VERSION.SDK_INT < 26){ //If API level outdated
            return; //Don't make notifications
        }//Else
        buildBedPrep = new NotificationCompat.Builder(this, "SDPrep") //Build notification to prepare for bed
                .setSmallIcon(R.drawable.diaryicon) //Set notification icon
                .setContentTitle("Get Ready for Bed") //Set title
                .setContentText("Go to bed by " + bed + " to get " + sleepTime + " sleep") //Set content
                .setPriority(NotificationCompat.PRIORITY_HIGH) //Set high priority
                .setAutoCancel(false); //Notification will not auto cancel

        buildBedTime = new NotificationCompat.Builder(this, "SDBed")
                .setSmallIcon(R.drawable.diaryicon)
                .setContentTitle("Time for Bed")
                .setContentText("Go to sleep now to get " + sleepTime + " sleep")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);

        Intent openAddEntry = new Intent(this, addEntry.class); //Make intent to start addEntry
        openAddEntry.putExtra("WakeUp", true); //Add WakeUp as true as intent extra
        openAddEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Intent not called from activity
        PendingIntent pOpenAdd = PendingIntent.getActivity(this, 0, openAddEntry, 0); //Make pending intent from intent

        buildWake = new NotificationCompat.Builder(this, "SDWake")
                .setSmallIcon(R.drawable.diaryicon)
                .setContentTitle("Time to get up!")
                .setContentText("Log your sleep")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{NotificationCompat.DEFAULT_VIBRATE}) //Notification will make device vibrate
                .setAutoCancel(true) //Notification will auto cancel
                .setTicker("Notification")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.diaryicon, "Add diary log", pOpenAdd); //Add button action that will follow intent
    }

    private void initChannel(){
        if (Build.VERSION.SDK_INT < 26){ //If API level outdated
            return; //Don't make notifications
        }//Else
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); //Open notification manager
        int importance = NotificationManager.IMPORTANCE_HIGH; //Set importance as high
        //Set up notification channels
        NotificationChannel wakeNC = new NotificationChannel("SDWake", "SleepDiaryWake", importance);
        NotificationChannel bedNC = new NotificationChannel("SDBed", "SleepDiaryBed", importance);
        NotificationChannel prepNC = new NotificationChannel("SDPrep", "SleepDiaryPrep", importance);
        NotificationChannel luxPrepNC = new NotificationChannel("SDLuxP", "SleepDiaryPrepLux", importance);
        NotificationChannel luxBedNC = new NotificationChannel("SDLuxB", "SleepDiaryBedLux", importance);
        notificationManager.createNotificationChannel(wakeNC);//Create wake up channel
        notificationManager.createNotificationChannel(bedNC);//Create bed time channel
        notificationManager.createNotificationChannel(prepNC);//Create prepare for bed channel
        notificationManager.createNotificationChannel(luxBedNC);//Create too bright at bedtime channel
        notificationManager.createNotificationChannel(luxPrepNC);//Create too bright before bedtime channel
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Intent dismissIntent = new Intent(AlarmClock.ALARM_SEARCH_MODE_ALL); //Make intent to dismiss existing alarms
        dismissIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        dismissIntent.putExtra(AlarmClock.ACTION_DISMISS_ALARM, true);
        dismissIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(dismissIntent);
        boolReminders = intent.getBooleanExtra("boolReminders", false ); //Get state of reminders
        if (!boolReminders){//If reminders have been switched off
            //Unregister broadcast receivers and sensor listener
            if (wakeBR!=null){unregisterReceiver(wakeBR);}
            if (bedBR!=null){unregisterReceiver(bedBR);}
            if (prepBR!=null){unregisterReceiver(prepBR);}
            if (sensorEventListener!=null) {sensorManager.unregisterListener(sensorEventListener);}
            return super.onStartCommand(intent, flags, startId);
        }
        bed = intent.getStringExtra("bed"); //Get bed time
        wake = intent.getStringExtra("wake"); //Get wake time
        bedPrep = intent.getStringExtra("bedPrep"); //Get 30mins before bed time
        sleepTime = intent.getStringExtra("sleepTime"); //Get planned time of sleep
        remindSent = false; //Reminder has not been sent
        bedSent = false; //Bed notification not sent
        wakeSent = false; //Wake notification not sent
        makeNotifs(); //Call function to make notifications
        int wakeHour = Integer.parseInt(wake.substring(0,2)); //Get wake hour from string using substring
        int wakeMin = Integer.parseInt(wake.substring(3,5)); //Get wake minute
        int bedHour = Integer.parseInt(bed.substring(0,2)); //Get bed hour
        int bedMin = Integer.parseInt(bed.substring(3,5)); //Get bed minute
        int prepHour = Integer.parseInt(bedPrep.substring(0,2)); //Get hour for bedtime reminder
        int prepMin = Integer.parseInt(bedPrep.substring(3,5)); //Get minute for bedtime reminder

        sensorEventListener = new SensorEventListener() { //Start sensor listener
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) //If light sensor detects change
                {
                    lux = event.values[0]; //Update light variable
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { //If sensor accuracy changes
                Log.d("Sensor accuracy", "Light sensor: " + accuracy); //Log new accuracy
            }
        };
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //Set sensor manager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //Set sensor type, i.e. light
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL); //Register sensor listener

        wakeBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { //Make broadcast receiver
                wakeNot(); //Make wake up notification
                SharedPreferences sharedPlan = getSharedPreferences("planSleep", 0); //Open shared preferences
                SharedPreferences.Editor planEditor = sharedPlan.edit(); //Edit shared preferences
                planEditor.putBoolean("Reminders", false).apply(); //Switch off reminders
                sendBroadcast(new Intent("SwitchOff").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); //Send broadcast to switch off reminders switch on PlanSleep
            }
        };
        registerReceiver(wakeBR, new IntentFilter("WakeUp")); //Register receiver

        bedBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bedNow();
            }
        };
        registerReceiver(bedBR, new IntentFilter("BedTime"));

        prepBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bedReminder();
            }
        };
        registerReceiver(prepBR, new IntentFilter("BedPrep"));

        makeNotification("WakeUp", wakeHour, wakeMin, 2); //Make wake notification
        makeNotification("BedTime", bedHour, bedMin, 1); //Make bed notification
        makeNotification("BedPrep", prepHour, prepMin, 0); //Make reminder notification
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM); //Make intent for setting alarm
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, wakeHour); //Add hour
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, wakeMin); //Add minute
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true); //Add to skip showing the clock app
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Start activity in separate task
        startActivity(alarmIntent); //Start intent
        return super.onStartCommand(intent, flags, startId);
    }

    private void makeNotification(String action, int hour, int min, int mute){
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE); //Open alarm manager
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0); //Make intent to broadcast action
        Calendar calendar = Calendar.getInstance(); //Get calender
        calendar.setTimeInMillis(System.currentTimeMillis()); //Set to current time
        if (hour < calendar.get(Calendar.HOUR_OF_DAY) || (hour == calendar.get(Calendar.HOUR_OF_DAY) && min < calendar.get(Calendar.MINUTE))){
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour); //Set calender hour to parameter hour
        calendar.set(Calendar.MINUTE, min); //Set calender minute to parameter minute
        assert manager != null; //Assert alarm manager is not null
        manager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent); //Use alarm manager to create broadcast at specified time
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //mySleepDiary will display notifications if the user's environment is too bright for sleeping
    //Ideal light levels for preparing for sleep (>180 Lux) and going to sleep (>=5 Lux) retrieved from http://sleep.mysplus.com/library/category2/article1.html
    public void bedReminder(){ //Notification for 30 reminder
        if (Build.VERSION.SDK_INT > 25) { //If API level not outdated
            notificationManager.notify(1, buildBedPrep.build()); //Build notification
            if (lux > 180) { //If environment too bright
                buildLuxPrep = new NotificationCompat.Builder(this, "SDLuxP") //Make notification
                        .setSmallIcon(R.drawable.diaryicon) //Set icon
                        .setContentTitle("Your surroundings are too bright!") //Set title
                        .setContentText("Dim them to help your body progress towards sleep.") //Set message
                        .setPriority(NotificationCompat.PRIORITY_HIGH) //Set priority as high
                        .setAutoCancel(false);
                notificationManager.notify(4, buildLuxPrep.build()); //Build notification
            }
        }
    }

    public void bedNow(){
        if (Build.VERSION.SDK_INT > 25){ //If API level not outdated
            notificationManager.notify(2, buildBedTime.build()); //make notification
            if (lux >= 5){ //If environment too bright for sleeping
                buildLuxBed = new NotificationCompat.Builder(this, "SDLuxB")
                        .setSmallIcon(R.drawable.diaryicon)
                        .setContentTitle("Your surroundings are too bright!")
                        .setContentText("Turn off lights to help you sleep.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(false);
                notificationManager.notify(5, buildLuxBed.build());
            }
        }//Else
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE); //Start audio manager
        assert audioManager != null; //Assert audioManager variable is not null
        SharedPreferences sharedVols = getSharedPreferences("volumes", 0); //Get shared preferences for volumes
        SharedPreferences.Editor volEditor = sharedVols.edit(); //Edit shared preferences
        volEditor.putInt("Notifs", audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)).apply(); //Put current notification volume in shared preferences
        volEditor.putInt("Music", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)).apply(); //Put current music volume in shared preferences
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0); //Mute notifications
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0); //Mute music

        if (sensorEventListener!=null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    public void wakeNot(){
        if (Build.VERSION.SDK_INT > 25){ //If API level not outdated
            notificationManager.notify(3, buildWake.build()); //Make notifications
        }//Else
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE); //Get audio manager
        assert audioManager != null;
        SharedPreferences sharedVols = getSharedPreferences("volumes", 0); //Get volumes shared preferences
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, sharedVols.getInt("Notifs",0), 0); //Set notification volume to previous volume from shared preferences
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sharedVols.getInt("Music",0), 0); //Set music volume to previous volume from shared preferences
    }
}