/*
 * Copyright (c) 2016.  Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Big thanks to the project contributors. Check them in the repository.
 *
 */

package jahirfiquitiva.iconshowcase.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.NotificationItem;
import jahirfiquitiva.iconshowcase.utilities.JSONParser;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class NotificationsService extends IntentService {

    private Preferences mPrefs;

    public NotificationsService() {
        super("IconShowcase - Notifs Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = new Preferences(this);
        if (Utils.hasNetwork(getApplicationContext()) && mPrefs.getNotifsEnabled()
                && !mPrefs.getActivityVisible()) {
            new CheckForNotifications().execute();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        onCreate();
    }

    public class CheckForNotifications extends AsyncTask<Void, String, Boolean> {

        public JSONObject mainObject, tag;
        public JSONArray notifs;

        private ArrayList<NotificationItem> notifsList = new ArrayList<>();

        public CheckForNotifications() {
        }

        @Override
        protected void onPreExecute() {
            if (notifsList != null) {
                notifsList.clear();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mainObject = JSONParser.getJSONFromURL(getApplicationContext(),
                        getResources().getString(R.string.notifications_file_url));
                if (mainObject != null) {
                    try {
                        notifs = mainObject.getJSONArray("notifications");
                        for (int i = 0; i < notifs.length(); i++) {
                            tag = notifs.getJSONObject(i);
                            String newWalls = tag.getString("new_walls");
                            if (newWalls != null) {
                                notifsList.add(new NotificationItem(newWalls, 1, 97));
                            }
                            String info = tag.getString("general");
                            if (info != null) {
                                notifsList.add(new NotificationItem(info, 2, 19));
                            }
                        }
                    } catch (JSONException e) {
                        Utils.showLog("Error downloading notifs - JSONException: " + e.getLocalizedMessage());
                    }
                }
            } catch (Exception e) {
                Utils.showLog("Error downloading notifs - Exception: " + e.getLocalizedMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean worked) {
            for (int i = 0; i < notifsList.size(); i++) {
                NotificationItem notif = notifsList.get(i);
                if (notif.getType() == 1) {
                    int number = 0;
                    String notifText = notif.getText();
                    try {
                        number = Integer.valueOf(notifText);
                    } catch (NumberFormatException numEx) {
                        number = 0;
                    }
                    if (number > 0) {
                        pushNotification(notif.getText(), notif.getType(), notif.getID());
                    }
                } else {
                    if (!(notif.getText().equals("null"))) {
                        pushNotification(notif.getText(), notif.getType(), notif.getID());
                    }
                }
            }
        }
    }

    @SuppressWarnings("ResourceAsColor")
    private void pushNotification(String content, int type, int ID) {

        Preferences mPrefs = new Preferences(this);

        String appName = Utils.getStringFromResources(this, R.string.app_name);

        String title = appName, notifContent = null;

        switch (type) {
            case 1:
                title = getResources().getString(R.string.new_walls_notif_title, appName);
                notifContent = getResources().getString(R.string.new_walls_notif_content, content);
                break;
            case 2:
                title = appName + " " + getResources().getString(R.string.news).toLowerCase();
                notifContent = content;
                break;
        }

        // Send Notification
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);
        notifBuilder.setAutoCancel(true);
        notifBuilder.setContentTitle(title);
        if (notifContent != null) {
            notifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notifContent));
            notifBuilder.setContentText(notifContent);
        }
        notifBuilder.setTicker(title);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notifBuilder.setSound(ringtoneUri);

        if (mPrefs.getNotifsVibrationEnabled()) {
            notifBuilder.setVibrate(new long[]{500, 500});
        } else {
            notifBuilder.setVibrate(null);
        }

        int ledColor = ThemeUtils.darkTheme ?
                ContextCompat.getColor(this, R.color.dark_theme_accent) :
                ContextCompat.getColor(this, R.color.light_theme_accent);

        notifBuilder.setColor(ledColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notifBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        Class appLauncherActivity = getLauncherClass(getApplicationContext());

        if (appLauncherActivity != null) {
            Intent appIntent = new Intent(this, appLauncherActivity);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            appIntent.putExtra("notifType", type);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(appLauncherActivity);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(appIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            notifBuilder.setContentIntent(resultPendingIntent);
        }

        notifBuilder.setOngoing(false);

        notifBuilder.setSmallIcon(R.drawable.ic_notifications);

        Notification notif = notifBuilder.build();

        if (mPrefs.getNotifsLedEnabled()) {
            notif.ledARGB = ledColor;
        }

        notifManager.notify(ID, notif);
    }

    public static void clearNotification(Context context, int ID) {
        NotificationManager notifManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancel(ID);
    }

    private Class getLauncherClass(Context context) {
        Class<?> className = null;

        String componentNameString = Utils.getAppPackageName(
                context.getApplicationContext()) + "." + Utils.getStringFromResources(
                context, R.string.main_activity_name);

        try {
            className = Class.forName(componentNameString);
        } catch (ClassNotFoundException e) {
            try {
                componentNameString = Utils.getStringFromResources(context,
                        R.string.main_activity_fullname);
                className = Class.forName(componentNameString);
            } catch (ClassNotFoundException e1) {
                //Do nothing
            }
        }

        return className;
    }

}