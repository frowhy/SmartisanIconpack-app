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

package jahirfiquitiva.iconshowcase.tasks;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.DisplayMetrics;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.fragments.RequestsFragment;
import jahirfiquitiva.iconshowcase.models.AppFilterError;
import jahirfiquitiva.iconshowcase.models.RequestItem;
import jahirfiquitiva.iconshowcase.models.RequestList;
import jahirfiquitiva.iconshowcase.utilities.Utils;


public class LoadAppsToRequest extends AsyncTask<Void, String, ArrayList<RequestItem>> {

    private static PackageManager mPackageManager;
    private static boolean debugging = false;
    private final static ArrayList<String> components = new ArrayList<>();
    private final static ArrayList<RequestItem> appsList = new ArrayList<>();
    private final static ArrayList<AppFilterError> appFilterErrors = new ArrayList<>();
    private final WeakReference<Context> context;
    private final long startTime;
    private static final String TASK = "AppsToRequest";

    @SuppressLint("PrivateResource")
    public LoadAppsToRequest(Context context) {
        startTime = System.currentTimeMillis();
        this.context = new WeakReference<>(context);

        debugging = context.getResources().getBoolean(R.bool.debugging);

        mPackageManager = context.getPackageManager();

        ArrayList<ResolveInfo> rAllActivitiesList =
                (ArrayList<ResolveInfo>) context.getPackageManager().queryIntentActivities(
                        getAllActivitiesIntent(), 0);

        for (ResolveInfo info : rAllActivitiesList) {

            if (info.activityInfo.packageName.equals(context.getApplicationContext().getPackageName())) {
                continue;
            }

            RequestItem appInfo = new RequestItem(
                    info.loadLabel(mPackageManager).toString(),
                    info.activityInfo.packageName,
                    info.activityInfo.name,
                    getAppIcon(info),
                    getNormalAppIcon(info, mPackageManager));

            appsList.add(appInfo);
        }

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected ArrayList<RequestItem> doInBackground(Void... params) {

        appsList.removeAll(createListFromXML(context.get()));

        Collections.sort(appsList, new Comparator<RequestItem>() {
            @Override
            public int compare(RequestItem a, RequestItem b) {
                return a.getAppName().compareToIgnoreCase(b.getAppName());
            }
        });

        return appsList;
    }

    @Override
    protected void onPostExecute(ArrayList<RequestItem> list) {
        RequestList.setRequestList(list);
        RequestsFragment.setupContent(RequestsFragment.layout, context.get());
        if (list != null) {
            long endTime = System.currentTimeMillis();
            Utils.showLog(context.get(),
                    "Apps to Request Task completed in: " +
                            String.valueOf((endTime - startTime) / 1000) + " secs.");
        }
        if (debugging) {
            if (appFilterErrors != null) {
                showAppFilterErrors(appFilterErrors, context.get());
            }
            if (components != null) {
                showDuplicatedComponentsInLog(components, context.get());
            }
        }
    }

    private static ResolveInfo getResolveInfo(String componentString) {
        Intent intent = new Intent();

        // Example format:
        //intent.setComponent(new ComponentName("com.myapp", "com.myapp.launcher.settings"));

        if (componentString != null) {
            String[] split = null;
            try {
                split = componentString.split("/");
            } catch (ArrayIndexOutOfBoundsException e) {
                //Do nothing
            }
            if (split != null) {
                try {
                    components.add(componentString);
                    intent.setComponent(new ComponentName(split[0], split[1]));
                } catch (ArrayIndexOutOfBoundsException e1) {
                    //Do nothing
                }
            }
            return mPackageManager.resolveActivity(intent, 0);
        } else {
            return null;
        }
    }

    private static String gComponentString(XmlPullParser xmlParser, Context context) {

        boolean halfEmptyPack = false, halfEmptyComp = false;

        try {

            final String initialComponent = xmlParser.getAttributeValue(null, "component").split("/")[1];
            final String finalComponent = initialComponent.substring(0, initialComponent.length() - 1);
            final String initialComponentPackage = xmlParser.getAttributeValue(null, "component").split("/")[0];
            final String finalComponentPackage = initialComponentPackage.substring(14, initialComponentPackage.length());

            if (finalComponentPackage.equals("")) {
                halfEmptyPack = true;
            } else if (finalComponent.equals("")) {
                halfEmptyComp = true;
            }

            final String iconName = getIconName(xmlParser);

            String emptyComponent = finalComponentPackage + finalComponent;
            String completeComponent = finalComponentPackage + "/" + finalComponent;

            boolean error = emptyComponent.equals("") || halfEmptyPack || halfEmptyComp;

            if (debugging) {
                appFilterErrors.add(new AppFilterError(
                        emptyComponent.equals(""),
                        halfEmptyPack,
                        halfEmptyComp,
                        iconName,
                        completeComponent,
                        Utils.getIconResId(context, context.getResources(),
                                context.getPackageName(), iconName, TASK)
                ));
            }

            if (error || iconName.equals("")) {
                return null;
            } else {
                return completeComponent;
            }

        } catch (Exception e) {
            //Do nothing
        }

        return null;

    }

    private static String getIconName(XmlPullParser xmlParser) {
        return xmlParser.getAttributeValue(null, "drawable");
    }

    private Intent getAllActivitiesIntent() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return mainIntent;
    }

    @SuppressLint("PrivateResource")
    private ArrayList<RequestItem> createListFromXML(Context context) {

        ArrayList<RequestItem> activitiesToRemove = new ArrayList<>();

        try {

            XmlPullParserFactory xmlFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlParser = xmlFactory.newPullParser();
            InputStream inputStream = context.getResources().openRawResource(R.raw.appfilter);
            xmlParser.setInput(inputStream, null);

            int activity = xmlParser.getEventType();

            while (activity != XmlPullParser.END_DOCUMENT) {

                String name = xmlParser.getName();

                switch (activity) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.END_TAG:

                        if (name.equals("item")) {

                            ResolveInfo info = getResolveInfo(gComponentString(xmlParser, context));

                            if (info != null) {

                                RequestItem appInfo = new RequestItem(
                                        info.loadLabel(mPackageManager).toString(),
                                        info.activityInfo.packageName,
                                        info.activityInfo.name,
                                        getAppIcon(info),
                                        getNormalAppIcon(info, mPackageManager));

                                activitiesToRemove.add(appInfo);
                            }
                        }

                        break;
                }

                activity = xmlParser.next();
            }

        } catch (IOException | XmlPullParserException e) {
            //Do nothing
        }

        return activitiesToRemove;
    }

    private static void showAppFilterErrors(ArrayList<AppFilterError> errors, Context context) {

        Utils.showAppFilterLog(context, "----- START OF APPFILTER DEBUG -----");

        for (AppFilterError error : errors) {
            String iconName = error.getIconName();
            if (iconName.equals("")) {
                Utils.showAppFilterLog(context, "Found empty drawable for component: \'" + error.getCompleteComponent() + "\'");
            } else {
                if (error.hasEmptyComponent()) {
                    Utils.showAppFilterLog(context, "Found empty ComponentInfo for icon: \'" + iconName + "\'");
                } else if (error.hasHalfEmptyPackage()) {
                    Utils.showAppFilterLog(context, "Found empty component package for icon: \'" + iconName + "\'");
                } else if (error.hasHalfEmptyComponent()) {
                    Utils.showAppFilterLog(context, "Found empty component for icon: \'" + iconName + "\'");
                }
                if (error.getIconID() == 0) {
                    Utils.showAppFilterLog(context, "Icon \'" + iconName + "\' is mentioned in appfilter.xml but could not be found in the app resources.");
                }
            }
        }
    }

    private static void showDuplicatedComponentsInLog(ArrayList<String> components,
                                                      Context context) {

        String[] componentsArray = new String[components.size()];
        componentsArray = components.toArray(componentsArray);

        SimpleArrayMap<String, Integer> occurrences = new SimpleArrayMap<>();

        int count = 0;

        for (String word : componentsArray) {
            count = occurrences.get(word) == null ? 0 : occurrences.get(word);
            occurrences.put(word, count + 1);
        }

        for (int i = 0; i < occurrences.size(); i++) {
            String word = occurrences.keyAt(i);
            if (count > 0) {
                Utils.showAppFilterLog(context, "Duplicated component: \'" + word + "\' - " + String.valueOf(count) + " times.");
            }
        }

        Utils.showAppFilterLog(context, "----- END OF APPFILTER DEBUG -----");

    }

    public Drawable getAppDefaultActivityIcon() {
        return getAppIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    @SuppressWarnings("deprecation")
    public Drawable getAppIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            int iconDpi;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                iconDpi = DisplayMetrics.DENSITY_XXXHIGH;
            } else {
                iconDpi = DisplayMetrics.DENSITY_XXHIGH;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d = resources.getDrawableForDensity(iconId, iconDpi, null);
            } else {
                d = resources.getDrawableForDensity(iconId, iconDpi);
            }

        } catch (Resources.NotFoundException e) {
            try {
                d = ContextCompat.getDrawable(context.get(), R.drawable.ic_na_launcher);
            } catch (Resources.NotFoundException e1) {
                d = null;
            }
        }

        return (d != null) ? d : getAppDefaultActivityIcon();
    }

    public Drawable getAppIcon(ResolveInfo info) {
        return getAppIcon(info.activityInfo);
    }

    public Drawable getAppIcon(ActivityInfo info) {
        Resources resources;
        try {
            resources = context.get().getPackageManager().getResourcesForApplication(info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                return getAppIcon(resources, iconId);
            }
        }
        return getAppDefaultActivityIcon();
    }

    private Drawable getNormalAppIcon(ResolveInfo info, PackageManager pm) {
        if (info != null) {
            return info.loadIcon(pm);
        } else {
            return getAppDefaultActivityIcon();
        }
    }

}