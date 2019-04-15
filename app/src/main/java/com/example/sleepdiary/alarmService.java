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
import android.os.IBinder;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

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
    String channelID;
    boolean boolReminders;
    boolean remindSent;
    boolean bedSent;
    boolean wakeSent;
    BroadcastReceiver wakeBR;
    BroadcastReceiver bedBR;
    BroadcastReceiver prepBR;

    @Override
    public void onCreate() {
        super.onCreate();
        initChannel();//Make notification channel
    }

    private void makeNotifs(){
        buildBedPrep = new NotificationCompat.Builder(this, channelID) //Build notification to prepare for bed
                .setSmallIcon(R.drawable.diaryicon) //Set notification icon
                .setContentTitle("Get Ready for Bed") //Set title
                .setContentText("Go to bed by " + bed + " to get " + sleepTime + " sleep") //Set content
                .setPriority(NotificationCompat.PRIORITY_HIGH) //Set high priority
                .setAutoCancel(false); //Notification will not auto cancel

        buildBedTime = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.diaryicon)
                .setContentTitle("Time for Bed")
                .setContentText("Go to sleep now to get " + sleepTime + " sleep")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);

        Intent openAddEntry = new Intent(this, addEntry.class); //Make intent to start addEntry
        openAddEntry.putExtra("WakeUp", true); //Add WakeUp as true as intent extra
        PendingIntent pOpenAdd = PendingIntent.getActivity(this, 0, openAddEntry, 0); //Make pending intent from intent

        buildWake = new NotificationCompat.Builder(this, channelID)
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
        channelID = "SleepDiaryCID"; //Set notification channel ID
        CharSequence channelName = "SleepDiary Channel"; //Set notification channel name
        int importance = NotificationManager.IMPORTANCE_HIGH; //Set importance as high
        NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);
        notificationManager.createNotificationChannel(notificationChannel);//Create new channel
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
            //Unregister broadcast receivers
            if (wakeBR!=null){unregisterReceiver(wakeBR);}
            if (bedBR!=null){unregisterReceiver(bedBR);}
            if (prepBR!=null){unregisterReceiver(prepBR);}
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

        wakeBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) { //Make broadcast receiver
                wakeNot(); //Make wake up notification
                sendBroadcast(new Intent("SwitchOff")); //Send broadcast to switch off reminders switch on PlanSleep
                unregisterReceiver(wakeBR); //Unregister receiver
            }
        };
        registerReceiver(wakeBR, new IntentFilter("WakeUp")); //Register receiver

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(action), 0); //Make intent to broadcast action
        Calendar calendar = Calendar.getInstance(); //Get calender
        calendar.setTimeInMillis(System.currentTimeMillis()); //Set to current time
        if (hour < calendar.get(Calendar.HOUR_OF_DAY) || (hour == calendar.get(Calendar.HOUR_OF_DAY) && min < calendar.get(Calendar.MINUTE))){
            calendar.add(Calendar.DAY_OF_YEAR, 1);
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

                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                break;
            }
            case 2 : {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                assert audioManager != null;
                SharedPreferences sharedVols = getSharedPreferences("volumes", 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, sharedVols.getInt("Notifs",0), 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sharedVols.getInt("Music",0), 0);
                break;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void bedReminder(){ notificationManager.notify(1, buildBedPrep.build()); }

    public void bedNow(){
        notificationManager.notify(2, buildBedTime.build());
    }

    public void wakeNot(){
        notificationManager.notify(3, buildWake.build());
    }
}
