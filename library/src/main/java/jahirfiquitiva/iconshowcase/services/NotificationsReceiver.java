/*
 * Copyright (c) 2016. Jahir Fiquitiva. Android Developer. All rights reserved.
 */

package jahirfiquitiva.iconshowcase.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class NotificationsReceiver extends BroadcastReceiver {

    public static void scheduleAlarms(Context context, boolean cancel) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        Preferences mPrefs = new Preferences(context);

        long interval = Utils.getNotifsUpdateIntervalInMillis(mPrefs.getNotifsUpdateInterval());

        if (mPrefs.getNotifsEnabled() && !cancel) {
            /**
             * TODO: Check which option is better for notifs update:
             * Option 1:
             * long elapsedTime = 5000;
             *
             * Option 2:
             * long elapsedTime = SystemClock.elapsedRealtime();
             *
             * Option 3:
             * something else D:
             */
            long elapsedTime = 5000;
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    elapsedTime,
                    interval,
                    pendingIntent);
            Utils.showLog("Scheduling next notification at " + new Date(System.currentTimeMillis() + interval));

        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void scheduleAlarms(Context context) {
        scheduleAlarms(context, false);
    }

    @Override
    public void onReceive(Context context, Intent i) {
        scheduleAlarms(context);
    }

}
