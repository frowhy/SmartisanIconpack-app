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

package jahirfiquitiva.iconshowcase.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jahirfiquitiva.iconshowcase.R;

public class FolderChooserDialog extends DialogFragment implements MaterialDialog.ListCallback {

    private File parentFolder;
    private File[] parentContents;
    private boolean canGoUp = true;
    private FolderSelectionCallback mCallback;
    private String initialPath;

    public interface FolderSelectionCallback {

        void onFolderSelection(File folder);
    }

    public FolderChooserDialog() {
    }

    public void setInitialPath(String path) {
        if (path == null)
            path = File.separator;
        initialPath = path;
    }

    private String getInitialPath() {
        return initialPath == null ? Environment.getExternalStorageDirectory().getAbsolutePath() : initialPath;
    }

    private String[] getContentsArray() {
        if (parentContents == null) return new String[]{};
        String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
        if (canGoUp) results[0] = "...";
        for (int i = 0; i < parentContents.length; i++)
            results[canGoUp ? i + 1 : i] = parentContents[i].getName();
        return results;
    }

    private File[] listFiles() {
        File[] contents = parentFolder.listFiles();
        List<File> results = new ArrayList<>();
        if (contents != null) {
            for (File fi : contents) {
                if (fi.isDirectory()) results.add(fi);
            }
            Collections.sort(results, new FolderSorter());
            return results.toArray(new File[results.size()]);
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            return new MaterialDialog.Builder(getActivity())
                    .title(R.string.md_error_label)
                    .content(getResources().getString(R.string.md_storage_perm_error, R.string.app_name))
                    .positiveText(android.R.string.ok)
                    .build();
        } else {
            parentFolder = new File(getInitialPath());
            parentContents = listFiles();

            return new MaterialDialog.Builder(getActivity())
                    .title(parentFolder.getAbsolutePath())
                    .items(getContentsArray())
                    .itemsCallback(this)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            mCallback.onFolderSelection(parentFolder);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            createFolder(getActivity(), dialog, parentFolder.getAbsolutePath());
                        }
                    })
                    .autoDismiss(false)
                    .positiveText(R.string.choose)
                    .negativeText(android.R.string.cancel)
                    .neutralText(R.string.new_folder)
                    .build();
        }
    }

    @Override
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence s) {
        if (canGoUp && i == 0) {
            parentFolder = parentFolder.getParentFile();
            canGoUp = parentFolder.getParent() != null;
        } else {
            parentFolder = parentContents[canGoUp ? i - 1 : i];
            canGoUp = true;
        }
        parentContents = listFiles();
        MaterialDialog dialog = (MaterialDialog) getDialog();
        dialog.setTitle(parentFolder.getAbsolutePath());
        dialog.setItems(getContentsArray());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (FolderSelectionCallback) activity;
    }

    public void show(AppCompatActivity context) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag("FOLDER_SELECTOR");
        if (frag != null) {
            ((DialogFragment) frag).dismiss();
            context.getSupportFragmentManager().beginTransaction()
                    .remove(frag).commit();
        }
        show(context.getSupportFragmentManager(), "FOLDER_SELECTOR");
    }

    private void createFolder(Context context, final MaterialDialog folderChooserDialog, final String folderPath) {
        new MaterialDialog.Builder(context)
                .title(R.string.new_folder_title)
                .content(R.string.new_folder_content)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .input(R.string.new_folder_hint, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        File folder = new File(folderPath + File.separator + input.toString());
                        if (!folder.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            folder.mkdir();
                        }
                        parentContents = listFiles();
                        folderChooserDialog.setItems(getContentsArray());
                    }
                }).show();

    }

    private static class FolderSorter implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

}