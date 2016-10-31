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

/*
 *
 */

package jahirfiquitiva.iconshowcase.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.LauncherIconRestorerActivity;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class IconRestorerWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.setComponent(new ComponentName(context.getPackageName(),
                        "LauncherIconRestorerActivity.class"));

                RemoteViews views = new RemoteViews(context.getPackageName(),
                        R.layout.icon_restorer_widget);

                views.setOnClickPendingIntent(R.id.appWidget, PendingIntent.getActivity(
                        context, 0, intent, 0));

                appWidgetManager.updateAppWidget(appWidgetId, views);

            } catch (ActivityNotFoundException e) {
                if (context.getResources().getBoolean(R.bool.debugging))
                    Utils.showLog(context, "App not found!");
            }

        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {

            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.icon_restorer_widget);

            Intent restore = new Intent(context, LauncherIconRestorerActivity.class);

            views.setOnClickPendingIntent(R.id.appWidget,
                    PendingIntent.getActivity(context, 0, restore, 0));

            AppWidgetManager
                    .getInstance(context)
                    .updateAppWidget(
                            intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS),
                            views);

        }

    }
}