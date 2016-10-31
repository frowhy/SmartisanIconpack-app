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

package jahirfiquitiva.iconshowcase.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.adapters.FeaturesAdapter;
import jahirfiquitiva.iconshowcase.dialogs.FolderChooserDialog;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.fragments.base.PreferenceFragment;
import jahirfiquitiva.iconshowcase.services.NotificationsReceiver;
import jahirfiquitiva.iconshowcase.utilities.PermissionUtils;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.utilities.color.ColorExtractor;


public class SettingsFragment extends PreferenceFragment implements
        PermissionUtils.OnPermissionResultListener {

    private Preferences mPrefs;
    private PackageManager p;
    private ComponentName componentName;
    private static Preference WSL, data, notifsUpdateInterval;
    private String location, cacheSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = new Preferences(getActivity());

        mPrefs.setSettingsModified(false);

        if (mPrefs.getDownloadsFolder() != null) {
            location = mPrefs.getDownloadsFolder();
        } else {
            location = getString(R.string.walls_save_location,
                    Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        cacheSize = fullCacheDataSize(getActivity().getApplicationContext());

        p = getActivity().getPackageManager();

        addPreferencesFromResource(R.xml.preferences);

        Class<?> className = null;

        String componentNameString = Utils.getAppPackageName(
                getActivity().getApplicationContext()) + "." + Utils.getStringFromResources(
                getActivity(), R.string.main_activity_name);

        try {
            className = Class.forName(componentNameString);
        } catch (ClassNotFoundException e) {
            try {
                componentNameString = Utils.getStringFromResources(getActivity(),
                        R.string.main_activity_fullname);
                className = Class.forName(componentNameString);
            } catch (ClassNotFoundException e1) {
                //Do nothing
            }
        }

        final PreferenceScreen preferences = (PreferenceScreen) findPreference("preferences");
        final PreferenceCategory launcherIcon = (PreferenceCategory) findPreference("launcherIconPreference");

        setupDevOptions(preferences, getActivity());

        PreferenceCategory notifs = (PreferenceCategory) findPreference("notifications");
        if (!(getResources().getBoolean(R.bool.enable_notifications_service))) {
            preferences.removePreference(notifs);
        }

        PreferenceCategory uiCategory = (PreferenceCategory) findPreference("uiPreferences");

        WSL = findPreference("wallsSaveLocation");
        WSL.setSummary(getResources().getString(R.string.pref_summary_wsl, location));

        SwitchPreference wallHeaderCheck = (SwitchPreference) findPreference("wallHeader");

        if (getResources().getBoolean(R.bool.enable_user_wallpaper_in_toolbar)) {
            wallHeaderCheck.setChecked(mPrefs.getWallpaperAsToolbarHeaderEnabled());
            wallHeaderCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setWallpaperAsToolbarHeaderEnabled(newValue.toString().equals("true"));
                    ((ShowcaseActivity) getActivity()).setupToolbarHeader(
                            getActivity(),
                            ((ShowcaseActivity) getActivity()).getToolbarHeader());
                    ColorExtractor.setupToolbarIconsAndTextsColors(
                            getActivity(),
                            ((ShowcaseActivity) getActivity()).getAppbar(),
                            ((ShowcaseActivity) getActivity()).getToolbar(),
                            ((ShowcaseActivity) getActivity()).getToolbarHeaderImage());
                    return true;
                }
            });
        } else {
            uiCategory.removePreference(wallHeaderCheck);
        }

        // Set the preference for current selected theme

        Preference themesSetting = findPreference("themes");

        if (getResources().getBoolean(R.bool.allow_user_theme_change)) {
            themesSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((ShowcaseActivity) getActivity()).setSettingsDialog(
                            ISDialogs.showThemeChooserDialog(getActivity()));
                    ((ShowcaseActivity) getActivity()).getSettingsDialog().show();
                    return true;
                }
            });
        } else {
            uiCategory.removePreference(themesSetting);
        }

        // Set the preference for colored nav bar on Lollipop
        final SwitchPreference coloredNavBar = (SwitchPreference) findPreference("coloredNavBar");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            coloredNavBar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setSettingsModified(true);
                    if (newValue.toString().equals("true")) {
                        ThemeUtils.changeNavBar(getActivity(), ThemeUtils.NAV_BAR_DEFAULT);
                    } else {
                        ThemeUtils.changeNavBar(getActivity(), ThemeUtils.NAV_BAR_BLACK);
                    }
                    return true;
                }
            });
        } else {
            uiCategory.removePreference(coloredNavBar);
        }

        SwitchPreference animations = (SwitchPreference) findPreference("animations");
        animations.setChecked(mPrefs.getAnimationsEnabled());
        animations.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mPrefs.setAnimationsEnabled(newValue.toString().equals("true"));
                return true;
            }
        });

        data = findPreference("clearData");
        data.setSummary(getResources().getString(R.string.pref_summary_cache, cacheSize));
        data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialDialog.SingleButtonCallback positiveCallback = new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        clearApplicationDataAndCache(getActivity());
                        changeValues(getActivity());
                    }
                };
                ISDialogs.showClearCacheDialog(getActivity(), positiveCallback);
                return true;
            }
        });

        findPreference("wallsSaveLocation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!PermissionUtils.canAccessStorage(getContext())) {
                    PermissionUtils.requestStoragePermission(getActivity(), SettingsFragment.this);
                } else {
                    showFolderChooserDialog();
                }
                return true;
            }
        });

        if ((getResources().getBoolean(R.bool.enable_notifications_service))) {
            SwitchPreference enableNotifs = (SwitchPreference) findPreference("enableNotifs");
            enableNotifs.setChecked(mPrefs.getNotifsEnabled());
            enableNotifs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setNotifsEnabled(newValue.toString().equals("true"));
                    NotificationsReceiver.scheduleAlarms(getActivity());
                    return true;
                }
            });

            SwitchPreference enableNotifsLED = (SwitchPreference) findPreference("enableNotifsLED");
            enableNotifsLED.setChecked(mPrefs.getNotifsLedEnabled());
            enableNotifsLED.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setNotifsLedEnabled(newValue.toString().equals("true"));
                    return true;
                }
            });

            SwitchPreference enableNotifsVibration = (SwitchPreference) findPreference("enableNotifsVibration");
            enableNotifsVibration.setChecked(mPrefs.getNotifsVibrationEnabled());
            enableNotifsVibration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setNotifsVibrationEnabled(newValue.toString().equals("true"));
                    return true;
                }
            });

            notifsUpdateInterval = findPreference("notifsUpdateInterval");
            changeNotifsUpdate(getActivity());
            notifsUpdateInterval.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final int currentInterval = mPrefs.getNotifsUpdateInterval();
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.pref_title_notifs_interval)
                            .content(R.string.pref_summary_notifs_interval)
                            .items(R.array.update_intervals)
                            .itemsCallbackSingleChoice(currentInterval - 1,
                                    new MaterialDialog.ListCallbackSingleChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog, View itemView,
                                                                   int which, CharSequence text) {
                                            int newInterval = which + 1;
                                            if (newInterval != currentInterval) {
                                                mPrefs.setNotifsUpdateInterval(newInterval);
                                            }
                                            NotificationsReceiver.scheduleAlarms(getActivity());
                                            changeNotifsUpdate(getActivity());
                                            return true;
                                        }
                                    })
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .show();
                    return true;
                }
            });
        }

        if (getResources().getBoolean(R.bool.allow_user_to_hide_app_icon)) {
            final SwitchPreference hideIcon = (SwitchPreference) findPreference("launcherIcon");
            if (mPrefs.getLauncherIconShown()) {
                hideIcon.setChecked(false);
            }

            final Class<?> finalClassName = className;
            final String finalComponentName = componentNameString;

            hideIcon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (finalClassName != null) {
                        componentName = new ComponentName(
                                Utils.getAppPackageName(getActivity().getApplicationContext()),
                                finalComponentName);
                        if (newValue.toString().equals("true")) {
                            MaterialDialog.SingleButtonCallback positive = new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (mPrefs.getLauncherIconShown()) {
                                        mPrefs.setIconShown(false);
                                        p.setComponentEnabledSetting(componentName,
                                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                                PackageManager.DONT_KILL_APP);
                                    }

                                    hideIcon.setChecked(true);
                                }
                            };

                            MaterialDialog.SingleButtonCallback negative = new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    hideIcon.setChecked(false);
                                }
                            };

                            DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    if (mPrefs.getLauncherIconShown()) {
                                        hideIcon.setChecked(false);
                                    }
                                }
                            };

                            ((ShowcaseActivity) getActivity()).setSettingsDialog(
                                    ISDialogs.showHideIconDialog(getActivity(),
                                            positive, negative, dismissListener));
                        } else {
                            if (!mPrefs.getLauncherIconShown()) {
                                mPrefs.setIconShown(true);
                                p.setComponentEnabledSetting(componentName,
                                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                        PackageManager.DONT_KILL_APP);
                            }
                        }
                        return true;
                    } else {
                        ISDialogs.showHideIconErrorDialog(getActivity());
                        return false;
                    }
                }
            });
        } else {
            preferences.removePreference(launcherIcon);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.collapseToolbar(getActivity());
    }

    private void setupDevOptions(PreferenceScreen mainPrefs, final Context context) {
        if (getResources().getBoolean(R.bool.dev_options)) {

            Preference drawerStyle, moarOptions;
            SwitchPreference miniHeaderPic, drawerHeaderTexts, iconsChangelog, listsCards;

            drawerStyle = (Preference) findPreference("headerStyle");
            moarOptions = (Preference) findPreference("moreOptions");

            miniHeaderPic = (SwitchPreference) findPreference("miniHeaderPic");
            drawerHeaderTexts = (SwitchPreference) findPreference("drawerHeaderTexts");
            iconsChangelog = (SwitchPreference) findPreference("iconsChangelog");
            listsCards = (SwitchPreference) findPreference("listsCards");

            drawerStyle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    final int selectedTheme = mPrefs.getDevDrawerHeaderStyle();

                    new MaterialDialog.Builder(context)
                            .title(R.string.dev_drawer_header_style_title)
                            .items(R.array.drawer_header_styles)
                            .itemsCallbackSingleChoice(selectedTheme, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                    if (selectedTheme != which) {
                                        mPrefs.setDevDrawerHeaderStyle(which);
                                        mPrefs.setSettingsModified(true);
                                        ThemeUtils.restartActivity((Activity) context);
                                    }
                                    return true;
                                }
                            })
                            .show();

                    return true;
                }
            });

            miniHeaderPic.setChecked(mPrefs.getDevMiniDrawerHeaderPicture());
            miniHeaderPic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setDevMiniDrawerHeaderPicture(newValue.toString().equals("true"));
                    mPrefs.setSettingsModified(true);
                    ThemeUtils.restartActivity((Activity) context);
                    return true;
                }
            });

            drawerHeaderTexts.setChecked(mPrefs.getDevDrawerTexts());
            drawerHeaderTexts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setDevDrawerTexts(newValue.toString().equals("true"));
                    mPrefs.setSettingsModified(true);
                    ThemeUtils.restartActivity((Activity) context);
                    return true;
                }
            });

            iconsChangelog.setChecked(mPrefs.getDevIconsChangelogStyle());
            iconsChangelog.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setDevIconsChangelogStyle(newValue.toString().equals("true"));
                    mPrefs.setSettingsModified(true);
                    ThemeUtils.restartActivity((Activity) context);
                    return true;
                }
            });

            listsCards.setChecked(mPrefs.getDevListsCards());
            listsCards.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setDevListsCards(newValue.toString().equals("true"));
                    mPrefs.setSettingsModified(true);
                    ThemeUtils.restartActivity((Activity) context);
                    return true;
                }
            });

            moarOptions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(context)
                            .title(R.string.dev_more_options_title)
                            .adapter(new FeaturesAdapter(context, R.array.dev_extra_features), null)
                            .positiveText(R.string.great)
                            .listSelector(android.R.color.transparent)
                            .show();
                    return true;
                }
            });

        } else {
            mainPrefs.removePreference(findPreference("devPrefs"));
        }
    }

    public void changeValues(Context context) {
        if (mPrefs.getDownloadsFolder() != null) {
            location = mPrefs.getDownloadsFolder();
        } else {
            location = context.getString(R.string.walls_save_location,
                    Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        WSL.setSummary(context.getResources().getString(R.string.pref_summary_wsl, location));
        cacheSize = fullCacheDataSize(context);
        data.setSummary(context.getResources().getString(R.string.pref_summary_cache, cacheSize));
    }

    public static void changeWallsFolderValue(Context context, Preferences mPrefs) {
        String location;
        if (mPrefs.getDownloadsFolder() != null) {
            location = mPrefs.getDownloadsFolder();
        } else {
            location = context.getString(R.string.walls_save_location,
                    Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        WSL.setSummary(context.getResources().getString(R.string.pref_summary_wsl, location));
    }

    private void clearApplicationDataAndCache(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
        clearCache(context);
        mPrefs.setIconShown(true);
        mPrefs.setDownloadsFolder(null);
        mPrefs.setApplyDialogDismissed(false);
        mPrefs.setWallsDialogDismissed(false);
    }

    private static void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            //Do nothing
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        return dir != null && dir.delete();
    }

    private static String fullCacheDataSize(Context context) {
        String finalSize;

        long cache = 0;
        long extCache = 0;
        double finalResult, mbFinalResult;

        File[] fileList = context.getCacheDir().listFiles();
        for (File aFileList : fileList) {
            if (aFileList.isDirectory()) {
                cache += dirSize(aFileList);
            } else {
                cache += aFileList.length();
            }
        }
        try {
            File[] fileExtList = new File[0];
            try {
                fileExtList = context.getExternalCacheDir().listFiles();
            } catch (NullPointerException e) {
                //Do nothing
            }
            if (fileExtList != null) {
                for (File aFileExtList : fileExtList) {
                    if (aFileExtList.isDirectory()) {
                        extCache += dirSize(aFileExtList);
                    } else {
                        extCache += aFileExtList.length();
                    }
                }
            }
        } catch (NullPointerException npe) {
            Log.d("CACHE", Log.getStackTraceString(npe));
        }

        finalResult = (cache + extCache) / 1000;

        if (finalResult > 1001) {
            mbFinalResult = finalResult / 1000;
            finalSize = String.format("%.2f", mbFinalResult) + " MB";
        } else {
            finalSize = String.format("%.2f", finalResult) + " KB";
        }

        return finalSize;
    }

    private static long dirSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += dirSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result;
        }
        return 0;
    }

    private void changeNotifsUpdate(Context context) {

        String num;

        switch (mPrefs.getNotifsUpdateInterval()) {
            case 1:
                num = "1 " + context.getResources().getString(R.string.hours);
                break;
            case 2:
                num = "6 " + context.getResources().getString(R.string.hours);
                break;
            case 3:
                num = "12 " + context.getResources().getString(R.string.hours);
                break;
            case 4:
                num = "1 " + context.getResources().getString(R.string.days);
                break;
            case 5:
                num = "2 " + context.getResources().getString(R.string.days);
                break;
            case 6:
                num = "4 " + context.getResources().getString(R.string.days);
                break;
            case 7:
                num = "7 " + context.getResources().getString(R.string.days);
                break;
            default:
                num = "1 " + context.getResources().getString(R.string.days);
                break;
        }

        String part1 = context.getResources().getString(R.string.pref_summary_notifs_interval);
        String part2 = "\n" + context.getResources().getString(R.string.pref_summary_notifs_interval_more, num.toLowerCase());
        notifsUpdateInterval.setSummary(part1 + part2);
    }

    private void showFolderChooserDialog() {
        new FolderChooserDialog().show((AppCompatActivity) getActivity());
    }

    @Override
    public void onStoragePermissionGranted() {
        //TODO Show Folder Chooser dialog
    }

}