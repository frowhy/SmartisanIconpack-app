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

package jahirfiquitiva.iconshowcase.activities;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.piracychecker.PiracyChecker;
import com.github.javiersantos.piracychecker.enums.InstallerID;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialize.util.UIUtils;

import org.sufficientlysecure.donations.google.util.IabHelper;
import org.sufficientlysecure.donations.google.util.IabResult;
import org.sufficientlysecure.donations.google.util.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import jahirfiquitiva.iconshowcase.BuildConfig;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.adapters.RequestsAdapter;
import jahirfiquitiva.iconshowcase.dialogs.FolderChooserDialog;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.fragments.DonationsFragment;
import jahirfiquitiva.iconshowcase.fragments.RequestsFragment;
import jahirfiquitiva.iconshowcase.fragments.SettingsFragment;
import jahirfiquitiva.iconshowcase.fragments.WallpapersFragment;
import jahirfiquitiva.iconshowcase.models.IconItem;
import jahirfiquitiva.iconshowcase.models.WallpapersList;
import jahirfiquitiva.iconshowcase.services.NotificationsService;
import jahirfiquitiva.iconshowcase.tasks.LoadIconsLists;
import jahirfiquitiva.iconshowcase.tasks.TasksExecutor;
import jahirfiquitiva.iconshowcase.utilities.PermissionUtils;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.utilities.ZooperIconFontsHelper;
import jahirfiquitiva.iconshowcase.utilities.color.ColorExtractor;


public class ShowcaseActivity extends AppCompatActivity implements
        FolderChooserDialog.FolderSelectionCallback, PermissionUtils.OnPermissionResultListener {

    private static boolean WITH_LICENSE_CHECKER = false,
            WITH_INSTALLED_FROM_AMAZON = false,
            WITH_DONATIONS_SECTION = false,
            WITH_ICONS_BASED_CHANGELOG = false,

    //Donations stuff
    DONATIONS_GOOGLE = false,
            DONATIONS_PAYPAL = false,
            DONATIONS_FLATTR = false,
            DONATIONS_BITCOIN = false,

    ENABLE_DEV_OPTIONS = false;
    //SHOW_LOAD_ICONS_DIALOG = true;

    public static boolean WITH_ZOOPER_SECTION = false, DEBUGGING = false,
            SELECT_ALL_APPS = true, ENABLE_USER_WALLPAPER_IN_TOOLBAR = true;

    private static String[] mGoogleCatalog = new String[0],
            GOOGLE_CATALOG_VALUES = new String[0];

    ///test array values
    private static String[] primaryDrawerItems = new String[0], secondaryDrawerItems = new String[0],
            GOOGLE_CATALOG_FREE, GOOGLE_CATALOG_PRO;

    private static String GOOGLE_PUBKEY = "",
            PAYPAL_USER = "",
            PAYPAL_CURRENCY_CODE = "";

    private IabHelper mHelper;

    private static int drawerHeaderStyle = 1, curVersionCode = 0;

    private static final String MARKET_URL = "http://a.app.qq.com/o/simple.jsp?pkgname=";
    private boolean mIsPremium = false, installedFromPlayStore = false;

    private static final String
            adw_action = "org.adw.launcher.icons.ACTION_PICK_ICON",
            turbo_action = "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON",
            nova_action = "com.novalauncher.THEME";

    public static boolean iconsPicker, wallsPicker, SHUFFLE = true;
    private static boolean iconsPickerEnabled = false, wallsEnabled = false, shuffleIcons = true;

    private static String thaAppName, thaHome, thaPreviews, thaApply, thaWalls, thaRequest,
            thaDonate, thaFAQs, thaZooper, thaCredits, thaSettings, thaKustom;

    private static AppCompatActivity context;

    public static long currentItem = -1, iconsPickerIdentifier = 0, applyIdentifier = 0;
    private static long wallsIdentifier = 0, settingsIdentifier = 0, secondaryStart = 0;

    public static int numOfIcons = 4;
    private static int wallpaper = -1;

    private boolean mLastTheme, mLastNavBar;
    private static Preferences mPrefs;

    public MaterialDialog settingsDialog, changelogDialog; //loadIcons,
    public static Toolbar toolbar;
    public static AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    public ImageView icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8;
    public static ImageView toolbarHeader;
    public static Bitmap toolbarHeaderImage;
    public static Drawable wallpaperDrawable;

    public Drawer drawer;

    private static boolean themeMode;
    private String installer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.onActivityCreateSetTheme(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThemeUtils.onActivityCreateSetNavBar(this);
        }

        super.onCreate(savedInstanceState);

        context = this;
        mPrefs = new Preferences(this);

        ENABLE_DEV_OPTIONS = getResources().getBoolean(R.bool.dev_options);

        installer = getIntent().getStringExtra("installer");
        int notifType = getIntent().getIntExtra("launchNotifType", 2);

        curVersionCode = getIntent().getIntExtra("curVersionCode", -1);

        WITH_DONATIONS_SECTION = getIntent().getBooleanExtra("enableDonations", false);
        DONATIONS_GOOGLE = getIntent().getBooleanExtra("enableGoogleDonations", false);
        DONATIONS_PAYPAL = getIntent().getBooleanExtra("enablePayPalDonations", false);
        DONATIONS_FLATTR = getIntent().getBooleanExtra("enableFlattrDonations", false);
        DONATIONS_BITCOIN = getIntent().getBooleanExtra("enableBitcoinDonations", false);

        WITH_LICENSE_CHECKER = getIntent().getBooleanExtra("enableLicenseCheck", false);
        WITH_INSTALLED_FROM_AMAZON = getIntent().getBooleanExtra("enableAmazonInstalls", false);

        GOOGLE_PUBKEY = getIntent().getStringExtra("googlePubKey");

        ENABLE_USER_WALLPAPER_IN_TOOLBAR = getResources().getBoolean(R.bool.enable_user_wallpaper_in_toolbar);

        getAction();

        TasksExecutor.with(context)
                .loadJust(
                        (iconsPicker && iconsPickerEnabled),
                        ((notifType == 1) ||
                                (wallsPicker && mPrefs.areFeaturesEnabled() && wallsEnabled)));

        DEBUGGING = getResources().getBoolean(R.bool.debugging);

        if (ENABLE_DEV_OPTIONS) {
            WITH_ICONS_BASED_CHANGELOG = mPrefs.getDevIconsChangelogStyle();
        } else {
            WITH_ICONS_BASED_CHANGELOG = getResources().getBoolean(R.bool.icons_changelog);
        }

        shuffleIcons = getResources().getBoolean(R.bool.shuffle_toolbar_icons);

        mPrefs.setActivityVisible(true);

        String[] configurePrimaryDrawerItems = getResources().getStringArray(R.array.primary_drawer_items);
        primaryDrawerItems = new String[configurePrimaryDrawerItems.length + 1];
        primaryDrawerItems[0] = "Main";
        System.arraycopy(configurePrimaryDrawerItems, 0, primaryDrawerItems, 1, configurePrimaryDrawerItems.length);

        themeMode = getResources().getBoolean(R.bool.theme_mode);

        if (notifType == 1) {
            NotificationsService.clearNotification(context, 97);
        }

        if (notifType == 2) {
            NotificationsService.clearNotification(context, 19);
        }

        try {
            if (installer.matches("com.google.android.feedback") || installer.matches("com.android.vending")) {
                installedFromPlayStore = true;
            }
        } catch (Exception e) {
            //Do nothing
        }

        runLicenseChecker(GOOGLE_PUBKEY);

        setupDonations();

        if (installedFromPlayStore) {
            // Disable donation methods not allowed by Google
            DONATIONS_PAYPAL = false;
            DONATIONS_FLATTR = false;
            DONATIONS_BITCOIN = false;
        }

        //Initialize SecondaryDrawerItems
        if (WITH_DONATIONS_SECTION) {
            secondaryDrawerItems = new String[]{"Credits", "Settings", "Donations"};
        } else {
            secondaryDrawerItems = new String[]{"Credits", "Settings"};
        }

        if (ENABLE_DEV_OPTIONS) {
            drawerHeaderStyle = mPrefs.getDevDrawerHeaderStyle() + 1;
        } else {
            drawerHeaderStyle = getResources().getInteger(R.integer.nav_drawer_header_style);
        }

        if (drawerHeaderStyle < 1 || drawerHeaderStyle > 3) {
            drawerHeaderStyle = 1;
        }

        if (!themeMode) {
            numOfIcons = context.getResources().getInteger(R.integer.toolbar_icons);
        }

        setContentView(R.layout.showcase_activity);

        icon1 = (ImageView) findViewById(R.id.iconOne);
        icon2 = (ImageView) findViewById(R.id.iconTwo);
        icon3 = (ImageView) findViewById(R.id.iconThree);
        icon4 = (ImageView) findViewById(R.id.iconFour);
        icon5 = (ImageView) findViewById(R.id.iconFive);
        icon6 = (ImageView) findViewById(R.id.iconSix);
        icon7 = (ImageView) findViewById(R.id.iconSeven);
        icon8 = (ImageView) findViewById(R.id.iconEight);

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        toolbarHeader = (ImageView) findViewById(R.id.toolbarHeader);

        setSupportActionBar(toolbar);

        thaAppName = getResources().getString(R.string.app_name);
        thaHome = getResources().getString(R.string.section_home);
        thaPreviews = getResources().getString(R.string.section_icons);
        thaApply = getResources().getString(R.string.section_apply);
        thaWalls = getResources().getString(R.string.section_wallpapers);
        thaRequest = getResources().getString(R.string.section_icon_request);
        thaDonate = getResources().getString(R.string.section_donate);
        thaCredits = getResources().getString(R.string.section_about);
        thaSettings = getResources().getString(R.string.title_settings);
        thaFAQs = getResources().getString(R.string.faqs_section);
        thaZooper = getResources().getString(R.string.zooper_section_title);
        thaKustom = "Kustom";

        collapsingToolbarLayout.setTitle(thaAppName);
        Utils.setupCollapsingToolbarTextColors(context, collapsingToolbarLayout);

        //Setup donations
        if (DONATIONS_GOOGLE) {
            final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {

                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    //TODO test this
                    if (inventory != null) {
                        if (DEBUGGING) Utils.showLog(context, "IAP inventory exists");
                        for (String aGOOGLE_CATALOG_FREE : GOOGLE_CATALOG_FREE) {
                            if (DEBUGGING)
                                Utils.showLog(context, aGOOGLE_CATALOG_FREE + " is " + inventory.hasPurchase(aGOOGLE_CATALOG_FREE));
                            if (inventory.hasPurchase(aGOOGLE_CATALOG_FREE)) { //at least one donation value found, now premium
                                mIsPremium = true;
                            }
                        }
                    }
                    if (isPremium()) {
                        mGoogleCatalog = GOOGLE_CATALOG_PRO;
                    }
                }
            };

            mHelper = new IabHelper(ShowcaseActivity.this, GOOGLE_PUBKEY);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        if (DEBUGGING)
                            Utils.showLog(context, "In-app Billing setup failed: " + result); //TODO move text to string?
                        new MaterialDialog.Builder(ShowcaseActivity.this)
                                .title(R.string.donations_error_title)
                                .content(R.string.donations_error_content)
                                .positiveText(android.R.string.ok)
                                .show();

                    } else {
                        mHelper.queryInventoryAsync(false, mGotInventoryListener);
                    }

                }
            });
        }

        setupDrawer(toolbar, savedInstanceState);

        if (savedInstanceState == null) {
            if (notifType == 1) {
                drawerItemClick(wallsIdentifier);
                drawer.setSelection(wallsIdentifier);
            } else if (iconsPicker && iconsPickerEnabled) {
                drawerItemClick(iconsPickerIdentifier);
                drawer.setSelection(iconsPickerIdentifier);
                /* TODO Double check if this is secure enough to be deleted.
                loadIcons = ISDialogs.showLoadingIconsDialog(context);
                loadIcons.show();
                loadIcons.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                if (!SHOW_LOAD_ICONS_DIALOG) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadIcons.dismiss();
                        }
                    }, 500);
                }
                */
            } else if (wallsPicker && mPrefs.areFeaturesEnabled() && wallsEnabled) {
                drawerItemClick(wallsIdentifier);
                drawer.setSelection(wallsIdentifier);
            } else {
                if (mPrefs.getSettingsModified()) {
                    drawerItemClick(settingsIdentifier);
                    drawer.setSelection(settingsIdentifier);
                } else {
                    currentItem = -1;
                    drawerItemClick(1);
                    drawer.setSelection(1);
                }
            }
        }
    }

    private static String fragment2title(String fragment) {
        switch (fragment) {
            case "Main":
                return thaAppName;
            case "Previews":
                return thaPreviews;
            case "Apply":
                return thaApply;
            case "Wallpapers":
                return thaWalls;
            case "Requests":
                return thaRequest;
            case "Zooper":
                return thaZooper;
            case "Donations":
                return thaDonate;
            case "FAQs":
                return thaFAQs;
            case "Credits":
                return thaCredits;
            case "Settings":
                return thaSettings;
            case "Kustom":
                return thaKustom;
        }
        return ":(";
    }

    public void switchFragment(long itemId, String fragment,
                               AppCompatActivity context) {

        if (currentItem == itemId) {
            // Don't allow re-selection of the currently active item
            return;
        }
        currentItem = itemId;

        if (fragment.equals("Main")) {
            if (!themeMode) {
                icon1.setVisibility(View.INVISIBLE);
                icon2.setVisibility(View.INVISIBLE);
                icon3.setVisibility(View.INVISIBLE);
                icon4.setVisibility(View.INVISIBLE);
            }
        }

        //Fragment Switcher
        if (mPrefs.getAnimationsEnabled()) {
            if (fragment.equals("Donations")) {
                DonationsFragment donationsFragment;
                donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG,
                        DONATIONS_GOOGLE, GOOGLE_PUBKEY, mGoogleCatalog, GOOGLE_CATALOG_VALUES,
                        DONATIONS_PAYPAL, PAYPAL_USER, PAYPAL_CURRENCY_CODE, context.getString(R.string.section_donate),
                        DONATIONS_FLATTR, null, null,
                        DONATIONS_BITCOIN, null);
                context.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.main, donationsFragment, "donationsFragment")
                        .commit();
            } else {
                context.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                        .replace(R.id.main, Fragment.instantiate(context,
                                "jahirfiquitiva.iconshowcase.fragments." + fragment + "Fragment"))
                        .commit();
            }
        } else {
            if (fragment.equals("Donations")) {
                DonationsFragment donationsFragment;
                donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG,
                        DONATIONS_GOOGLE, GOOGLE_PUBKEY, mGoogleCatalog, GOOGLE_CATALOG_VALUES,
                        DONATIONS_PAYPAL, PAYPAL_USER, PAYPAL_CURRENCY_CODE, context.getString(R.string.section_donate),
                        DONATIONS_FLATTR, null, null,
                        DONATIONS_BITCOIN, null);
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, donationsFragment, "donationsFragment")
                        .commit();
            } else {
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, Fragment.instantiate(context,
                                "jahirfiquitiva.iconshowcase.fragments." + fragment + "Fragment"))
                        .commit();
            }
        }

        collapsingToolbarLayout.setTitle(fragment2title(fragment));

        if (drawer != null) {
            drawer.setSelection(itemId);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mLastTheme = ThemeUtils.darkTheme;
        mLastNavBar = ThemeUtils.coloredNavBar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPrefs == null) {
            mPrefs = new Preferences(this);
        }
        if (!iconsPicker && !wallsPicker) {
            setupToolbarHeader(this, toolbarHeader);
        }
        ColorExtractor.setupToolbarIconsAndTextsColors(context, appbar, toolbar, toolbarHeaderImage);
        if (mLastTheme != ThemeUtils.darkTheme
                || mLastNavBar != ThemeUtils.coloredNavBar) {
            ThemeUtils.restartActivity(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (drawer != null)
            outState = drawer.saveInstanceState(outState);
        if (collapsingToolbarLayout != null && collapsingToolbarLayout.getTitle() != null) {
            outState.putString("toolbarTitle", collapsingToolbarLayout.getTitle().toString());
        }
        outState.putInt("currentSection", (int) currentItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (collapsingToolbarLayout != null) {
            Utils.setupCollapsingToolbarTextColors(this, collapsingToolbarLayout);
            collapsingToolbarLayout.setTitle(savedInstanceState.getString("toolbarTitle",
                    thaAppName));
        }
        drawerItemClick(savedInstanceState.getInt("currentSection"));
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (drawer != null && currentItem != 1 && !iconsPicker) {
            drawer.setSelection(1);
        } else if (drawer != null) {
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingsDialog != null) {
            settingsDialog.dismiss();
            settingsDialog = null;
        }
        if (changelogDialog != null) {
            changelogDialog.dismiss();
            changelogDialog = null;
        }
        if (mPrefs == null) {
            mPrefs = new Preferences(this);
        }
        mPrefs.setActivityVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                if (PermissionUtils.permissionReceived() != null)
                    PermissionUtils.permissionReceived().onStoragePermissionGranted();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int i = item.getItemId();
        if (i == R.id.changelog) {
            if (WITH_ICONS_BASED_CHANGELOG) {
                ISDialogs.showIconsChangelogDialog(this);
            } else {
                ISDialogs.showChangelogDialog(this);
            }
        } else if (i == R.id.refresh) {
            WallpapersFragment.refreshWalls(context);
            loadWallsList(this);
        } else if (i == R.id.columns) {
            ISDialogs.showColumnsSelectorDialog(context);
        } else if (i == R.id.select_all) {
            RequestsAdapter requestsAdapter = RequestsFragment.requestsAdapter;
            if (requestsAdapter != null && RequestsFragment.requestsAdapter.appsList.size() > 0) {
                RequestsFragment.requestsAdapter.selectOrDeselectAll(SELECT_ALL_APPS, mPrefs);
                SELECT_ALL_APPS = !SELECT_ALL_APPS;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2) {
            RequestsAdapter adapter = ((RequestsAdapter) RequestsFragment.mRecyclerView.getAdapter());
            if (adapter != null) {
                adapter.deselectAllApps();
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void runLicenseChecker(String licenseKey) {
        mPrefs.setSettingsModified(false);
        if (mPrefs.isFirstRun()) {
            if (WITH_LICENSE_CHECKER) {
                checkLicense(context, mPrefs, licenseKey);
            } else {
                mPrefs.setFeaturesEnabled(true);
                showChangelogDialog();
            }
            mPrefs.setFirstRun(false);
        } else {
            if (WITH_LICENSE_CHECKER) {
                checkLicense(context, mPrefs, licenseKey);
            } else {
                mPrefs.setFeaturesEnabled(true);
                showChangelogDialog();
            }
        }
    }

    private void showChangelogDialog() {

        int prevVersionCode = mPrefs.getVersionCode();

        if ((curVersionCode > prevVersionCode) && (curVersionCode > -1)) {
            mPrefs.setVersionCode(curVersionCode);
            if (WITH_ZOOPER_SECTION) {
                if (!PermissionUtils.canAccessStorage(this)) {
                    PermissionUtils.requestStoragePermission(this, this);
                } else {
                    new ZooperIconFontsHelper(context).check(true);
                }
            }
            if (WITH_ICONS_BASED_CHANGELOG) {
                ISDialogs.showIconsChangelogDialog(this);
            } else {
                ISDialogs.showChangelogDialog(this);
            }
        }

    }

    private void checkLicense(final Context context, final Preferences mPrefs, String licenseKey) {

        PiracyChecker checker = new PiracyChecker(context);

        if ((licenseKey != null) && (!(licenseKey.isEmpty())) && (licenseKey.length() > 25)) {
            checker.enableGooglePlayLicensing(licenseKey);
        }

        checker.enableInstallerId(InstallerID.GOOGLE_PLAY);

        if (WITH_INSTALLED_FROM_AMAZON) {
            checker.enableInstallerId(InstallerID.AMAZON_APP_STORE);
        }

        checker.callback(new PiracyCheckerCallback() {
            @Override
            public void allow() {
                licenseSuccessDialog().show();
            }

            @Override
            public void dontAllow(PiracyCheckerError piracyCheckerError) {
                showNotLicensedDialog((Activity) context, mPrefs, MARKET_URL);
            }
        });

        checker.start();

    }

    private MaterialDialog licenseSuccessDialog() {
        MaterialDialog successDialog = ISDialogs.showLicenseSuccessDialog(context,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mPrefs.setFeaturesEnabled(true);
                        showChangelogDialog();
                    }
                });

        successDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mPrefs.setFeaturesEnabled(true);
                showChangelogDialog();
            }
        });

        successDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mPrefs.setFeaturesEnabled(true);
                showChangelogDialog();
            }
        });

        return successDialog;
    }

    private void showNotLicensedDialog(final Activity act, Preferences mPrefs, final String MARKET_URL) {
        mPrefs.setFeaturesEnabled(false);
        ISDialogs.showLicenseFailDialog(act,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + act.getPackageName()));
                        act.startActivity(browserIntent);
                    }
                }, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        act.finish();
                    }
                }, new MaterialDialog.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        act.finish();
                    }
                }, new MaterialDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        act.finish();
                    }
                });
    }

    private void loadWallsList(Context context) {
        if (mPrefs.getWallsListLoaded()) {
            WallpapersList.clearList();
            mPrefs.setWallsListLoaded(!mPrefs.getWallsListLoaded());
        }
        new WallpapersFragment.DownloadJSON(new WallsListInterface() {
            @Override
            public void checkWallsListCreation(boolean result) {
                mPrefs.setWallsListLoaded(result);
                if (WallpapersFragment.mSwipeRefreshLayout != null) {
                    WallpapersFragment.mSwipeRefreshLayout.setEnabled(false);
                    WallpapersFragment.mSwipeRefreshLayout.setRefreshing(false);
                }
                if (WallpapersFragment.mAdapter != null) {
                    WallpapersFragment.mAdapter.notifyDataSetChanged();
                }
            }
        }, context, WallpapersFragment.noConnection).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onStoragePermissionGranted() {
        new ZooperIconFontsHelper(context).check(true);
    }

    public interface WallsListInterface {

        void checkWallsListCreation(boolean result);
    }

    @SuppressWarnings("ResourceAsColor")
    private void setupDrawer(final Toolbar toolbar, Bundle savedInstanceState) {

        //Initialize PrimaryDrawerItem
        PrimaryDrawerItem home, previews, walls, requests, apply, faqs, zooper, kustom,
                creditsItem, settingsItem, donationsItem;

        secondaryStart = primaryDrawerItems.length + 1; //marks the first identifier value that should be used

        DrawerBuilder drawerBuilder;

        if (themeMode) { //enabled theme mode, long press added
            drawerBuilder = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withFireOnInitialOnClick(true)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            if (drawerItem != null) {
                                drawerItemClick(drawerItem.getIdentifier());
                            }
                            return false;
                        }
                    })
                    .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {

                        @Override
                        public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                            if (drawerItem.getIdentifier() == iconsPickerIdentifier && mIsPremium) {
                                switchFragment(iconsPickerIdentifier, "Requests", context);
                                drawer.closeDrawer();
                            }
                            return false;
                        }
                    });
        } else {
            drawerBuilder = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withFireOnInitialOnClick(true)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            if (drawerItem != null) {
                                drawerItemClick(drawerItem.getIdentifier());
                            }
                            return false;
                        }
                    });
        }

        for (int i = 0; i < primaryDrawerItems.length; i++) {
            switch (primaryDrawerItems[i]) {
                case "Main":
                    home = new PrimaryDrawerItem().withName(thaHome).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_home)).withIconTintingEnabled(true).withIdentifier(i + 1);
                    drawerBuilder.addDrawerItems(home);
                    break;

                case "Previews":
                    iconsPickerEnabled = true;
                    iconsPickerIdentifier = i + 1;
                    previews = new PrimaryDrawerItem().withName(thaPreviews).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_previews)).withIconTintingEnabled(true).withIdentifier(iconsPickerIdentifier);
                    drawerBuilder.addDrawerItems(previews);
                    break;

                case "Wallpapers":
                    wallsEnabled = true;
                    wallsIdentifier = i + 1;
                    walls = new PrimaryDrawerItem().withName(thaWalls).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_wallpapers)).withIconTintingEnabled(true).withIdentifier(wallsIdentifier);
                    drawerBuilder.addDrawerItems(walls);
                    break;

                case "Requests":
                    requests = new PrimaryDrawerItem().withName(thaRequest).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_request)).withIconTintingEnabled(true).withIdentifier(i + 1);
                    drawerBuilder.addDrawerItems(requests);
                    break;

                case "Apply":
                    applyIdentifier = i + 1;
                    apply = new PrimaryDrawerItem().withName(thaApply).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_apply)).withIconTintingEnabled(true).withIdentifier(applyIdentifier);
                    drawerBuilder.addDrawerItems(apply);
                    break;

                case "FAQs":
                    faqs = new PrimaryDrawerItem().withName(thaFAQs).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_questions)).withIconTintingEnabled(true).withIdentifier(i + 1);
                    drawerBuilder.addDrawerItems(faqs);
                    break;

                case "Zooper":
                    WITH_ZOOPER_SECTION = true;
                    zooper = new PrimaryDrawerItem().withName(thaZooper).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_zooper_kustom)).withIconTintingEnabled(true).withIdentifier(i + 1);
                    drawerBuilder.addDrawerItems(zooper);
                    break;

                case "Kustom":
                    kustom = new PrimaryDrawerItem().withName(thaKustom).withIcon(Utils.getVectorDrawable(context, R.drawable.ic_zooper_kustom)).withIconTintingEnabled(true).withIdentifier(i + 1);
                    drawerBuilder.addDrawerItems(kustom);
            }
        }

        drawerBuilder.addDrawerItems(new DividerDrawerItem()); //divider between primary and secondary

        for (int i = 0; i < secondaryDrawerItems.length; i++) {
            switch (secondaryDrawerItems[i]) {
                case "Credits":
                    creditsItem = new SecondaryDrawerItem().withName(thaCredits).withIdentifier(i + secondaryStart);
                    drawerBuilder.addDrawerItems(creditsItem);
                    break;
                case "Settings":
                    settingsItem = new SecondaryDrawerItem().withName(thaSettings).withIdentifier(i + secondaryStart);
                    drawerBuilder.addDrawerItems(settingsItem);
                    break;
                case "Donations":
                    donationsItem = new SecondaryDrawerItem().withName(thaDonate).withIdentifier(i + secondaryStart);
                    drawerBuilder.addDrawerItems(donationsItem);
                    break;
            }
        }

        drawerBuilder.withSavedInstance(savedInstanceState);

        String headerAppName = "", headerAppVersion = "";

        boolean withDrawerTexts;

        if (ENABLE_DEV_OPTIONS) {
            withDrawerTexts = mPrefs.getDevDrawerTexts();
        } else {
            withDrawerTexts = getResources().getBoolean(R.bool.with_drawer_texts);
        }

        if (withDrawerTexts) {
            headerAppName = getResources().getString(R.string.app_long_name);
            headerAppVersion = "v " + Utils.getAppVersion(this);
        }

        switch (drawerHeaderStyle) {
            case 1:
                AccountHeader drawerHeader = new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(ThemeUtils.darkTheme ?
                                ThemeUtils.transparent ?
                                        R.drawable.drawer_header_clear
                                        : R.drawable.drawer_header_dark
                                : R.drawable.drawer_header_light)
                        .withSelectionFirstLine(headerAppName)
                        .withSelectionSecondLine(headerAppVersion)
                        .withProfileImagesClickable(false)
                        .withResetDrawerOnProfileListClick(false)
                        .withSelectionListEnabled(false)
                        .withSelectionListEnabledForSingleProfile(false)
                        .withSavedInstance(savedInstanceState)
                        .build();

                drawerBuilder.withAccountHeader(drawerHeader);
                break;
            case 2:
                drawerBuilder.withHeader(R.layout.mini_drawer_header);
                break;
            case 3:
                break;
        }

        drawer = drawerBuilder.build();

        if (drawerHeaderStyle == 2) {
            ImageView miniHeader = (ImageView) drawer.getHeader().findViewById(R.id.mini_drawer_header);
            miniHeader.getLayoutParams().height = UIUtils.getActionBarHeight(this) + UIUtils.getStatusBarHeight(this);
            TextView appVersion = (TextView) drawer.getHeader().findViewById(R.id.text_app_version);
            TextView appName = (TextView) drawer.getHeader().findViewById(R.id.text_app_name);

            boolean miniHeaderSolidColor;

            if (ENABLE_DEV_OPTIONS) {
                miniHeaderSolidColor = !(mPrefs.getDevMiniDrawerHeaderPicture());
            } else {
                miniHeaderSolidColor = context.getResources().getBoolean(R.bool.mini_header_solid_color);
            }

            if (miniHeaderSolidColor) {
                int backgroundColor = ThemeUtils.darkTheme ?
                        ContextCompat.getColor(context, R.color.dark_theme_primary) :
                        ContextCompat.getColor(context, R.color.light_theme_primary);
                miniHeader.setBackgroundColor(backgroundColor);
                int iconsColor = ThemeUtils.darkTheme ?
                        ContextCompat.getColor(this, R.color.toolbar_text_dark) :
                        ContextCompat.getColor(this, R.color.toolbar_text_light);
                appVersion.setTextColor(iconsColor);
                appName.setTextColor(iconsColor);
            } else {
                miniHeader.setImageDrawable(ThemeUtils.darkTheme ?
                        ThemeUtils.transparent ?
                                ContextCompat.getDrawable(context, R.drawable.drawer_header_clear)
                                : ContextCompat.getDrawable(context, R.drawable.drawer_header_dark)
                        : ContextCompat.getDrawable(context, R.drawable.drawer_header_light));
                appVersion.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                appName.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
            appName.setText(headerAppName);
            appVersion.setText(headerAppVersion);
        }

    }

    public void drawerItemClick(long id) {
        if (id <= primaryDrawerItems.length) {
            switchFragment(id, primaryDrawerItems[(int) id - 1], context);
        } else {
            switchFragment(id, secondaryDrawerItems[((int) (id - secondaryStart))], context);
        }
    }

    private void getAction() {
        String action;
        try {
            action = getIntent().getAction();
        } catch (Exception e) {
            action = "action";
        }

        try {
            switch (action) {
                case adw_action:
                case turbo_action:
                case nova_action:
                case Intent.ACTION_PICK:
                case Intent.ACTION_GET_CONTENT:
                    iconsPicker = true;
                    wallsPicker = false;
                    break;
                case Intent.ACTION_SET_WALLPAPER:
                    iconsPicker = false;
                    wallsPicker = true;
                    break;
                default:
                    iconsPicker = false;
                    wallsPicker = false;
                    break;
            }
        } catch (ActivityNotFoundException | NullPointerException e) {
            iconsPicker = false;
            wallsPicker = false;
        }
    }

    public void setupIcons() {

        ArrayList<IconItem> icons = null;

        if (LoadIconsLists.getIconsLists() != null) {
            icons = LoadIconsLists.getIconsLists().get(1).getIconsArray();
        }

        ArrayList<IconItem> finalIconsList = new ArrayList<>();

        if (icons != null && SHUFFLE && shuffleIcons) {
            Collections.shuffle(icons);
        }

        int i = 0;

        if (icons != null) {
            while (i < numOfIcons) {
                finalIconsList.add(icons.get(i));
                i++;
            }

            icon1.setImageResource(finalIconsList.get(0).getResId());
            icon2.setImageResource(finalIconsList.get(1).getResId());
            icon3.setImageResource(finalIconsList.get(2).getResId());
            icon4.setImageResource(finalIconsList.get(3).getResId());

            if (numOfIcons == 6) {
                icon5.setImageResource(finalIconsList.get(4).getResId());
                icon6.setImageResource(finalIconsList.get(5).getResId());
            } else if (numOfIcons == 8) {
                icon5.setImageResource(finalIconsList.get(4).getResId());
                icon6.setImageResource(finalIconsList.get(5).getResId());
                icon7.setImageResource(finalIconsList.get(6).getResId());
                icon8.setImageResource(finalIconsList.get(7).getResId());
            }
        }

        SHUFFLE = false;

    }

    public void animateIcons(int delay) {

        if (!iconsPicker && !wallsPicker) {
            switch (numOfIcons) {
                case 4:
                    if (icon1 != null) icon1.setVisibility(View.VISIBLE);
                    if (icon2 != null) icon2.setVisibility(View.VISIBLE);
                    if (icon3 != null) icon3.setVisibility(View.VISIBLE);
                    if (icon4 != null) icon4.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    if (icon1 != null) icon1.setVisibility(View.VISIBLE);
                    if (icon2 != null) icon2.setVisibility(View.VISIBLE);
                    if (icon3 != null) icon3.setVisibility(View.VISIBLE);
                    if (icon4 != null) icon4.setVisibility(View.VISIBLE);
                    if (icon5 != null) icon5.setVisibility(View.VISIBLE);
                    if (icon6 != null) icon6.setVisibility(View.VISIBLE);
                    break;
                case 8:
                    if (icon1 != null) icon1.setVisibility(View.VISIBLE);
                    if (icon2 != null) icon2.setVisibility(View.VISIBLE);
                    if (icon3 != null) icon3.setVisibility(View.VISIBLE);
                    if (icon4 != null) icon4.setVisibility(View.VISIBLE);
                    if (icon5 != null) icon5.setVisibility(View.VISIBLE);
                    if (icon6 != null) icon6.setVisibility(View.VISIBLE);
                    if (icon7 != null) icon7.setVisibility(View.VISIBLE);
                    if (icon8 != null) icon8.setVisibility(View.VISIBLE);
                    break;
            }
        }

        if (mPrefs.getAnimationsEnabled()) {
            final Animation anim = AnimationUtils.loadAnimation(context, R.anim.bounce);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playIconsAnimations(anim);
                }
            }, delay);
        }

    }

    private void playIconsAnimations(Animation anim) {

        icon1.startAnimation(anim);
        icon2.startAnimation(anim);
        icon3.startAnimation(anim);
        icon4.startAnimation(anim);

        switch (numOfIcons) {
            case 6:
                icon5.startAnimation(anim);
                icon6.startAnimation(anim);
                break;
            case 8:
                icon5.startAnimation(anim);
                icon6.startAnimation(anim);
                icon7.startAnimation(anim);
                icon8.startAnimation(anim);
                break;
        }
    }

    public static void setupToolbarHeader(Context context, ImageView toolbarHeader) {

        if (themeMode) {
            wallpaperDrawable = ContextCompat.getDrawable(context, R.drawable.heroimage);
            toolbarHeader.setImageDrawable(wallpaperDrawable);
            toolbarHeaderImage = Utils.drawableToBitmap(wallpaperDrawable);
        } else if (ENABLE_USER_WALLPAPER_IN_TOOLBAR && mPrefs.getWallpaperAsToolbarHeaderEnabled()) {
            WallpaperManager wm = WallpaperManager.getInstance(context);

            if (wm != null) {
                Drawable currentWallpaper = wm.getFastDrawable();
                if (currentWallpaper != null) {
                    toolbarHeader.setAlpha(0.9f);
                    toolbarHeader.setImageDrawable(currentWallpaper);
                    wallpaperDrawable = currentWallpaper;
                    toolbarHeaderImage = Utils.drawableToBitmap(currentWallpaper);
                }
            }
        } else {
            String[] wallpapers = context.getResources().getStringArray(R.array.wallpapers);

            if (wallpapers.length > 0) {
                int res;
                ArrayList<Integer> wallpapersArray = new ArrayList<>();

                for (String wallpaper : wallpapers) {
                    res = context.getResources().getIdentifier(wallpaper, "drawable", context.getPackageName());
                    if (res != 0) {
                        final int thumbRes = context.getResources().getIdentifier(wallpaper, "drawable", context.getPackageName());
                        if (thumbRes != 0) {
                            wallpapersArray.add(thumbRes);
                        }
                    }
                }

                Random random = new Random();

                if (wallpaper == -1) {
                    wallpaper = random.nextInt(wallpapersArray.size());
                }

                wallpaperDrawable = ContextCompat.getDrawable(context, wallpapersArray.get(wallpaper));
                toolbarHeader.setImageDrawable(wallpaperDrawable);
                toolbarHeaderImage = Utils.drawableToBitmap(
                        ContextCompat.getDrawable(context, wallpapersArray.get(wallpaper)));
            }
        }

        toolbarHeader.setVisibility(View.VISIBLE);
    }

    private boolean isPremium() {
        return mIsPremium;
    }

    public MaterialDialog getSettingsDialog() {
        return this.settingsDialog;
    }

    public void setSettingsDialog(MaterialDialog settingsDialog) {
        this.settingsDialog = settingsDialog;
    }

    public MaterialDialog getChangelogDialog() {
        return this.changelogDialog;
    }

    public void setChangelogDialog(MaterialDialog changelogDialog) {
        this.changelogDialog = changelogDialog;
    }

    public Toolbar getToolbar() {
        return this.toolbar;
    }

    public AppBarLayout getAppbar() {
        return this.appbar;
    }

    public ImageView getToolbarHeader() {
        return this.toolbarHeader;
    }

    public Bitmap getToolbarHeaderImage() {
        return this.toolbarHeaderImage;
    }

    public Drawer getDrawer() {
        return this.drawer;
    }

    @Override
    public void onFolderSelection(File folder) {
        mPrefs.setDownloadsFolder(folder.getAbsolutePath());
        SettingsFragment.changeWallsFolderValue(this, mPrefs);
    }

    private void setupDonations() {
        //donations stuff
        //google
        if (DONATIONS_GOOGLE) {
            GOOGLE_CATALOG_FREE = getResources().getStringArray(R.array.nonconsumable_google_donation_items);
            GOOGLE_CATALOG_PRO = getResources().getStringArray(R.array.consumable_google_donation_items);
            mGoogleCatalog = GOOGLE_CATALOG_FREE;
            GOOGLE_CATALOG_VALUES = getResources().getStringArray(R.array.google_donations_catalog);

            //TODO check if 50 is a good reference value
            try {
                if (!(GOOGLE_PUBKEY.length() > 50) || !(GOOGLE_CATALOG_VALUES.length > 0) || !(GOOGLE_CATALOG_FREE.length == GOOGLE_CATALOG_PRO.length) || !(GOOGLE_CATALOG_FREE.length == GOOGLE_CATALOG_VALUES.length)) {
                    DONATIONS_GOOGLE = false; //google donations setup is incorrect
                }
            } catch (Exception e) {
                DONATIONS_GOOGLE = false;
            }

        }

        //paypal
        if (DONATIONS_PAYPAL) {
            PAYPAL_USER = getResources().getString(R.string.paypal_user);
            PAYPAL_CURRENCY_CODE = getResources().getString(R.string.paypal_currency_code);
            if (!(PAYPAL_USER.length() > 5) || !(PAYPAL_CURRENCY_CODE.length() > 1)) {
                DONATIONS_PAYPAL = false; //paypal content incorrect
            }
        }

        if (WITH_DONATIONS_SECTION) {
            WITH_DONATIONS_SECTION = DONATIONS_GOOGLE || DONATIONS_PAYPAL || DONATIONS_FLATTR || DONATIONS_BITCOIN; //if one of the donations are enabled, then the section is enabled
        }
    }

}