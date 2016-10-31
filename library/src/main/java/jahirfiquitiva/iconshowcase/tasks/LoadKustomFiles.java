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

package jahirfiquitiva.iconshowcase.tasks;

import android.content.Context;
import android.content.res.AssetManager;
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

import jahirfiquitiva.iconshowcase.models.KustomKomponent;
import jahirfiquitiva.iconshowcase.models.KustomWallpaper;
import jahirfiquitiva.iconshowcase.models.KustomWidget;
import jahirfiquitiva.iconshowcase.utilities.Utils;


public class LoadKustomFiles extends AsyncTask<Void, String, Boolean> {

    private final WeakReference<Context> context;
    public final static ArrayList<KustomKomponent> komponents = new ArrayList<>();
    public final static ArrayList<KustomWallpaper> wallpapers = new ArrayList<>();
    public final static ArrayList<KustomWidget> widgets = new ArrayList<>();
    private long startTime;

    public LoadKustomFiles(Context context) {
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

            String[] kustomFolders = {"komponents", "wallpapers", "widgets"};

            for (String kustomFolder : kustomFolders) {
                worked = readKustomFiles(assetManager, kustomFolder);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean readKustomFiles(AssetManager assetManager, String folder) {
        try {
            String[] kustomFiles = assetManager.list(folder);

            File previewsFolder = new File(context.get().getExternalCacheDir(), Utils.capitalizeText(folder) + "Previews");

            if (kustomFiles != null && kustomFiles.length > 0) {
                clean(previewsFolder);
                previewsFolder.mkdirs();
                for (String template : kustomFiles) {
                    File widgetPreviewFile = new File(previewsFolder, template);
                    String widgetName = getFilenameWithoutExtension(template);
                    String[] previews = getWidgetPreviewPathFromZip(widgetName, folder,
                            assetManager.open(folder + "/" + template), previewsFolder, widgetPreviewFile);
                    if (previews != null) {
                        switch (folder) {
                            case "komponents":
                                komponents.add(new KustomKomponent(previews[0]));
                                break;
                            case "wallpapers":
                                wallpapers.add(new KustomWallpaper(template, previews[0], previews[1]));
                                break;
                            case "widgets":
                                widgets.add(new KustomWidget(template, previews[0], previews[1]));
                                break;
                        }
                    }
                    widgetPreviewFile.delete();
                }
                switch (folder) {
                    case "komponents":
                        return komponents.size() == kustomFiles.length;
                    case "wallpapers":
                        return wallpapers.size() == kustomFiles.length;
                    case "widgets":
                        return widgets.size() == kustomFiles.length;
                    default:
                        return false;
                }
            } else {
                return false;
            }
        } catch (IOException ex) {
            return false;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String[] getWidgetPreviewPathFromZip(String name,
                                                 String folder,
                                                 InputStream in,
                                                 File previewsFolder, File widgetPreviewFile) {
        OutputStream out;

        name.replaceAll(".komp", "");
        name.replaceAll(".kwgt", "");
        name.replaceAll(".klwp", "");

        String[] thumbNames = {"", ""};
        switch (folder) {
            case "komponents":
                thumbNames[0] = "komponent_thumb";
                break;
            default:
                thumbNames[0] = "preset_thumb_portrait";
                thumbNames[1] = "preset_thumb_landscape";
                break;
        }

        File preview1 = new File(previewsFolder, name + "_port.jpg");
        File preview2 = new File(previewsFolder, name + "_land.jpg");

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
                    if (entry.getName().endsWith(thumbNames[0] + ".jpg")) {
                        InputStream zipIn = null;
                        OutputStream zipOut = null;
                        try {
                            zipIn = zipFile.getInputStream(entry);
                            zipOut = new FileOutputStream(preview1);
                            copyFiles(zipIn, zipOut);
                        } finally {
                            if (zipIn != null) zipIn.close();
                            if (zipOut != null) zipOut.close();
                        }
                    }

                    if (!(thumbNames[1].isEmpty())) {
                        if (entry.getName().endsWith(thumbNames[1] + ".jpg")) {
                            InputStream zipIn = null;
                            OutputStream zipOut = null;
                            try {
                                zipIn = zipFile.getInputStream(entry);
                                zipOut = new FileOutputStream(preview2);
                                copyFiles(zipIn, zipOut);
                            } finally {
                                if (zipIn != null) zipIn.close();
                                if (zipOut != null) zipOut.close();
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            //Do nothing
        }

        return new String[]{preview1.getAbsolutePath(), preview2.getAbsolutePath()};
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