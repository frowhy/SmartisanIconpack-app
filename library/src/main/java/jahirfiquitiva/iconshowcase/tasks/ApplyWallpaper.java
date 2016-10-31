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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.lang.ref.WeakReference;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.utilities.color.ColorExtractor;


public class ApplyWallpaper extends AsyncTask<Void, String, Boolean> {

    private WeakReference<Context> context;
    private Activity activity;
    private final MaterialDialog dialog;
    private final Bitmap resource;
    private final View layout;
    private final boolean isPicker;
    private WeakReference<Activity> wrActivity;
    private LinearLayout toHide1, toHide2;
    private volatile boolean wasCancelled = false;

    public ApplyWallpaper(Context context, MaterialDialog dialog, Bitmap resource, boolean isPicker,
                          View layout) {
        this.context = new WeakReference<>(context);
        this.dialog = dialog;
        this.resource = resource;
        this.isPicker = isPicker;
        this.layout = layout;
    }

    public ApplyWallpaper(Activity activity, MaterialDialog dialog, Bitmap resource, boolean isPicker,
                          View layout, LinearLayout toHide1, LinearLayout toHide2) {
        this.wrActivity = new WeakReference<>(activity);
        this.dialog = dialog;
        this.resource = resource;
        this.isPicker = isPicker;
        this.layout = layout;
        this.toHide1 = toHide1;
        this.toHide2 = toHide2;
    }

    @Override
    protected void onPreExecute() {
        if (wrActivity != null) {
            activity = wrActivity.get();
        } else if (context != null) {
            activity = (Activity) context.get();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean worked = false;
        if ((!wasCancelled) && (activity != null)) {
            WallpaperManager wm = WallpaperManager.getInstance(activity);
            try {
                try {
                    wm.setBitmap(scaleToActualAspectRatio(resource));
                } catch (OutOfMemoryError ex) {
                    if (ShowcaseActivity.DEBUGGING)
                        Utils.showLog(activity, "OutOfMemoryError: " + ex.getLocalizedMessage());
                    showRetrySnackbar();
                }
                worked = true;
            } catch (IOException e2) {
                worked = false;
            }
        } else {
            worked = false;
        }
        return worked;
    }

    @Override
    protected void onCancelled() {
        wasCancelled = true;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        if (!wasCancelled) {
            if (worked) {
                dialog.dismiss();
                if (!isPicker) {

                    if (toHide1 != null && toHide2 != null) {
                        toHide1.setVisibility(View.GONE);
                        toHide2.setVisibility(View.GONE);
                    } else {
                        ShowcaseActivity.setupToolbarHeader(activity, ShowcaseActivity.toolbarHeader);
                        ColorExtractor.setupToolbarIconsAndTextsColors(activity,
                                ShowcaseActivity.appbar, ShowcaseActivity.toolbar,
                                ShowcaseActivity.toolbarHeaderImage);
                    }

                    Snackbar longSnackbar = Snackbar.make(layout,
                            activity.getString(R.string.set_as_wall_done), Snackbar.LENGTH_LONG);
                    final int snackbarLight = ContextCompat.getColor(activity, R.color.snackbar_light);
                    final int snackbarDark = ContextCompat.getColor(activity, R.color.snackbar_dark);
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
            } else {
                showRetrySnackbar();
            }
            if (isPicker) {
                activity.finish();
            }
        }
    }

    private Bitmap scaleToActualAspectRatio(Bitmap bitmap) {
        if (bitmap != null) {
            boolean flag = true;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int deviceWidth = displayMetrics.widthPixels;
            int deviceHeight = displayMetrics.heightPixels;

            int bitmapHeight = bitmap.getHeight();
            int bitmapWidth = bitmap.getWidth();
            if (bitmapWidth > deviceWidth) {
                flag = false;
                int scaledHeight = deviceHeight;
                int scaledWidth = (scaledHeight * bitmapWidth) / bitmapHeight;
                try {
                    if (scaledHeight > deviceHeight) {
                        scaledHeight = deviceHeight;
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth,
                            scaledHeight, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (flag) {
                if (bitmapHeight > deviceHeight) {
                    int scaledWidth = (deviceHeight * bitmapWidth)
                            / bitmapHeight;
                    try {
                        if (scaledWidth > deviceWidth)
                            scaledWidth = deviceWidth;
                        bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth,
                                deviceHeight, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    private void showRetrySnackbar() {
        String retry = activity.getResources().getString(R.string.retry);
        Snackbar snackbar = Snackbar
                .make(layout, R.string.error, Snackbar.LENGTH_INDEFINITE)
                .setAction(retry.toUpperCase(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new ApplyWallpaper((Activity) activity, dialog, resource, isPicker, layout);
                    }
                });
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(R.attr.accentColor, typedValue, true);
        int actionTextColor = typedValue.data;
        snackbar.setActionTextColor(actionTextColor);
        snackbar.show();
    }

}
