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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.ZooperWidget;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class LoadZooperWidgets extends AsyncTask<Void, String, Boolean> {

    private final WeakReference<Context> context;
    public final static ArrayList<ZooperWidget> widgets = new ArrayList<>();
    private long startTime;

    public LoadZooperWidgets(Context context) {
        this.context = new WeakReference<Context>(context);
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected Boolean doInBackground(Void... params) {

        boolean worked = false;

        try {
            AssetManager assetManager = context.get().getAssets();
            String[] templates = assetManager.list("templates");

            File previewsFolder = new File(context.get().getExternalCacheDir(), "ZooperWidgetsPreviews");

            if (templates != null && templates.length > 0) {
                clean(previewsFolder);
                previewsFolder.mkdirs();
                for (String template : templates) {
                    File widgetPreviewFile = new File(previewsFolder, template);
                    String widgetName = getFilenameWithoutExtension(template);
                    String preview = getWidgetPreviewPathFromZip(context, widgetName,
                            assetManager.open("templates/" + template), previewsFolder, widgetPreviewFile);
                    if (preview != null) {
                        widgets.add(new ZooperWidget(preview));
                    }
                    widgetPreviewFile.delete();
                }
                worked = widgets.size() == templates.length;
            }
        } catch (Exception e) {
            //Do nothing
            worked = false;
        }

        return worked;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        long endTime = System.currentTimeMillis();
        if (worked) {
            Utils.showLog(context.get(),
                    "Load of widgets task completed successfully in: " +
                            String.valueOf((endTime - startTime)) + " millisecs.");
        }
    }

    private void copyFiles(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[2048];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    private String getFilenameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * This code was created by Aidan Follestad. Complete credits to him.
     */
    private String getWidgetPreviewPathFromZip(WeakReference<Context> context, String name, InputStream in,
                                               File previewsFolder, File widgetPreviewFile) {
        OutputStream out;
        File preview = new File(previewsFolder, name + ".png");

        try {
            out = new FileOutputStream(widgetPreviewFile);
            copyFiles(in, out);
            in.close();
            out.close();

            if (widgetPreviewFile.exists()) {
                ZipFile zipFile = new ZipFile(widgetPreviewFile);
                Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
                ZipEntry entry;
                while ((entry = entryEnum.nextElement()) != null) {
                    if (entry.getName().endsWith("screen.png")) {
                        InputStream zipIn = null;
                        OutputStream zipOut = null;
                        try {
                            zipIn = zipFile.getInputStream(entry);
                            zipOut = new FileOutputStream(preview);
                            copyFiles(zipIn, zipOut);
                        } finally {
                            if (zipIn != null) zipIn.close();
                            if (zipOut != null) zipOut.close();
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            //Do nothing
        }

        if (context.get().getResources().getBoolean(R.bool.remove_zooper_previews_background)) {
            out = null;
            try {
                Bitmap bmp = ZooperWidget.getTransparentBackgroundPreview(
                        BitmapFactory.decodeFile(preview.getAbsolutePath()));
                out = new FileOutputStream(preview);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                Utils.showLog(context.get(), "IOExcption: " + e.getLocalizedMessage());
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e1) {
                    Utils.showLog(context.get(), "IOExcption: " + e1.getLocalizedMessage());
                }
            }
        }

        return preview.getAbsolutePath();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static int clean(File file) {
        if (!file.exists()) return 0;
        int count = 0;
        if (file.isDirectory()) {
            File[] folderContent = file.listFiles();
            if (folderContent != null && folderContent.length > 0) {
                for (File fileInFolder : folderContent) {
                    count += clean(fileInFolder);
                }
            }
        }
        file.delete();
        return count;
    }

}