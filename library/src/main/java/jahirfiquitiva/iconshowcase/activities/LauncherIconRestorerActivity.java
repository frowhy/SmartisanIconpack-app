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

package jahirfiquitiva.iconshowcase.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class LauncherIconRestorerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preferences mPrefs = new Preferences(LauncherIconRestorerActivity.this);
        mPrefs.setActivityVisible(true);

        PackageManager p = getPackageManager();

        Class<?> className = null;

        final String packageName = Utils.getAppPackageName(getApplicationContext());
        String componentNameString = packageName + "." + getResources().getString(R.string.main_activity_name);

        try {
            className = Class.forName(componentNameString);
        } catch (ClassNotFoundException e) {
            componentNameString = getResources().getString(R.string.main_activity_fullname);
            try{
                className = Class.forName(componentNameString);
            }catch (ClassNotFoundException ex){
                //Do nothing
            }
        }

        if (className != null) {
            ComponentName componentName = new ComponentName(packageName, componentNameString);

            if (!mPrefs.getLauncherIconShown()) {

                mPrefs.setIconShown(true);

                p.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                String toastContent = getResources().getString(R.string.launcher_icon_restored,
                        getResources().getString(R.string.app_name));
                Toast.makeText(getApplicationContext(), toastContent, Toast.LENGTH_LONG)
                        .show();

            } else {
                String newToastContent = getResources().getString(R.string.launcher_icon_no_restored,
                        getResources().getString(R.string.app_name));
                Toast.makeText(getApplicationContext(),
                        newToastContent, Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            String errorToastContent = getResources().getString(R.string.launcher_icon_restorer_error,
                    getResources().getString(R.string.app_name));
            Toast.makeText(getApplicationContext(),
                    errorToastContent, Toast.LENGTH_LONG)
                    .show();
        }

        mPrefs.setActivityVisible(false);
        finish();

    }

}
