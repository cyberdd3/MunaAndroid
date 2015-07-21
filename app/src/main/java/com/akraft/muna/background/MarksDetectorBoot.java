package com.akraft.muna.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.akraft.muna.Utils;

public class MarksDetectorBoot extends BroadcastReceiver {
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private static final long CHECK_INTERVAL = AlarmManager.INTERVAL_HOUR;

    @Override
    public void onReceive(Context context, Intent intent) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent workIntent = new Intent(context, MarksDetector.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, workIntent, 0);

        if (intent.getAction().equals("com.akraft.muna.action.DISABLE_MARKS_DETECTOR")) {
            alarmManager.cancel(alarmIntent);
        } else if (intent.getAction().equals("com.akraft.muna.action.START_MARKS_DETECTOR")){

            if (Utils.isNetworkConnected(context) && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("nearby_marks_notifications", true)) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + CHECK_INTERVAL, CHECK_INTERVAL, alarmIntent);
            } else if (alarmManager != null) {
                alarmManager.cancel(alarmIntent);
            }
        }
    }

}
