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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.models.RequestItem;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class ZipFilesToRequest extends AsyncTask<Void, String, Boolean> {

    private final MaterialDialog dialog;
    private ArrayList<RequestItem> appsListFinal;
    private static final int BUFFER = 2048;
    private String zipFilePath;
    private WeakReference<Context> context;
    private StringBuilder emailContent = new StringBuilder();
    private final WeakReference<Activity> wrActivity;
    private Activity activity;
    private Preferences mPrefs;
    private File filesFolder;

    public ZipFilesToRequest(Activity activity, MaterialDialog dialog,
                             ArrayList<RequestItem> appsListFinal) {
        this.wrActivity = new WeakReference<>(activity);
        this.dialog = dialog;
        this.appsListFinal = appsListFinal;
        this.mPrefs = new Preferences(activity);
    }

    @Override
    protected void onPreExecute() {
        final Activity act = wrActivity.get();
        this.filesFolder = null;
        if (act != null) {
            this.context = new WeakReference<>(act.getApplicationContext());
            this.activity = act;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected Boolean doInBackground(Void... params) {
        boolean worked;

        String zipLocation = context.get().getString(R.string.request_save_location,
                Environment.getExternalStorageDirectory().getAbsolutePath());
        String filesLocation = zipLocation + "Files";

        String appNameCorrected = context.get().getResources().getString(R.string.app_name).replace(" ", "");

        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.getDefault());
        String momentOfCreation = date.format(new Date());
        zipFilePath = zipLocation + appNameCorrected + "_" + momentOfCreation + ".zip";

        try {
            final File zipFolder = new File(zipLocation);
            filesFolder = new File(filesLocation + "/");

            deleteDirectory(zipFolder);
            deleteDirectory(filesFolder);

            zipFolder.mkdirs();
            filesFolder.mkdirs();

            StringBuilder sb = new StringBuilder();
            StringBuilder appFilterBuilder = new StringBuilder();
            StringBuilder appMapBuilder = new StringBuilder();
            StringBuilder themeResourcesBuilder = new StringBuilder();

            sb.append("These apps have no icons, please add some for them. Thanks in advance.\n\n");

            int appsCount = 0;
            for (int i = 0; i < appsListFinal.size(); i++) {

                if (appsListFinal.get(i).isSelected()) {

                    appFilterBuilder.append("<!-- " + appsListFinal.get(i).getAppName() +
                            " -->\n");

                    appMapBuilder.append("<!-- " + appsListFinal.get(i).getAppName() +
                            " -->\n");

                    themeResourcesBuilder.append("<!-- " + appsListFinal.get(i).getAppName() +
                            " -->\n");

                    appFilterBuilder.append("<item component=\"ComponentInfo{" +
                            appsListFinal.get(i).getPackageName() + "/" + appsListFinal.get(i).getClassName() + "}\"" +
                            " drawable=\"" + appsListFinal.get(i).getAppName().replace(" ", "_").toLowerCase() + "\"/>" + "\n\n");

                    appMapBuilder.append("<item class=\"" + appsListFinal.get(i).getClassName() +
                            "\" name=\"" + appsListFinal.get(i).getAppName().replace(" ", "_").toLowerCase() + "\" />" + "\n\n");

                    themeResourcesBuilder.append("<AppIcon name=\"" +
                            appsListFinal.get(i).getPackageName() + "/" + appsListFinal.get(i).getClassName() +
                            "\" image=\"" + appsListFinal.get(i).getAppName().replace(" ", "_").toLowerCase() + "\"/>" + "\n\n");

                    sb.append("App Name: " + appsListFinal.get(i).getAppName() + "\n");
                    sb.append("App ComponentInfo: " + appsListFinal.get(i).getPackageName() + "/" + appsListFinal.get(i).getClassName() + "\n");
                    sb.append("App Link: " + "http://a.app.qq.com/o/simple.jsp?pkgname=" + appsListFinal.get(i).getPackageName() + "\n");
                    sb.append("\n");

                    Bitmap bitmap = ((BitmapDrawable) (appsListFinal.get(i).getIcon())).getBitmap();

                    FileOutputStream fileOutputStream;
                    try {
                        fileOutputStream = new FileOutputStream(filesLocation + "/" + appsListFinal.get(i).getAppName().replace(" ", "_").toLowerCase() + ".png");
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        //Do nothing
                    }

                    appsCount += 1;
                }

            }

            int left = mPrefs.getRequestsLeft() - appsCount;
            mPrefs.setRequestsLeft(left >= 0 ? left : 0);

            sb.append("\nAndroid Version: " + Build.VERSION.RELEASE);
            sb.append("\nOS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")");
            sb.append("\nOS API Level: " + Build.VERSION.SDK_INT);

            try {
                PackageInfo appInfo = context.get().getPackageManager().getPackageInfo(context.get().getPackageName(), 0);
                sb.append("\nApp Version Name: " + appInfo.versionName);
                sb.append("\nApp Version Code: " + appInfo.versionCode);
            } catch (Exception e) {
                //Do nothing
            }

            sb.append("\nDevice: " + Build.MODEL);
            sb.append("\nManufacturer: " + Build.MANUFACTURER);
            sb.append("\nModel (and Product): " + Build.DEVICE + " (" + Build.PRODUCT + ")");
            if (context.get().getResources().getBoolean(R.bool.theme_engine_info)) {
                if (Utils.isAppInstalled(context.get(), "org.cyanogenmod.theme.chooser")) {
                    sb.append("\nCMTE is installed");
                }
                if (Utils.isAppInstalled(context.get(), "com.cyngn.theme.chooser")) {
                    sb.append("\nCyngn theme engine is installed");
                }
                if (Utils.isAppInstalled(context.get(), "com.lovejoy777.rroandlayersmanager")) {
                    sb.append("\nLayers Manager is installed");
                }
            }

            if (appsCount != 0) {

                try {
                    FileWriter fileWriter1 = new FileWriter(filesLocation + "/" + "appfilter" + "_" + momentOfCreation + ".xml");
                    BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
                    bufferedWriter1.write(appFilterBuilder.toString());
                    bufferedWriter1.close();
                } catch (Exception e) {
                    worked = false;
                }

                try {
                    FileWriter fileWriter2 = new FileWriter(filesLocation + "/" + "appmap" + "_" + momentOfCreation + ".xml");
                    BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
                    bufferedWriter2.write(appMapBuilder.toString());
                    bufferedWriter2.close();
                } catch (Exception e) {
                    worked = false;
                }

                try {
                    FileWriter fileWriter3 = new FileWriter(filesLocation + "/" + "theme_resources" + "_" + momentOfCreation + ".xml");
                    BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);
                    bufferedWriter3.write(themeResourcesBuilder.toString());
                    bufferedWriter3.close();
                } catch (Exception e) {
                    worked = false;
                }

                createZipFile(filesLocation, zipFilePath);
                deleteDirectory(filesFolder);

            }

            worked = true;
            emailContent = sb;

        } catch (Exception e) {
            worked = false;
            emailContent = null;
            e.getLocalizedMessage();
        }

        return worked;

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onPostExecute(Boolean worked) {

        if (worked) {

            if (emailContent != null) {
                dialog.dismiss();
                final Uri uri = Uri.parse("file://" + zipFilePath);

                String[] recipients = new String[]{context.get().getString(R.string.email_id)};

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("application/zip");
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.putExtra("android.intent.extra.EMAIL", recipients);
                sendIntent.putExtra("android.intent.extra.SUBJECT",
                        context.get().getString(R.string.request_title));
                sendIntent.putExtra("android.intent.extra.TEXT", emailContent.toString());
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    if (filesFolder != null) {
                        filesFolder.delete();
                    }
                    activity.startActivityForResult(Intent.createChooser(sendIntent, "Send mail..."), 2);
                    Calendar c = Calendar.getInstance();
                    Utils.saveCurrentTimeOfRequest(mPrefs, c);
                } catch (ActivityNotFoundException e) {
                    //Do nothing
                }
            }

        } else {
            dialog.setContent(R.string.error);
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                    String[] folderFiles = dir.list();
                    for (String folderFile : folderFiles) {
                        new File(dir, folderFile).delete();
                    }
                } else {
                    file.delete();
                }
            }

        }
    }

    private static void createZipFile(final String path, final String outputFile) {
        final File filesFolder = new File(path);

        if (!filesFolder.canRead() || !filesFolder.canWrite()) {
            return;
        }

        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(outputFile), BUFFER));
            try {
                zipFile(path, zipOutputStream, "");
            } catch (Exception e) {
                final File files[] = filesFolder.listFiles();
                for (final File file : files) {
                    zipFolder(file, zipOutputStream);
                }
            }
            zipOutputStream.close();
        } catch (FileNotFoundException e) {
            if (ShowcaseActivity.DEBUGGING) Utils.showLog("File not found: " + e.getMessage());
        } catch (IOException e) {
            if (ShowcaseActivity.DEBUGGING) Utils.showLog("IOException: " + e.getMessage());
        }
    }

    private static void zipFile(final String zipFilesPath, final ZipOutputStream zipOutputStream, final String zipPath) throws IOException {
        final File file = new File(zipFilesPath);

        if (!file.exists()) {
            return;
        }

        final byte[] buf = new byte[BUFFER];
        final String[] files = file.list();

        if (file.isFile()) {
            try {
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                zipOutputStream.putNextEntry(new ZipEntry(zipPath + file.getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    zipOutputStream.write(buf, 0, len);
                }
                zipOutputStream.closeEntry();
                in.close();
            } catch (ZipException e) {
                //Do nothing
            } finally {
                if (zipOutputStream != null) zipOutputStream.closeEntry();

            }
        } else if (files.length > 0) {
            for (String file1 : files) {
                zipFile(zipFilesPath + "/" + file1, zipOutputStream, zipPath + file.getName() + "/");
            }
        }
    }

    private static void zipFolder(File file, ZipOutputStream zipOutputStream) throws IOException {
        byte[] data = new byte[BUFFER];
        int read;

        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    new FileInputStream(file));
            while ((read = bufferedInputStream.read(data, 0, BUFFER)) != -1)
                zipOutputStream.write(data, 0, read);
            zipOutputStream.closeEntry();
            bufferedInputStream.close();
        } else if (file.isDirectory()) {
            String[] list = file.list();
            for (String aList : list)
                zipFolder(new File(file.getPath() + "/" + aList), zipOutputStream);
        }
    }

}