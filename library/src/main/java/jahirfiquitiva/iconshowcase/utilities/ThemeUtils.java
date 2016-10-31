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

package jahirfiquitiva.iconshowcase.utilities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

import jahirfiquitiva.iconshowcase.R;

@SuppressWarnings("ResourceAsColor")
public class ThemeUtils {

    public final static int NAV_BAR_DEFAULT = 0;
    public final static int NAV_BAR_BLACK = 1;

    public final static int LIGHT = 0;
    public final static int DARK = 1;
    public final static int CLEAR = 2;
    public final static int AUTO = 3;

    public static boolean darkTheme;
    public static boolean transparent;
    public static boolean coloredNavBar;

    public static void onActivityCreateSetTheme(Activity activity) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        int mTheme = sp.getInt("theme", (activity.getResources().getInteger(R.integer.default_theme) - 1));
        switch (mTheme) {
            default:
            case LIGHT:
                activity.setTheme(R.style.AppTheme);
                darkTheme = false;
                transparent = false;
                break;
            case DARK:
                activity.setTheme(R.style.AppThemeDark);
                darkTheme = true;
                transparent = false;
                break;
            case CLEAR:
                activity.setTheme(R.style.AppThemeClear);
                darkTheme = true;
                transparent = true;
                break;
            case AUTO:
                Calendar c = Calendar.getInstance();
                transparent = false;
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                if (timeOfDay >= 7 && timeOfDay < 20) {
                    activity.setTheme(R.style.AppTheme);
                    darkTheme = false;
                } else {
                    activity.setTheme(R.style.AppThemeDark);
                    darkTheme = true;
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void onActivityCreateSetNavBar(Activity activity) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        int navBarState = 0;
        int mNavBar = sp.getInt("navBar", navBarState);
        switch (mNavBar) {
            default:
            case NAV_BAR_DEFAULT:
                activity.getWindow().setNavigationBarColor(darkTheme ?
                        ContextCompat.getColor(activity, R.color.dark_theme_primary_dark) :
                        ContextCompat.getColor(activity, R.color.light_theme_primary_dark));
                coloredNavBar = true;
                break;
            case NAV_BAR_BLACK:
                activity.getWindow().setNavigationBarColor(transparent ?
                        ContextCompat.getColor(activity, R.color.home_clear_background) :
                        ContextCompat.getColor(activity, android.R.color.black));
                coloredNavBar = false;
                break;
        }
    }

    public static void changeNavBar(Activity activity, int mNavBar) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        final Editor editor = sp.edit();
        editor.putInt("navBar", mNavBar).apply();
        restartActivity(activity);
    }

    public static void changeToTheme(Activity activity, int mTheme) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        final Editor editor = sp.edit();
        editor.putInt("theme", mTheme).apply();
    }

    public static void restartActivity(Activity activity) {
        activity.recreate();
    }

}