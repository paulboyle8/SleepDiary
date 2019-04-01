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
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class alarmService extends Service {

    Handler timerHandler = new Handler();
    Date bedTime;
    Date wakeTime;
    String bed;
    String wake;
    String bedPrep;
    String sleepTime;
    NotificationManager notificationManager;
    NotificationCompat.Builder buildBedPrep;
    NotificationCompat.Builder buildBedTime;
    NotificationCompat.Builder buildWake;
    String channelID;
    boolean boolReminders;
    boolean remindSent;
    boolean bedSent;
    boolean wakeSent;
    BroadcastReceiver wakeBR;
    BroadcastReceiver bedBR;
    BroadcastReceiver prepBR;

    /*  boolean timerRunning = false;

    Thread timerT = new Thread() {

        @Override
        public void run() {
            while (timerRunning){
                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTime();
                        }
                    });
                    } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
               // timerTick();
            }
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
//        Bundle extras = PlanSleep.getIntent();
//        Intent intent = intent_service.getIntent();
        //startClock();
        initChannel(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        channelID = "SleepDiaryCID";
        CharSequence channelName = "SleepDiary Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);
        notificationManager.createNotificationChannel(notificationChannel);

       /* br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wakeNot();
            }
        };
        registerReceiver(br, new IntentFilter("WakeUp"));
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pAlarm = PendingIntent.getBroadcast(this, 0, new Intent("WakeUp"), 0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try {
            wakeTime = dateFormat.parse(wake);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(wakeTime);
            manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pAlarm);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }

   /* private void startClock(){
        Thread tB=new Thread(){
            @Override
            //runnable = new Runnable(){
            public void run(){
                while (boolReminders){
                    try{
                        Thread.sleep(1000);
                        timerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                compareTime();
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


        };
        tB.start();
    }*/

    private void makeNotifs(){
        buildBedPrep = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.diaryicon)
                .setContentTitle("Get Ready for Bed")
                .setContentText("Go to bed by " + bed + " to get " + sleepTime + "sleep")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);
        buildBedTime = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.diaryicon)
                .setContentTitle("Time for Bed")
                .setContentText("Go to sleep now to get " + sleepTime + "sleep")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);
        Intent openAddEntry = new Intent(this, addEntry.class);
        Intent dismiss = new Intent(this, DismissNotification.class);
        dismiss.putExtra("notID", 3);

        openAddEntry.putExtra("WakeUp", true);
        PendingIntent pOpenAdd = PendingIntent.getActivity(this, 0, openAddEntry, 0);
        PendingIntent pDismiss = PendingIntent.getBroadcast(this, 0, dismiss, 0);

        buildWake = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.diaryicon)
                .setContentTitle("Time to get up!")
                .setContentText("Log your sleep")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{NotificationCompat.DEFAULT_VIBRATE})
                .setAutoCancel(true)
                .setTicker("Notification")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //.setContentIntent(pendingIntent)
                .addAction(R.drawable.diaryicon, "Add diary log", pOpenAdd)
                .addAction(R.drawable.diaryicon, "Dismiss", pDismiss);
    }

    private void initChannel(Context context){
        if (Build.VERSION.SDK_INT < 26){
            return;
        }
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelID = "SleepDiaryCID";
        CharSequence channelName = "SleepDiary Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        /*try{
            unregisterReceiver(br);
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
        }*/

        Intent dismissIntent = new Intent(AlarmClock.ALARM_SEARCH_MODE_ALL);
        dismissIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        dismissIntent.putExtra(AlarmClock.ACTION_DISMISS_ALARM, true);
        dismissIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(dismissIntent);
        //startActivity(dismissIntent);
        boolReminders = intent.getBooleanExtra("boolReminders", false );
        if (!boolReminders){
            if (wakeBR!=null){unregisterReceiver(wakeBR);}
            if (bedBR!=null){unregisterReceiver(bedBR);}
            if (prepBR!=null){unregisterReceiver(prepBR);}
            return super.onStartCommand(intent, flags, startId);
        }
        bed = intent.getStringExtra("bed");
        wake = intent.getStringExtra("wake");
        bedPrep = intent.getStringExtra("bedPrep");
        sleepTime = intent.getStringExtra("sleepTime");
        remindSent = false;
        bedSent = false;
        wakeSent = false;
        //startClock();
        makeNotifs();
        int wakeHour = Integer.parseInt(wake.substring(0,2));
        int wakeMin = Integer.parseInt(wake.substring(3,5));
        int bedHour = Integer.parseInt(bed.substring(0,2));
        int bedMin = Integer.parseInt(bed.substring(3,5));
        int prepHour = Integer.parseInt(bedPrep.substring(0,2));
        int prepMin = Integer.parseInt(bedPrep.substring(3,5));

        wakeBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wakeNot();
                unregisterReceiver(wakeBR);
            }
        };
        registerReceiver(wakeBR, new IntentFilter("WakeUp"));

        bedBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bedNow();
                unregisterReceiver(bedBR);
            }
        };
        registerReceiver(bedBR, new IntentFilter("BedTime"));

        prepBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bedReminder();
                unregisterReceiver(prepBR);
            }
        };
        registerReceiver(prepBR, new IntentFilter("BedPrep"));

        makeNotification("WakeUp", wakeHour, wakeMin, 2);
        makeNotification("BedTime", bedHour, bedMin, 1);
        makeNotification("BedPrep", prepHour, prepMin, 0);

        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, wakeHour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, wakeMin);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //sendBroadcast(alarmIntent);
        startActivity(alarmIntent);
        /*SimpleDateFormat format = new SimpleDateFormat("hh:mm");
        try {
            bedTime = format.parse(bed);
        } catch (ParseException e) {
            bedTime = null;
            e.printStackTrace();
        }
        try {
            wakeTime = format.parse(wake);
        } catch (ParseException e) {
            wakeTime = null;
            e.printStackTrace();
        }
//        addEntry.
        bedReminder();
        wakeAlarm();
        wakeNot();

//        PlanSleep.get
//        Bundle extras = PlanSleep.getIntent();*/
        return super.onStartCommand(intent, flags, startId);
    }

    private void makeNotification(String action, int hour, int min, int mute){
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(action), 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (hour < Calendar.HOUR_OF_DAY || (hour == Calendar.HOUR_OF_DAY && min < Calendar.MINUTE)){
            calendar.add(Calendar.DATE, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        manager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        switch (mute){
            case 0 : break;
            case 1 : {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                assert audioManager != null;
                SharedPreferences sharedVols = getSharedPreferences("volumes", 0);
                SharedPreferences.Editor volEditor = sharedVols.edit();
                volEditor.putInt("Notifs", audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)).apply();
                volEditor.putInt("Music", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)).apply();
                //volEditor.putInt("Ringer", audioManager.getRingerMode()).apply();

                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
            }
            case 2 : {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                assert audioManager != null;
                //audioManager.getRingerMode();
                SharedPreferences sharedVols = getSharedPreferences("volumes", 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, sharedVols.getInt("Notifs",0), 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sharedVols.getInt("Music",0), 0);
                //audioManager.setRingerMode(sharedVols.getInt("Ringer",0));
                break;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void bedReminder(){
        /*NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Low-Priority Notification")
                .setContentText(textContent)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        PlanSleep.*/
        notificationManager.notify(1, buildBedPrep.build());
    }

    public void bedNow(){
        notificationManager.notify(2, buildBedTime.build());
    }

    public void wakeAlarm(){

    }

    public void wakeNot(){
        notificationManager.notify(3, buildWake.build());
    }

    /*private void compareTime(){
        Calendar now = Calendar.getInstance();
        int nowH = now.get(Calendar.HOUR_OF_DAY);
        int nowM = now.get(Calendar.MINUTE);
        String strNow = addEntry.convNum(nowH) + ":" + addEntry.convNum(nowM);

        if (!remindSent && strNow.equals(bedPrep)){
            remindSent = true;
            bedReminder();
        }
        else if (!wakeSent && strNow.equals(wake)){
            wakeSent = true;
            wakeNot();
        }
        else if (!bedSent && strNow.equals(bed)){
            bedSent = true;
            bedNow();
        }
    }*/

    private void cancel(){
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
       // unregisterReceiver(br);
    }

}
