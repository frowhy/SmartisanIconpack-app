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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.tasks.ApplyWallpaper;
import jahirfiquitiva.iconshowcase.tasks.WallpaperToCrop;
import jahirfiquitiva.iconshowcase.utilities.PermissionUtils;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.utilities.color.ColorExtractor;
import jahirfiquitiva.iconshowcase.utilities.color.ColorUtils;
import jahirfiquitiva.iconshowcase.utilities.color.ToolbarColorizer;
import jahirfiquitiva.iconshowcase.views.TouchImageView;


public class ViewerActivity extends AppCompatActivity {

    private boolean mLastTheme, mLastNavBar, usePalette;

    private WallpaperItem item;

    private RelativeLayout layout;
    private static Preferences mPrefs;
    private static File downloadsFolder;
    private MaterialDialog dialogApply;
    private Toolbar toolbar;

    private LinearLayout toHide1, toHide2;

    private static MaterialDialog downloadDialog;

    private Activity context;

    @SuppressWarnings("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.onActivityCreateSetTheme(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ThemeUtils.onActivityCreateSetNavBar(this);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        super.onCreate(savedInstanceState);

        context = this;

        usePalette = getResources().getBoolean(R.bool.use_palette_api_in_viewer);

        mPrefs = new Preferences(context);

        mPrefs.setActivityVisible(true);

        Intent intent = getIntent();
        String transitionName = intent.getStringExtra("transitionName");

        item = intent.getParcelableExtra("item");

        setContentView(R.layout.wall_viewer_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        final int iconsColor = ThemeUtils.darkTheme ?
                ContextCompat.getColor(context, R.color.toolbar_text_dark) :
                ContextCompat.getColor(context, R.color.toolbar_text_light);

        ToolbarColorizer.colorizeToolbar(toolbar, iconsColor);

        toHide1 = (LinearLayout) findViewById(R.id.iconsA);
        toHide2 = (LinearLayout) findViewById(R.id.iconsB);

        int tintLightLighter = ContextCompat.getColor(context, R.color.drawable_base_tint);
        int tintDark = ContextCompat.getColor(context, R.color.drawable_tint_dark);

        Drawable save = ColorUtils.getTintedIcon(context,
                R.drawable.ic_save,
                ThemeUtils.darkTheme ? tintDark : tintLightLighter);

        Drawable apply = ColorUtils.getTintedIcon(context,
                R.drawable.ic_apply_wallpaper,
                ThemeUtils.darkTheme ? tintDark : tintLightLighter);

        Drawable info = ColorUtils.getTintedIcon(context,
                R.drawable.ic_info,
                ThemeUtils.darkTheme ? tintDark : tintLightLighter);

        ImageView saveIV = (ImageView) findViewById(R.id.download);
        if (item.isDownloadable()) {
            saveIV.setImageDrawable(save);
            saveIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!PermissionUtils.canAccessStorage(context)) {
                        PermissionUtils.setViewerActivityAction("save");
                        PermissionUtils.requestStoragePermission(context);
                    } else {
                        showDialogs("save");
                    }
                }
            });
        } else {
            saveIV.setVisibility(View.GONE);
        }

        ImageView applyIV = (ImageView) findViewById(R.id.apply);
        applyIV.setImageDrawable(apply);
        applyIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showApplyWallpaperDialog(context, item.getWallURL());
            }
        });

        ImageView infoIV = (ImageView) findViewById(R.id.info);
        infoIV.setImageDrawable(info);
        infoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ISDialogs.showWallpaperDetailsDialog(context, item.getWallName(), item.getWallAuthor(), item.getWallDimensions(), item.getWallCopyright());
            }
        });

        TouchImageView mPhoto = (TouchImageView) findViewById(R.id.big_wallpaper);
        ViewCompat.setTransitionName(mPhoto, transitionName);

        layout = (RelativeLayout) findViewById(R.id.viewerLayout);

        TextView wallNameText = (TextView) findViewById(R.id.wallName);
        wallNameText.setText(item.getWallName());

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            if (filename != null) {
                FileInputStream is = context.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
            } else {
                bmp = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int colorFromCachedPic = 0;

        if (bmp != null) {
            colorFromCachedPic = ColorExtractor.getFinalGeneratedIconsColorFromPalette(bmp, usePalette);
        } else {
            colorFromCachedPic = ThemeUtils.darkTheme ? tintDark : tintLightLighter;
        }

        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress);
        spinner.getIndeterminateDrawable()
                .setColorFilter(colorFromCachedPic, PorterDuff.Mode.SRC_IN);

        ToolbarColorizer.colorizeToolbar(toolbar, colorFromCachedPic);

        Drawable d;
        if (bmp != null) {
            d = new GlideBitmapDrawable(getResources(), bmp);
        } else {
            d = new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent));
        }

        if (mPrefs.getAnimationsEnabled()) {
            Glide.with(context)
                    .load(item.getWallURL())
                    .placeholder(d)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Bitmap picture = ((GlideBitmapDrawable) resource).getBitmap();
                            ToolbarColorizer.colorizeToolbar(toolbar,
                                    ColorExtractor.getFinalGeneratedIconsColorFromPalette(picture, usePalette));
                            spinner.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mPhoto);
        } else {
            Glide.with(context)
                    .load(item.getWallURL())
                    .placeholder(d)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Bitmap picture = ((GlideBitmapDrawable) resource).getBitmap();
                            ToolbarColorizer.colorizeToolbar(toolbar,
                                    ColorExtractor.getFinalGeneratedIconsColorFromPalette(picture, usePalette));
                            spinner.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mPhoto);
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
        if (mLastTheme != ThemeUtils.darkTheme
                || mLastNavBar != ThemeUtils.coloredNavBar) {
            ThemeUtils.restartActivity(context);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialogApply != null) {
            dialogApply.dismiss();
            dialogApply = null;
        }
        if (mPrefs == null) {
            mPrefs = new Preferences(this);
        }
        mPrefs.setActivityVisible(false);
    }

    @Override
    public void onBackPressed() {
        closeViewer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeViewer();
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                if (PermissionUtils.getViewerActivityAction().equals("crop")) {
                    cropWallpaper(item.getWallURL());
                } else {
                    showDialogs(PermissionUtils.getViewerActivityAction());
                }
            } else {
                ISDialogs.showPermissionNotGrantedDialog(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            //Crop request
            if (toHide1 != null && toHide2 != null) {
                toHide1.setVisibility(View.VISIBLE);
                toHide2.setVisibility(View.VISIBLE);
            }
        }
    }

    private void closeViewer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportFinishAfterTransition();
        } else {
            finish();
        }
    }

    private void saveWallpaperAction(final String name, String url) {

        if (downloadDialog != null) {
            downloadDialog.dismiss();
        }

        final boolean[] enteredDownloadTask = {false};

        downloadDialog = new MaterialDialog.Builder(context)
                .content(R.string.downloading_wallpaper)
                .progress(true, 0)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (downloadDialog != null) {
                            downloadDialog.dismiss();
                        }
                    }
                })
                .show();

        Glide.with(context)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (resource != null && downloadDialog.isShowing()) {
                            enteredDownloadTask[0] = true;
                            saveWallpaper(context, name, downloadDialog, resource);
                        }
                    }
                });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUIThread(context, new Runnable() {
                    @Override
                    public void run() {
                        if (!enteredDownloadTask[0]) {
                            String newContent = context.getString(R.string.downloading_wallpaper)
                                    + "\n"
                                    + context.getString(R.string.download_takes_longer);
                            downloadDialog.setContent(newContent);
                            downloadDialog.setActionButton(DialogAction.POSITIVE, android.R.string.cancel);
                        }
                    }
                });
            }
        }, 15000);
    }

    private void saveWallpaper(final Activity context, final String wallName,
                               final MaterialDialog downloadDialog, final Bitmap result) {
        downloadDialog.setContent(context.getString(R.string.saving_wallpaper));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mPrefs.getDownloadsFolder() != null) {
                    downloadsFolder = new File(mPrefs.getDownloadsFolder());
                } else {
                    downloadsFolder = new File(context.getString(R.string.walls_save_location,
                            Environment.getExternalStorageDirectory().getAbsolutePath()));
                }
                //noinspection ResultOfMethodCallIgnored
                downloadsFolder.mkdirs();
                final File destFile = new File(downloadsFolder, wallName + ".png");
                String snackbarText;
                if (!destFile.exists()) {
                    try {
                        result.compress(Bitmap.CompressFormat.PNG, 100,
                                new FileOutputStream(destFile));
                        snackbarText = context.getString(R.string.wallpaper_downloaded,
                                destFile.getAbsolutePath());
                    } catch (final Exception e) {
                        snackbarText = context.getString(R.string.error);
                    }
                } else {
                    snackbarText = context.getString(R.string.wallpaper_downloaded,
                            destFile.getAbsolutePath());
                }
                final String finalSnackbarText = snackbarText;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadDialog.dismiss();

                        if (toHide1 != null && toHide2 != null) {
                            toHide1.setVisibility(View.GONE);
                            toHide2.setVisibility(View.GONE);
                        }

                        Snackbar longSnackbar = Snackbar.make(layout, finalSnackbarText,
                                Snackbar.LENGTH_LONG);
                        final int snackbarLight = ContextCompat.getColor(context, R.color.snackbar_light);
                        final int snackbarDark = ContextCompat.getColor(context, R.color.snackbar_dark);
                        ViewGroup snackbarView = (ViewGroup) longSnackbar.getView();
                        snackbarView.setBackgroundColor(ThemeUtils.darkTheme ? snackbarDark : snackbarLight);
                        longSnackbar.show();
                        longSnackbar.setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                                if (toHide1 != null && toHide2 != null) {
                                    toHide1.setVisibility(View.VISIBLE);
                                    toHide2.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void showApplyWallpaperDialog(final Activity context, final String wallUrl) {
        ISDialogs.showApplyWallpaperDialog(context,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogApply != null) {
                            dialogApply.dismiss();
                        }

                        final ApplyWallpaper[] applyTask = new ApplyWallpaper[1];

                        final boolean[] enteredApplyTask = {false};

                        dialogApply = new MaterialDialog.Builder(context)
                                .content(R.string.downloading_wallpaper)
                                .progress(true, 0)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (applyTask[0] != null) {
                                            applyTask[0].cancel(true);
                                        }
                                        dialogApply.dismiss();
                                    }
                                })
                                .show();

                        Glide.with(context)
                                .load(wallUrl)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        if (resource != null && dialogApply.isShowing()) {
                                            enteredApplyTask[0] = true;

                                            if (dialogApply != null) {
                                                dialogApply.dismiss();
                                            }
                                            dialogApply = new MaterialDialog.Builder(context)
                                                    .content(R.string.setting_wall_title)
                                                    .progress(true, 0)
                                                    .cancelable(false)
                                                    .show();

                                            applyTask[0] = new ApplyWallpaper(context, dialogApply, resource, false, layout,
                                                    toHide1, toHide2);
                                            applyTask[0].execute();
                                        }
                                    }
                                });

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUIThread(context, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!enteredApplyTask[0]) {
                                            String newContent = context.getString(R.string.downloading_wallpaper)
                                                    + "\n"
                                                    + context.getString(R.string.download_takes_longer);
                                            dialogApply.setContent(newContent);
                                            dialogApply.setActionButton(DialogAction.POSITIVE, android.R.string.cancel);
                                        }
                                    }
                                });
                            }
                        }, 15000);
                    }
                }, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (!PermissionUtils.canAccessStorage(context)) {
                            PermissionUtils.setViewerActivityAction("crop");
                            PermissionUtils.requestStoragePermission(context);
                        } else {
                            cropWallpaper(wallUrl);
                        }
                    }
                });
    }

    private void showNotConnectedSnackBar() {
        Snackbar notConnectedSnackBar = Snackbar.make(layout, R.string.no_conn_title,
                Snackbar.LENGTH_LONG);

        final int snackbarLight = ContextCompat.getColor(context, R.color.snackbar_light);
        final int snackbarDark = ContextCompat.getColor(context, R.color.snackbar_dark);
        ViewGroup snackbarView = (ViewGroup) notConnectedSnackBar.getView();
        snackbarView.setBackgroundColor(ThemeUtils.darkTheme ? snackbarDark : snackbarLight);
        notConnectedSnackBar.show();
    }

    private void showDialogs(String action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            new MaterialDialog.Builder(context)
                    .title(R.string.md_error_label)
                    .content(context.getResources().getString(R.string.md_storage_perm_error,
                            context.getResources().getString(R.string.app_name)))
                    .positiveText(android.R.string.ok)
                    .show();
        } else {
            if (Utils.hasNetwork(context)) {
                switch (action) {
                    case "save":
                        saveWallpaperAction(item.getWallName(), item.getWallURL());
                        break;
                }
            } else {
                showNotConnectedSnackBar();
            }
        }
    }

    private static Handler handler(Context context) {
        return new Handler(context.getMainLooper());
    }

    private static void runOnUIThread(Context context, Runnable r) {
        handler(context).post(r);
    }

    private void cropWallpaper(String wallUrl) {
        if (dialogApply != null) {
            dialogApply.dismiss();
        }

        final WallpaperToCrop[] cropTask = new WallpaperToCrop[1];

        final boolean[] enteredCropTask = {false};

        dialogApply = new MaterialDialog.Builder(context)
                .content(R.string.downloading_wallpaper)
                .progress(true, 0)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (cropTask[0] != null) {
                            cropTask[0].cancel(true);
                        }
                        dialogApply.dismiss();
                    }
                })
                .show();

        Glide.with(context)
                .load(wallUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (resource != null && dialogApply.isShowing()) {
                            enteredCropTask[0] = true;
                            if (dialogApply != null) {
                                dialogApply.dismiss();
                            }
                            dialogApply = new MaterialDialog.Builder(context)
                                    .content(context.getString(R.string.preparing_wallpaper))
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            if (cropTask[0] != null) {
                                                cropTask[0].cancel(true);
                                            }
                                            dialogApply.dismiss();
                                        }
                                    })
                                    .show();
                            cropTask[0] = new WallpaperToCrop(context, dialogApply, resource,
                                    layout, item.getWallName(), toHide1, toHide2);
                            cropTask[0].execute();
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUIThread(context, new Runnable() {
                                        @Override
                                        public void run() {
                                            String content = context.getString(R.string.preparing_wallpaper)
                                                    + "\n" + context.getString(R.string.download_takes_longer);

                                            dialogApply.setContent(content);
                                            dialogApply.setActionButton(DialogAction.POSITIVE, android.R.string.cancel);
                                        }
                                    });
                                }
                            }, 7000);
                        }
                    }
                });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUIThread(context, new Runnable() {
                    @Override
                    public void run() {
                        if (!enteredCropTask[0]) {
                            String newContent = context.getString(R.string.downloading_wallpaper)
                                    + "\n"
                                    + context.getString(R.string.download_takes_longer);
                            dialogApply.setContent(newContent);
                            dialogApply.setActionButton(DialogAction.POSITIVE, android.R.string.cancel);
                        }
                    }
                });
            }
        }, 15000);
    }

}